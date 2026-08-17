package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionManus;
import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import cyou.yuanbaomao.sellersprite.ai.research.curation.evidence.EvidenceDatasetPayload;
import cyou.yuanbaomao.sellersprite.ai.research.curation.evidence.EvidenceWorkbookAssembler;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.SheetAnalysisResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.report.AmazonSelectionMarkdownReportRenderer;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchAnalysisCancelledException;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisArtifactService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSseEventPublisher;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class ResearchAnalysisExecutorTest {

    private static final String ANALYSIS_RUN_ID = "analysis-run-1";
    private static final String JOB_ID = "job-1";
    private static final String USER_ID = "user-1";
    private static final String CONVERSATION_ID = "conversation-1";
    private static final String ANALYSIS_GOAL = "判断这个市场是否值得进入";
    private static final String EXECUTION_OWNER = "executor-1";
    private static final String EXECUTION_TOKEN = "token-1";

    @Mock
    private TaskScheduler heartbeatScheduler;

    @Mock
    private ScheduledFuture<?> heartbeatFuture;

    @Mock
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Mock
    private MarketResearchJobDao jobDao;

    @Mock
    private ResearchDatasetService datasetService;

    @Mock
    private EvidenceWorkbookAssembler workbookAssembler;

    @Mock
    private ObjectProvider<AmazonSelectionManus> agentProvider;

    @Mock
    private AmazonSelectionManus agent;

    @Mock
    private AmazonSelectionMarkdownReportRenderer reportRenderer;

    @Mock
    private ResearchAnalysisArtifactService artifactService;

    @Mock
    private ResearchAnalysisConversationRecorder conversationRecorder;

    @Mock
    private ResearchSseEventPublisher eventPublisher;

    private final AtomicReference<Runnable> heartbeatTask = new AtomicReference<>();
    private final AtomicLong nanoTime = new AtomicLong();

    private ResearchAnalysisExecutor executor;
    private CurationAnalysisProperties analysisProperties;
    private ResearchAnalysisLease lease;
    private MarketResearchAnalysisRun persistedRun;
    private ProductWorkbook workbook;
    private ResearchAnalysisConversationRecorder.Session session;
    private ResearchDataset evidenceDataset;

    @BeforeEach
    void setUp() {
        analysisProperties = new CurationAnalysisProperties();
        ResearchAnalysisStateService stateService = new ResearchAnalysisStateService(
                analysisRunDao,
                jobDao,
                new ResearchProperties(),
                eventPublisher);
        Executor directExecutor = Runnable::run;
        executor = new ResearchAnalysisExecutor(
                directExecutor,
                heartbeatScheduler,
                stateService,
                datasetService,
                workbookAssembler,
                agentProvider,
                reportRenderer,
                artifactService,
                conversationRecorder,
                eventPublisher,
                analysisProperties,
                nanoTime::get);

        lease = new ResearchAnalysisLease(
                ANALYSIS_RUN_ID,
                JOB_ID,
                USER_ID,
                CONVERSATION_ID,
                ResearchAnalysisRunType.FINAL_ANALYSIS.name(),
                ANALYSIS_GOAL,
                EXECUTION_OWNER,
                EXECUTION_TOKEN,
                1,
                1);
        persistedRun = runningRun();
        workbook = workbook();
        session = new ResearchAnalysisConversationRecorder.Session(ANALYSIS_RUN_ID, 1L);
        evidenceDataset = dataset();

        lenient().when(heartbeatScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class)))
                .thenAnswer(invocation -> {
                    heartbeatTask.set(invocation.getArgument(0));
                    return heartbeatFuture;
                });
        when(analysisRunDao.getById(ANALYSIS_RUN_ID)).thenReturn(persistedRun);
        lenient().when(jobDao.isOwnedRunning(JOB_ID, EXECUTION_TOKEN)).thenReturn(true);
        lenient().when(analysisRunDao.updateProgress(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyString(),
                anyInt()))
                .thenReturn(true);
        lenient().when(analysisRunDao.incrementCounters(
                eq(ANALYSIS_RUN_ID), eq(EXECUTION_OWNER), eq(EXECUTION_TOKEN), anyInt(), anyInt()))
                .thenReturn(true);
        lenient().when(analysisRunDao.saveFinalSummary(
                eq(ANALYSIS_RUN_ID), eq(EXECUTION_OWNER), eq(EXECUTION_TOKEN), anyString()))
                .thenReturn(true);
        lenient().when(datasetService.readEvidenceDatasets(JOB_ID)).thenReturn(List.of(evidenceDataset));
        lenient().when(workbookAssembler.assemble(eq(JOB_ID), any())).thenReturn(workbook);
        lenient().when(agentProvider.getObject()).thenReturn(agent);
        lenient().when(conversationRecorder.start(any(ResearchAnalysisLease.class))).thenReturn(session);
    }

    @Test
    void shouldPersistFinalStageMarkdownAndCompletionEvents() {
        AmazonSelectionReactResult result = successfulResult();
        whenFinalAgentRuns().thenAnswer(invocation -> {
            Consumer<AmazonSelectionReactEvent> eventConsumer = invocation.getArgument(4);
            CurationAnalysisBudget budget = invocation.getArgument(5);
            budget.beforeModelCall();
            budget.beforeModelCall();
            eventConsumer.accept(event(ResearchEventTypes.PLAN, "plan", null, "开始分析", ANALYSIS_GOAL));
            eventConsumer.accept(event(
                    ResearchEventTypes.SUMMARY_DELTA, "summary", null, "综合中", "综合中"));
            eventConsumer.accept(event(
                    ResearchEventTypes.SUMMARY, "summary", null, result.getFinalSummary(), result));
            return result;
        });
        when(reportRenderer.renderFinal(result)).thenReturn("# AI分析报告");
        when(analysisRunDao.markSucceeded(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                eq("# AI分析报告"),
                anyLong()))
                .thenReturn(true);

        executor.executeSynchronously(lease);

        verify(datasetService).readEvidenceDatasets(JOB_ID);
        verify(datasetService, never()).listByJobId(JOB_ID);
        verify(workbookAssembler).assemble(
                JOB_ID,
                List.of(new EvidenceDatasetPayload("evidence.products", "{}")));
        verify(analysisRunDao).saveFinalSummary(
                ANALYSIS_RUN_ID,
                EXECUTION_OWNER,
                EXECUTION_TOKEN,
                "# AI分析报告");
        verify(artifactService, never()).publishMarkdown(any(), anyString());
        verify(conversationRecorder).complete(session, lease, "# AI分析报告");
        verify(analysisRunDao, org.mockito.Mockito.times(2)).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 1, 0);
        verify(analysisRunDao, org.mockito.Mockito.times(3)).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 0, 1);
        verify(analysisRunDao).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 0, 2);
        verify(analysisRunDao).markSucceeded(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                eq("# AI分析报告"),
                anyLong());
        verify(analysisRunDao, never()).markFailed(
                anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());
        verify(analysisRunDao, never()).markCancelled(anyString(), anyString(), anyString(), anyLong());
        verifyNoInteractions(heartbeatScheduler);

        List<ResearchEventCommand> events = publishedEvents(5);
        assertThat(events).extracting(ResearchEventCommand::getEventType)
                .containsExactly(
                        ResearchEventTypes.PLAN,
                        ResearchEventTypes.SUMMARY_DELTA,
                        ResearchEventTypes.SUMMARY,
                        ResearchEventTypes.DONE,
                        ResearchEventTypes.STAGE_COMPLETED);
        assertThat(events.get(2).getMessage()).isEqualTo("# AI分析报告");
        assertDoneStatus(events, ResearchAnalysisRunStatus.SUCCEEDED.name());
        assertThat(events).noneMatch(event -> event.getSheetName() != null);
        verify(agent, never()).run(
                anyString(),
                anyString(),
                any(ProductWorkbook.class),
                anyString(),
                org.mockito.ArgumentMatchers.<Consumer<AmazonSelectionReactEvent>>any(),
                any(CurationAnalysisBudget.class));
    }

    @Test
    void shouldFinalizeSavedFinalStageMarkdownWithoutRunningTheModelAgain() {
        persistedRun.setFinalSummary("已持久化的最终摘要");
        when(analysisRunDao.markSucceeded(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                eq("已持久化的最终摘要"),
                anyLong()))
                .thenReturn(true);

        executor.executeSynchronously(lease);

        verify(conversationRecorder).start(lease);
        verify(conversationRecorder).complete(session, lease, "已持久化的最终摘要");
        verify(analysisRunDao).markSucceeded(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                eq("已持久化的最终摘要"),
                anyLong());
        verify(analysisRunDao, never()).saveFinalSummary(
                anyString(), anyString(), anyString(), anyString());
        verify(datasetService, never()).readEvidenceDatasets(anyString());
        verifyNoInteractions(artifactService, workbookAssembler, agentProvider, agent, reportRenderer);

        List<ResearchEventCommand> events = publishedEvents(2);
        assertDoneStatus(events, ResearchAnalysisRunStatus.SUCCEEDED.name());
        assertThat(events).extracting(ResearchEventCommand::getEventType)
                .containsExactly(ResearchEventTypes.DONE, ResearchEventTypes.STAGE_COMPLETED);
    }

    @Test
    void shouldReadOnlyScreeningEvidenceAndNotPublishMarkdown() {
        lease = stageLease(ResearchAnalysisRunType.SCREENING);
        persistedRun.setRunType(ResearchAnalysisRunType.SCREENING.name());
        AmazonSelectionReactResult result = successfulResult();
        String normalizedSummary = """
                ## US

                - 市场存在进入机会。
                """;
        when(datasetService.readEvidenceDatasets(JOB_ID, EvidenceStage.SCREENING))
                .thenReturn(List.of(evidenceDataset));
        when(reportRenderer.renderScreeningSummary(result)).thenReturn(normalizedSummary);
        whenStageAgentRuns().thenAnswer(invocation -> {
            Consumer<AmazonSelectionReactEvent> eventConsumer = invocation.getArgument(4);
            eventConsumer.accept(event(
                    ResearchEventTypes.SHEET_THINK, "think", "US", "初筛分析完成", "screening-summary"));
            eventConsumer.accept(event(
                    ResearchEventTypes.SUMMARY, "summary", null, result.getFinalSummary(), result));
            return result;
        });
        when(analysisRunDao.markSucceeded(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                eq(normalizedSummary),
                anyLong()))
                .thenReturn(true);

        executor.executeSynchronously(lease);

        verify(datasetService).readEvidenceDatasets(JOB_ID, EvidenceStage.SCREENING);
        verify(datasetService, never()).readEvidenceDatasets(JOB_ID);
        verify(reportRenderer, org.mockito.Mockito.times(2)).renderScreeningSummary(result);
        verifyNoInteractions(artifactService);
        List<ResearchEventCommand> events = publishedEvents(4);
        assertThat(events).extracting(ResearchEventCommand::getEventType)
                .containsExactly(
                        ResearchEventTypes.SHEET_THINK,
                        ResearchEventTypes.SUMMARY,
                        ResearchEventTypes.DONE,
                        ResearchEventTypes.STAGE_COMPLETED);
        assertThat(events.get(1).getMessage()).isEqualTo(normalizedSummary.stripTrailing());
        Map<?, ?> sheetPayload = (Map<?, ?>) events.getFirst().getPayload();
        assertThat(sheetPayload.get("stageCode")).isEqualTo(ResearchStageCode.SCREENING.name());
        assertThat(sheetPayload.get("datasetCode")).isEqualTo("evidence.products");
    }

    @Test
    void shouldReadOnlyDeepDiveEvidenceAndNotPublishMarkdown() {
        lease = stageLease(ResearchAnalysisRunType.DEEP_DIVE);
        persistedRun.setRunType(ResearchAnalysisRunType.DEEP_DIVE.name());
        evidenceDataset = dataset("evidence.keywords");
        workbook = workbook("Keywords");
        AmazonSelectionReactResult result = successfulResult();
        result.setWorkbook(workbook);
        result.setSheetAnalyses(List.of(SheetAnalysisResult.builder()
                .sheetName("Keywords")
                .rowCount(1)
                .build()));
        when(datasetService.readEvidenceDatasets(JOB_ID, EvidenceStage.DEEP_DIVE))
                .thenReturn(List.of(evidenceDataset));
        when(workbookAssembler.assemble(eq(JOB_ID), any())).thenReturn(workbook);
        whenStageAgentRuns().thenReturn(result);
        when(analysisRunDao.markSucceeded(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                eq(result.getFinalSummary()),
                anyLong()))
                .thenReturn(true);

        executor.executeSynchronously(lease);

        verify(datasetService).readEvidenceDatasets(JOB_ID, EvidenceStage.DEEP_DIVE);
        verify(datasetService, never()).readEvidenceDatasets(JOB_ID);
        verify(workbookAssembler).assemble(
                JOB_ID,
                List.of(new EvidenceDatasetPayload("evidence.keywords", "{}")));
        verifyNoInteractions(reportRenderer, artifactService);
        List<ResearchEventCommand> events = publishedEvents(2);
        assertThat(events).extracting(ResearchEventCommand::getEventType)
                .containsExactly(ResearchEventTypes.DONE, ResearchEventTypes.STAGE_COMPLETED);
        Map<?, ?> stagePayload = (Map<?, ?>) events.getLast().getPayload();
        assertThat(stagePayload.get("stageCode")).isEqualTo(ResearchStageCode.DEEP_DIVE.name());
    }

    @Test
    void shouldPersistModelFailureWithoutDuplicatingAgentErrorEvent() {
        AmazonSelectionAnalysisException modelFailure = new AmazonSelectionAnalysisException(
                AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED,
                "模型请求失败");
        whenFinalAgentRuns().thenAnswer(invocation -> {
            Consumer<AmazonSelectionReactEvent> eventConsumer = invocation.getArgument(4);
            CurationAnalysisBudget budget = invocation.getArgument(5);
            budget.beforeModelCall();
            eventConsumer.accept(event(
                    ResearchEventTypes.ERROR,
                    "error",
                    null,
                    modelFailure.getMessage(),
                    Map.of("errorCode", modelFailure.getErrorCode().name())));
            throw modelFailure;
        });
        when(analysisRunDao.markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED.name()),
                eq("模型请求失败")))
                .thenReturn(true);

        assertThatThrownBy(() -> executor.executeSynchronously(lease))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("阶段分析失败");

        verify(conversationRecorder).fail(session, modelFailure);
        verify(analysisRunDao).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 1, 0);
        verify(analysisRunDao, org.mockito.Mockito.times(2)).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 0, 1);
        verify(analysisRunDao).markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED.name()),
                eq("模型请求失败"));
        verify(analysisRunDao, never()).markSucceeded(
                anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(analysisRunDao, never()).markCancelled(anyString(), anyString(), anyString(), anyLong());
        verifyNoInteractions(reportRenderer, artifactService);

        List<ResearchEventCommand> events = publishedEvents(2);
        assertThat(events).extracting(ResearchEventCommand::getEventType)
                .containsExactly(
                        ResearchEventTypes.ERROR,
                        ResearchEventTypes.DONE);
        assertThat(events).filteredOn(event -> ResearchEventTypes.ERROR.equals(event.getEventType()))
                .hasSize(1);
        assertDoneStatus(events, ResearchAnalysisRunStatus.FAILED.name());
    }

    @Test
    void shouldRecognizeCancellationWrappedByAgentAndPersistCancelledState() {
        whenFinalAgentRuns().thenAnswer(invocation -> {
            persistedRun.setCancelRequestedAt(System.currentTimeMillis());
            Consumer<AmazonSelectionReactEvent> eventConsumer = invocation.getArgument(4);
            try {
                eventConsumer.accept(event(
                        ResearchEventTypes.PLAN, "plan", null, "开始分析", ANALYSIS_GOAL));
                throw new AssertionError("取消请求应阻止事件写入");
            } catch (ResearchAnalysisCancelledException cancellation) {
                throw new AmazonSelectionAnalysisException(
                        AmazonSelectionAnalysisException.ErrorCode.AGENT_EXECUTION_FAILED,
                        "Agent事件回调失败",
                        cancellation);
            }
        });
        when(analysisRunDao.markCancelled(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> executor.executeSynchronously(lease))
                .isInstanceOf(ResearchAnalysisCancelledException.class);

        verify(conversationRecorder).cancel(session);
        verify(conversationRecorder, never()).fail(any(), any());
        verify(analysisRunDao).markCancelled(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong());
        verify(analysisRunDao, never()).markFailed(
                anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());
        verify(analysisRunDao, never()).markSucceeded(
                anyString(), anyString(), anyString(), anyString(), anyLong());
        verifyNoInteractions(reportRenderer, artifactService);

        List<ResearchEventCommand> events = publishedEvents(1);
        assertThat(events).extracting(ResearchEventCommand::getEventType)
                .containsExactly(ResearchEventTypes.DONE)
                .doesNotContain(ResearchEventTypes.ERROR);
        assertDoneStatus(events, ResearchAnalysisRunStatus.CANCELLED.name());
    }

    @Test
    void shouldStopAllWritesAfterHeartbeatDetectsLeaseLoss() {
        lease = followUpLease();
        persistedRun.setRunType(ResearchAnalysisRunType.FOLLOW_UP.name());
        AmazonSelectionReactResult result = successfulResult();
        when(analysisRunDao.heartbeat(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                anyLong()))
                .thenReturn(false);
        whenStageAgentRuns().thenAnswer(invocation -> {
            heartbeatTask.get().run();
            return result;
        });

        executor.submit(lease);

        verify(analysisRunDao).heartbeat(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                anyLong());
        verify(analysisRunDao, never()).markSucceeded(
                anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(analysisRunDao, never()).markFailed(
                anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());
        verify(analysisRunDao, never()).markCancelled(anyString(), anyString(), anyString(), anyLong());
        verify(conversationRecorder, never()).complete(any(), any(), anyString());
        verify(conversationRecorder, never()).fail(any(), any());
        verifyNoInteractions(reportRenderer, artifactService, eventPublisher);
        verify(heartbeatFuture).cancel(false);
    }

    @Test
    void shouldRejectWorkbookBeforeStartingAgentWhenSheetLimitIsExceeded() {
        lease = leaseWithMaxAttempts(3);
        analysisProperties.setMaxSheets(1);
        ProductSheet secondSheet = new ProductSheet();
        secondSheet.setSheetName("UK");
        workbook.getSheets().add(secondSheet);
        when(analysisRunDao.markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.SHEET_LIMIT_EXCEEDED.name()),
                contains("Sheet 数 2")))
                .thenReturn(true);

        executor.submit(lease);

        verifyNoInteractions(agent, reportRenderer, artifactService, conversationRecorder);
        verify(analysisRunDao, org.mockito.Mockito.times(2)).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 0, 1);
        verify(analysisRunDao).markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.SHEET_LIMIT_EXCEEDED.name()),
                contains("Sheet 数 2"));
        verify(analysisRunDao, never()).markRetryWait(
                anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());

        List<ResearchEventCommand> events = publishedEvents(2);
        assertThat(events.getFirst().getEventType()).isEqualTo(ResearchEventTypes.ERROR);
        assertThat(events.getFirst().getPayload()).isEqualTo(Map.of(
                "stageCode", ResearchStageCode.FINAL_ANALYSIS.name(),
                "errorCode", AmazonSelectionAnalysisException.ErrorCode.SHEET_LIMIT_EXCEEDED.name()));
        assertDoneStatus(events, ResearchAnalysisRunStatus.FAILED.name());
    }

    @Test
    void shouldPersistEachModelCallBeforeRejectingModelCallLimit() {
        lease = leaseWithMaxAttempts(3);
        analysisProperties.setMaxModelCalls(1);
        whenFinalAgentRuns().thenAnswer(invocation -> {
            CurationAnalysisBudget budget = invocation.getArgument(5);
            budget.beforeModelCall();
            budget.beforeModelCall();
            throw new AssertionError("第二次模型调用应被预算拒绝");
        });
        when(analysisRunDao.markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.MODEL_CALL_LIMIT_EXCEEDED.name()),
                contains("模型调用次数")))
                .thenReturn(true);

        executor.submit(lease);

        verify(analysisRunDao).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 1, 0);
        verify(analysisRunDao, org.mockito.Mockito.times(2)).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 0, 1);
        verify(analysisRunDao).markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.MODEL_CALL_LIMIT_EXCEEDED.name()),
                contains("模型调用次数"));
        verify(analysisRunDao, never()).markRetryWait(
                anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());
        verifyNoInteractions(reportRenderer, artifactService);

        List<ResearchEventCommand> events = publishedEvents(2);
        assertThat(events.getFirst().getPayload()).isEqualTo(Map.of(
                "stageCode", ResearchStageCode.FINAL_ANALYSIS.name(),
                "errorCode", AmazonSelectionAnalysisException.ErrorCode.MODEL_CALL_LIMIT_EXCEEDED.name()));
        assertDoneStatus(events, ResearchAnalysisRunStatus.FAILED.name());
    }

    @Test
    void shouldFailWithExplicitDurationCodeAtEventPersistenceBoundary() {
        lease = leaseWithMaxAttempts(3);
        analysisProperties.setMaxExecutionDurationMs(1_000L);
        whenFinalAgentRuns().thenAnswer(invocation -> {
            nanoTime.set(TimeUnit.MILLISECONDS.toNanos(1_000L));
            Consumer<AmazonSelectionReactEvent> eventConsumer = invocation.getArgument(4);
            eventConsumer.accept(event(ResearchEventTypes.PLAN, "plan", null, "开始分析", ANALYSIS_GOAL));
            throw new AssertionError("超时事件不应完成持久化");
        });
        when(analysisRunDao.markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.EXECUTION_DURATION_EXCEEDED.name()),
                contains("执行时长")))
                .thenReturn(true);

        executor.submit(lease);

        verify(analysisRunDao, never()).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 1, 0);
        verify(analysisRunDao, org.mockito.Mockito.times(2)).incrementCounters(
                ANALYSIS_RUN_ID, EXECUTION_OWNER, EXECUTION_TOKEN, 0, 1);
        verify(analysisRunDao).markFailed(
                eq(ANALYSIS_RUN_ID),
                eq(EXECUTION_OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(AmazonSelectionAnalysisException.ErrorCode.EXECUTION_DURATION_EXCEEDED.name()),
                contains("执行时长"));
        verify(analysisRunDao, never()).markRetryWait(
                anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());
        verifyNoInteractions(reportRenderer, artifactService);

        List<ResearchEventCommand> events = publishedEvents(2);
        assertThat(events.getFirst().getPayload()).isEqualTo(Map.of(
                "stageCode", ResearchStageCode.FINAL_ANALYSIS.name(),
                "errorCode", AmazonSelectionAnalysisException.ErrorCode.EXECUTION_DURATION_EXCEEDED.name()));
        assertDoneStatus(events, ResearchAnalysisRunStatus.FAILED.name());
    }

    private org.mockito.stubbing.OngoingStubbing<AmazonSelectionReactResult> whenStageAgentRuns() {
        return when(agent.run(
                eq(ANALYSIS_RUN_ID),
                eq(CONVERSATION_ID),
                same(workbook),
                eq(ANALYSIS_GOAL),
                org.mockito.ArgumentMatchers.<Consumer<AmazonSelectionReactEvent>>any(),
                any(CurationAnalysisBudget.class)));
    }

    private org.mockito.stubbing.OngoingStubbing<AmazonSelectionReactResult> whenFinalAgentRuns() {
        return when(agent.runFinalAnalysis(
                eq(ANALYSIS_RUN_ID),
                eq(CONVERSATION_ID),
                same(workbook),
                eq(ANALYSIS_GOAL),
                org.mockito.ArgumentMatchers.<Consumer<AmazonSelectionReactEvent>>any(),
                any(CurationAnalysisBudget.class)));
    }

    private List<ResearchEventCommand> publishedEvents(int expectedCount) {
        ArgumentCaptor<ResearchEventCommand> commandCaptor = ArgumentCaptor.forClass(ResearchEventCommand.class);
        verify(eventPublisher, org.mockito.Mockito.times(expectedCount)).publish(commandCaptor.capture());
        return commandCaptor.getAllValues();
    }

    private void assertDoneStatus(List<ResearchEventCommand> events, String expectedStatus) {
        assertThat(events).filteredOn(event -> ResearchEventTypes.DONE.equals(event.getEventType()))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getPayload()).isInstanceOf(Map.class);
                    Map<?, ?> payload = (Map<?, ?>) event.getPayload();
                    assertThat(payload.get("analysisStatus")).isEqualTo(expectedStatus);
                    assertThat(payload.get("stageCode")).isEqualTo(ResearchStageCode.FINAL_ANALYSIS.name());
                    assertThat(event.isTerminal()).isFalse();
                });
    }

    private MarketResearchAnalysisRun runningRun() {
        MarketResearchAnalysisRun run = new MarketResearchAnalysisRun();
        run.setAnalysisRunId(ANALYSIS_RUN_ID);
        run.setJobId(JOB_ID);
        run.setUserId(USER_ID);
        run.setConversationId(CONVERSATION_ID);
        run.setRunType(ResearchAnalysisRunType.FINAL_ANALYSIS.name());
        run.setAnalysisGoal(ANALYSIS_GOAL);
        run.setRunStatus(ResearchAnalysisRunStatus.RUNNING.name());
        run.setExecutionOwner(EXECUTION_OWNER);
        run.setExecutionToken(EXECUTION_TOKEN);
        return run;
    }

    private ResearchAnalysisLease leaseWithMaxAttempts(int maxAttempts) {
        return new ResearchAnalysisLease(
                ANALYSIS_RUN_ID,
                JOB_ID,
                USER_ID,
                CONVERSATION_ID,
                ResearchAnalysisRunType.FINAL_ANALYSIS.name(),
                ANALYSIS_GOAL,
                EXECUTION_OWNER,
                EXECUTION_TOKEN,
                1,
                maxAttempts);
    }

    private ResearchAnalysisLease followUpLease() {
        return new ResearchAnalysisLease(
                ANALYSIS_RUN_ID,
                JOB_ID,
                USER_ID,
                CONVERSATION_ID,
                ResearchAnalysisRunType.FOLLOW_UP.name(),
                ANALYSIS_GOAL,
                EXECUTION_OWNER,
                EXECUTION_TOKEN,
                1,
                3);
    }

    private ResearchAnalysisLease stageLease(ResearchAnalysisRunType runType) {
        return new ResearchAnalysisLease(
                ANALYSIS_RUN_ID,
                JOB_ID,
                USER_ID,
                CONVERSATION_ID,
                runType.name(),
                ANALYSIS_GOAL,
                EXECUTION_OWNER,
                EXECUTION_TOKEN,
                1,
                3);
    }

    private ResearchDataset dataset() {
        return dataset("evidence.products");
    }

    private ResearchDataset dataset(String datasetCode) {
        return new ResearchDataset(
                datasetCode,
                "PREPARE_EVIDENCE",
                new tools.jackson.databind.ObjectMapper().createObjectNode(),
                0);
    }

    private ProductWorkbook workbook() {
        return workbook("US");
    }

    private ProductWorkbook workbook(String sheetName) {
        ProductWorkbook value = new ProductWorkbook();
        value.setFileName("market-research-job-1.xlsx");
        ProductSheet sheet = new ProductSheet();
        sheet.setSheetName(sheetName);
        value.getSheets().add(sheet);
        return value;
    }

    private AmazonSelectionReactResult successfulResult() {
        AmazonSelectionReactResult result = new AmazonSelectionReactResult();
        result.setConversationId(CONVERSATION_ID);
        result.setWorkbook(workbook);
        result.setModelInvoked(true);
        result.setFinalSummary("市场具备机会，但需要控制竞争风险。");
        result.setSheetAnalyses(List.of(SheetAnalysisResult.builder()
                .sheetName("US")
                .rowCount(1)
                .build()));
        return result;
    }

    private AmazonSelectionReactEvent event(
            String eventType,
            String phase,
            String sheetName,
            String message,
            Object data) {
        return AmazonSelectionReactEvent.builder()
                .eventType(eventType)
                .conversationId(CONVERSATION_ID)
                .stepIndex(1)
                .phase(phase)
                .sheetName(sheetName)
                .message(message)
                .data(data)
                .build();
    }
}
