package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionManus;
import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.ResearchStageAgentRouter;
import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import cyou.yuanbaomao.sellersprite.ai.research.curation.evidence.EvidenceDatasetPayload;
import cyou.yuanbaomao.sellersprite.ai.research.curation.evidence.EvidenceWorkbookAssembler;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.report.AmazonSelectionMarkdownReportRenderer;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchAnalysisCancelledException;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisArtifactService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAgentTaskContextService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSseEventPublisher;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/** 组装持久化 evidence、运行 Curation Agent、保存事件/会话/报告并写入独立终态。 */
@Slf4j
@Component
public class ResearchAnalysisExecutor {

    private static final int PLAN_PROGRESS = 5;
    private static final int WORKBOOK_PROGRESS = 10;
    private static final int SUMMARY_PREPARE_PROGRESS = 85;
    private static final int SUMMARY_PROGRESS = 95;
    private static final int COMPLETION_EVENT_COUNT = 1;
    private static final int STAGE_SUCCESS_COMPLETION_EVENT_COUNT = 2;

    private final Executor taskExecutor;
    private final TaskScheduler heartbeatScheduler;
    private final ResearchAnalysisStateService stateService;
    private final ResearchDatasetService datasetService;
    private final EvidenceWorkbookAssembler workbookAssembler;
    private final java.util.function.Function<ResearchAnalysisRunType, AmazonSelectionManus> agentResolver;
    private final BiFunction<String, ResearchStageCode, String> taskContextProvider;
    private final boolean rawDataScopeEnabled;
    private final AmazonSelectionMarkdownReportRenderer reportRenderer;
    private final ResearchAnalysisArtifactService artifactService;
    private final ResearchAnalysisConversationRecorder conversationRecorder;
    private final ResearchSseEventPublisher eventPublisher;
    private final CurationAnalysisProperties properties;
    private final LongSupplier nanoTime;

    @Autowired
    public ResearchAnalysisExecutor(
            @Qualifier(ResearchAnalysisRuntimeConfiguration.EXECUTOR_BEAN_NAME)
                    Executor taskExecutor,
            @Qualifier(ResearchAnalysisRuntimeConfiguration.HEARTBEAT_SCHEDULER_BEAN_NAME)
                    TaskScheduler heartbeatScheduler,
            ResearchAnalysisStateService stateService,
            ResearchDatasetService datasetService,
            EvidenceWorkbookAssembler workbookAssembler,
            ResearchStageAgentRouter agentRouter,
            ResearchAgentTaskContextService taskContextService,
            AmazonSelectionMarkdownReportRenderer reportRenderer,
            ResearchAnalysisArtifactService artifactService,
            ResearchAnalysisConversationRecorder conversationRecorder,
            ResearchSseEventPublisher eventPublisher,
            CurationAnalysisProperties properties) {
        this(taskExecutor, heartbeatScheduler, stateService, datasetService, workbookAssembler, agentRouter,
                taskContextService,
                reportRenderer, artifactService, conversationRecorder, eventPublisher,
                properties, System::nanoTime);
    }

    ResearchAnalysisExecutor(
            Executor taskExecutor,
            TaskScheduler heartbeatScheduler,
            ResearchAnalysisStateService stateService,
            ResearchDatasetService datasetService,
            EvidenceWorkbookAssembler workbookAssembler,
            ResearchStageAgentRouter agentRouter,
            ResearchAgentTaskContextService taskContextService,
            AmazonSelectionMarkdownReportRenderer reportRenderer,
            ResearchAnalysisArtifactService artifactService,
            ResearchAnalysisConversationRecorder conversationRecorder,
            ResearchSseEventPublisher eventPublisher,
            CurationAnalysisProperties properties,
            LongSupplier nanoTime) {
        this.taskExecutor = taskExecutor;
        this.heartbeatScheduler = heartbeatScheduler;
        this.stateService = stateService;
        this.datasetService = datasetService;
        this.workbookAssembler = workbookAssembler;
        this.agentResolver = agentRouter::getAgent;
        this.taskContextProvider = taskContextService::describe;
        this.rawDataScopeEnabled = true;
        this.reportRenderer = reportRenderer;
        this.artifactService = artifactService;
        this.conversationRecorder = conversationRecorder;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.nanoTime = nanoTime;
    }

    /** 仅保留给现有单元测试的直接 Agent 注入构造器；生产入口使用 ResearchStageAgentRouter。 */
    ResearchAnalysisExecutor(
            Executor taskExecutor,
            TaskScheduler heartbeatScheduler,
            ResearchAnalysisStateService stateService,
            ResearchDatasetService datasetService,
            EvidenceWorkbookAssembler workbookAssembler,
            org.springframework.beans.factory.ObjectProvider<AmazonSelectionManus> agentProvider,
            AmazonSelectionMarkdownReportRenderer reportRenderer,
            ResearchAnalysisArtifactService artifactService,
            ResearchAnalysisConversationRecorder conversationRecorder,
            ResearchSseEventPublisher eventPublisher,
            CurationAnalysisProperties properties,
            LongSupplier nanoTime) {
        this.taskExecutor = taskExecutor;
        this.heartbeatScheduler = heartbeatScheduler;
        this.stateService = stateService;
        this.datasetService = datasetService;
        this.workbookAssembler = workbookAssembler;
        this.agentResolver = ignored -> agentProvider.getObject();
        this.taskContextProvider = (ignoredJobId, ignoredStageCode) -> "";
        this.rawDataScopeEnabled = false;
        this.reportRenderer = reportRenderer;
        this.artifactService = artifactService;
        this.conversationRecorder = conversationRecorder;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.nanoTime = nanoTime;
    }

    public void submit(ResearchAnalysisLease lease) {
        taskExecutor.execute(() -> execute(lease, false));
    }

    public void executeSynchronously(ResearchAnalysisLease lease) {
        execute(lease, true);
    }

    private void execute(ResearchAnalysisLease lease, boolean propagateFailure) {
        AtomicBoolean leaseLost = new AtomicBoolean(false);
        ScheduledFuture<?> heartbeat = startHeartbeat(lease, leaseLost);
        ResearchAnalysisConversationRecorder.Session session = null;
        AtomicBoolean agentErrorPersisted = new AtomicBoolean(false);
        try {
            assertLeaseExecutable(lease);
            Optional<String> savedFinalSummary = stateService.findSavedFinalSummary(
                    lease.analysisRunId());
            if (savedFinalSummary.isPresent()
                    && (!publishesArtifact(lease)
                    || artifactService.hasPublishedMarkdown(lease.analysisRunId()))) {
                session = conversationRecorder.start(lease);
                conversationRecorder.complete(session, lease, savedFinalSummary.orElseThrow());
                reserveSuccessfulCompletionEvents(lease);
                stateService.markSucceeded(lease, savedFinalSummary.orElseThrow());
                log.info("复用已持久化的市场调研阶段摘要完成终态，analysisRunId={}，stageCode={}",
                        lease.analysisRunId(), stageCode(lease).name());
                return;
            }
            CurationAnalysisBudget budget = new CurationAnalysisBudget(
                    properties.getMaxSheets(),
                    properties.getMaxModelCalls(),
                    properties.getMaxExecutionDurationMs(),
                    () -> recordModelCall(lease),
                    nanoTime);
            updateStateProgress(lease, "assemble_workbook", WORKBOOK_PROGRESS);
            ProductWorkbook workbook = workbookAssembler.assemble(
                    lease.jobId(), evidencePayloads(lease));
            budget.assertSheetCount(workbook.getSheets().size());
            assertLeaseExecutable(lease);
            session = conversationRecorder.start(lease);
            ResearchAnalysisRunType currentRunType = runType(lease);
            AmazonSelectionManus agent = agentResolver.apply(currentRunType);
            String agentAnalysisContext = agentAnalysisContext(lease);
            int totalSheets = workbook.getSheets().size();
            AtomicInteger completedSheets = new AtomicInteger();
            Consumer<AmazonSelectionReactEvent> eventConsumer = event ->
                    persistAgentEvent(
                            lease,
                            event,
                            leaseLost,
                            totalSheets,
                            completedSheets,
                            agentErrorPersisted,
                            budget);
            ResearchRawDataAccessScope rawDataScope = rawDataScopeEnabled
                    ? new ResearchRawDataAccessScope(lease.jobId(), stageCode(lease))
                    : null;
            boolean finalAnalysisPath = rawDataScopeEnabled
                    ? currentRunType != ResearchAnalysisRunType.SCREENING
                            && currentRunType != ResearchAnalysisRunType.DEEP_DIVE
                    : currentRunType == ResearchAnalysisRunType.FINAL_ANALYSIS;
            AmazonSelectionReactResult result;
            if (finalAnalysisPath) {
                result = rawDataScopeEnabled
                        ? agent.runFinalAnalysis(
                                lease.analysisRunId(), lease.conversationId(), workbook, agentAnalysisContext,
                                eventConsumer, budget, rawDataScope)
                        : agent.runFinalAnalysis(
                                lease.analysisRunId(), lease.conversationId(), workbook, agentAnalysisContext,
                                eventConsumer, budget);
            } else {
                result = rawDataScopeEnabled
                        ? agent.run(
                                lease.analysisRunId(), lease.conversationId(), workbook, agentAnalysisContext,
                                eventConsumer, budget, rawDataScope)
                        : agent.run(
                                lease.analysisRunId(), lease.conversationId(), workbook, agentAnalysisContext,
                                eventConsumer, budget);
            }
            if (leaseLost.get()) {
                throw new AnalysisLeaseLostException(lease.analysisRunId());
            }
            assertLeaseExecutable(lease);
            budget.assertExecutionDuration();
            String markdown;
            try {
                markdown = rendersMarkdown(lease)
                        ? currentRunType == ResearchAnalysisRunType.FINAL_ANALYSIS
                                ? reportRenderer.renderFinal(result)
                                : reportRenderer.render(result)
                        : null;
            } catch (IllegalStateException exception) {
                if (currentRunType == ResearchAnalysisRunType.FINAL_ANALYSIS) {
                    throw new AmazonSelectionAnalysisException(
                            AmazonSelectionAnalysisException.ErrorCode.REPORT_STRUCTURE_INVALID,
                            exception.getMessage(),
                            exception);
                }
                throw exception;
            }
            String persistedSummary = switch (currentRunType) {
                case FINAL_ANALYSIS -> markdown;
                case SCREENING -> reportRenderer.renderScreeningSummary(result);
                default -> result.getFinalSummary();
            };
            stateService.saveFinalSummary(lease, persistedSummary);
            if (publishesArtifact(lease)) {
                budget.assertExecutionDuration();
                artifactService.publishMarkdown(lease, markdown);
            }
            conversationRecorder.complete(session, lease, persistedSummary);
            budget.assertExecutionDuration();
            reserveSuccessfulCompletionEvents(lease);
            stateService.markSucceeded(lease, persistedSummary);
            log.info("市场调研Curation阶段分析完成，analysisRunId={}，stageCode={}，sheets={}",
                    lease.analysisRunId(), stageCode(lease).name(), result.getSheetAnalyses().size());
        } catch (ResearchAnalysisCancelledException exception) {
            handleCancellation(session, lease);
            if (propagateFailure) {
                throw exception;
            }
        } catch (Exception exception) {
            if (hasCause(exception, ResearchAnalysisCancelledException.class)) {
                handleCancellation(session, lease);
                if (propagateFailure) {
                    throw new ResearchAnalysisCancelledException(lease.analysisRunId());
                }
                return;
            }
            if (leaseLost.get() || hasCause(exception, AnalysisLeaseLostException.class)) {
                log.warn("市场调研分析执行租约已丢失，停止旧执行器写入，analysisRunId={}",
                        lease.analysisRunId());
                if (propagateFailure) {
                    throw new AnalysisLeaseLostException(lease.analysisRunId(), exception);
                }
                return;
            }
            if (session != null) {
                conversationRecorder.fail(session, exception);
            }
            if (!(exception instanceof AmazonSelectionAnalysisException) || !agentErrorPersisted.get()) {
                publishExecutorError(lease, exception);
            }
            boolean retryable = !isParentManaged(lease) && isRetryable(exception);
            reserveCompletionEvent(lease);
            stateService.handleFailure(
                    lease,
                    errorCode(exception),
                    safeMessage(exception),
                    retryable);
            log.error("市场调研Curation分析失败，analysisRunId={}", lease.analysisRunId(), exception);
            if (propagateFailure) {
                throw new IllegalStateException("市场调研阶段分析失败", exception);
            }
        } finally {
            if (heartbeat != null) {
                heartbeat.cancel(false);
            }
        }
    }

    private String agentAnalysisContext(ResearchAnalysisLease lease) {
        String taskContext = taskContextProvider.apply(lease.jobId(), stageCode(lease));
        if (taskContext == null || taskContext.isBlank()) {
            return lease.analysisGoal();
        }
        String analysisGoal = lease.analysisGoal() == null || lease.analysisGoal().isBlank()
                ? "用户没有补充具体问题，请完成通用亚马逊选品数据分析。"
                : lease.analysisGoal().trim();
        return "【用户分析目标】\n" + analysisGoal + "\n\n" + taskContext.trim();
    }

    private ScheduledFuture<?> startHeartbeat(
            ResearchAnalysisLease lease, AtomicBoolean leaseLost) {
        if (isParentManaged(lease)) {
            return null;
        }
        return heartbeatScheduler.scheduleAtFixedRate(
                () -> refreshLease(lease, leaseLost),
                Duration.ofMillis(properties.getHeartbeatIntervalMs()));
    }

    private boolean isParentManaged(ResearchAnalysisLease lease) {
        ResearchAnalysisRunType runType = runType(lease);
        return runType == ResearchAnalysisRunType.INITIAL
                || runType == ResearchAnalysisRunType.SCREENING
                || runType == ResearchAnalysisRunType.DEEP_DIVE
                || runType == ResearchAnalysisRunType.FINAL_ANALYSIS;
    }

    private List<EvidenceDatasetPayload> evidencePayloads(ResearchAnalysisLease lease) {
        List<ResearchDataset> datasets = switch (runType(lease)) {
            case SCREENING -> datasetService.readEvidenceDatasets(
                    lease.jobId(), EvidenceStage.SCREENING);
            case DEEP_DIVE -> datasetService.readEvidenceDatasets(
                    lease.jobId(), EvidenceStage.DEEP_DIVE);
            default -> datasetService.readEvidenceDatasets(lease.jobId());
        };
        return datasets.stream()
                .map(this::toEvidencePayload)
                .toList();
    }

    private EvidenceDatasetPayload toEvidencePayload(ResearchDataset dataset) {
        return new EvidenceDatasetPayload(dataset.getDatasetCode(), dataset.getPayload().toString());
    }

    private void persistAgentEvent(
            ResearchAnalysisLease lease,
            AmazonSelectionReactEvent event,
            AtomicBoolean leaseLost,
            int totalSheets,
            AtomicInteger completedSheets,
            AtomicBoolean agentErrorPersisted,
            CurationAnalysisBudget budget) {
        if (leaseLost.get()) {
            throw new AnalysisLeaseLostException(lease.analysisRunId());
        }
        budget.beforeEventPersistence();
        assertLeaseExecutable(lease);
        updateProgress(lease, event, totalSheets, completedSheets);
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .conversationId(lease.conversationId())
                .analysisRunId(lease.analysisRunId())
                .scope(ResearchEventScope.ANALYSIS)
                .eventType(event.getEventType())
                .phase(event.getPhase())
                .sheetName(event.getSheetName())
                .message(agentEventMessage(lease, event))
                .payload(eventPayload(lease, event))
                .build());
        incrementRunCounters(lease, 0, 1);
        if (ResearchEventTypes.ERROR.equals(event.getEventType())) {
            agentErrorPersisted.set(true);
        }
    }

    private String agentEventMessage(ResearchAnalysisLease lease, AmazonSelectionReactEvent event) {
        if (ResearchEventTypes.SUMMARY.equals(event.getEventType())
                && event.getData() instanceof AmazonSelectionReactResult result) {
            return switch (runType(lease)) {
                case SCREENING -> reportRenderer.renderScreeningSummary(result).stripTrailing();
                case FINAL_ANALYSIS -> reportRenderer.renderFinal(result).stripTrailing();
                default -> event.getMessage() == null ? "" : event.getMessage();
            };
        }
        return event.getMessage() == null ? "" : event.getMessage();
    }

    private Map<String, Object> eventPayload(
            ResearchAnalysisLease lease, AmazonSelectionReactEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stageCode", stageCode(lease).name());
        datasetCode(event.getSheetName()).ifPresent(
                datasetCode -> payload.put("datasetCode", datasetCode));
        payload.put("stepIndex", event.getStepIndex());
        Object data = event.getData();
        if (data == null
                || data instanceof CharSequence
                || data instanceof Number
                || data instanceof Boolean
                || data instanceof Map<?, ?>
                || data instanceof List<?>) {
            payload.put("data", data);
        } else if (data instanceof AmazonSelectionReactResult result) {
            payload.put("data", Map.of(
                    "modelInvoked", result.isModelInvoked(),
                    "sheetCount", result.getSheetAnalyses().size()));
        } else {
            payload.put("data", String.valueOf(data));
        }
        return payload;
    }

    private void updateProgress(
            ResearchAnalysisLease lease,
            AmazonSelectionReactEvent event,
            int totalSheets,
            AtomicInteger completedSheets) {
        switch (event.getEventType()) {
            case ResearchEventTypes.PLAN ->
                    updateStateProgress(lease, event.getPhase(), PLAN_PROGRESS);
            case ResearchEventTypes.WORKBOOK ->
                    updateStateProgress(lease, event.getPhase(), WORKBOOK_PROGRESS);
            case ResearchEventTypes.SHEET_THINK -> {
                int completed = completedSheets.incrementAndGet();
                int progress = WORKBOOK_PROGRESS
                        + (int) Math.round(70D * completed / Math.max(1, totalSheets));
                updateStateProgress(lease, event.getPhase(), progress);
            }
            case ResearchEventTypes.SUMMARY_PREPARE ->
                    updateStateProgress(lease, event.getPhase(), SUMMARY_PREPARE_PROGRESS);
            case ResearchEventTypes.SUMMARY ->
                    updateStateProgress(lease, event.getPhase(), SUMMARY_PROGRESS);
            default -> {
                // 高频 delta 只持久化事件，不额外写运行进度。
            }
        }
    }

    private void publishExecutorError(ResearchAnalysisLease lease, Exception exception) {
        try {
            assertLeaseExecutable(lease);
            eventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(lease.jobId())
                    .conversationId(lease.conversationId())
                    .analysisRunId(lease.analysisRunId())
                    .scope(ResearchEventScope.ANALYSIS)
                    .eventType(ResearchEventTypes.ERROR)
                    .phase("error")
                    .message(safeMessage(exception))
                    .payload(Map.of(
                            "stageCode", stageCode(lease).name(),
                            "errorCode", errorCode(exception)))
                    .build());
            incrementRunCounters(lease, 0, 1);
        } catch (RuntimeException publishException) {
            log.warn("持久化市场调研分析失败事件异常，analysisRunId={}",
                    lease.analysisRunId(), publishException);
        }
    }

    private void refreshLease(ResearchAnalysisLease lease, AtomicBoolean leaseLost) {
        try {
            if (!stateService.heartbeat(lease, properties.getLeaseDurationMs())) {
                leaseLost.set(true);
                log.warn("市场调研分析心跳失败，analysisRunId={}", lease.analysisRunId());
            }
        } catch (RuntimeException exception) {
            leaseLost.set(true);
            log.error("市场调研分析心跳异常，analysisRunId={}", lease.analysisRunId(), exception);
        }
    }

    private void assertLeaseExecutable(ResearchAnalysisLease lease) {
        try {
            stateService.assertExecutable(lease);
        } catch (ResearchAnalysisCancelledException exception) {
            throw exception;
        } catch (IllegalStateException exception) {
            throw new AnalysisLeaseLostException(lease.analysisRunId(), exception);
        }
    }

    private void updateStateProgress(ResearchAnalysisLease lease, String phase, int progress) {
        try {
            stateService.updateProgress(lease, phase, progress);
        } catch (IllegalStateException exception) {
            throw new AnalysisLeaseLostException(lease.analysisRunId(), exception);
        }
    }

    private void recordModelCall(ResearchAnalysisLease lease) {
        assertLeaseExecutable(lease);
        incrementRunCounters(lease, 1, 0);
    }

    private void incrementRunCounters(
            ResearchAnalysisLease lease, int modelCallIncrement, int eventIncrement) {
        try {
            stateService.incrementCounters(lease, modelCallIncrement, eventIncrement);
        } catch (IllegalStateException exception) {
            throw new AnalysisLeaseLostException(lease.analysisRunId(), exception);
        }
    }

    private void reserveCompletionEvent(ResearchAnalysisLease lease) {
        incrementRunCounters(lease, 0, COMPLETION_EVENT_COUNT);
    }

    private void reserveSuccessfulCompletionEvents(ResearchAnalysisLease lease) {
        int eventCount = isStageRun(lease)
                ? STAGE_SUCCESS_COMPLETION_EVENT_COUNT
                : COMPLETION_EVENT_COUNT;
        incrementRunCounters(lease, 0, eventCount);
    }

    private boolean rendersMarkdown(ResearchAnalysisLease lease) {
        return runType(lease) != ResearchAnalysisRunType.SCREENING
                && runType(lease) != ResearchAnalysisRunType.DEEP_DIVE;
    }

    private boolean publishesArtifact(ResearchAnalysisLease lease) {
        return !isStageRun(lease) && rendersMarkdown(lease);
    }

    private boolean isStageRun(ResearchAnalysisLease lease) {
        ResearchAnalysisRunType runType = runType(lease);
        return runType == ResearchAnalysisRunType.SCREENING
                || runType == ResearchAnalysisRunType.DEEP_DIVE
                || runType == ResearchAnalysisRunType.FINAL_ANALYSIS;
    }

    private ResearchAnalysisRunType runType(ResearchAnalysisLease lease) {
        return ResearchAnalysisRunType.valueOf(lease.runType());
    }

    private ResearchStageCode stageCode(ResearchAnalysisLease lease) {
        return switch (runType(lease)) {
            case SCREENING -> ResearchStageCode.SCREENING;
            case DEEP_DIVE -> ResearchStageCode.DEEP_DIVE;
            default -> ResearchStageCode.FINAL_ANALYSIS;
        };
    }

    private Optional<String> datasetCode(String sheetName) {
        if (sheetName == null || sheetName.isBlank()) {
            return Optional.empty();
        }
        return ResearchEvidenceCatalog.DEFINITIONS.stream()
                .filter(definition -> definition.sheetName().equals(sheetName))
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .findFirst();
    }

    private void handleCancellation(
            ResearchAnalysisConversationRecorder.Session session,
            ResearchAnalysisLease lease) {
        if (session != null) {
            conversationRecorder.cancel(session);
        }
        reserveCompletionEvent(lease);
        stateService.markCancelled(lease);
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> expectedType) {
        Throwable current = throwable;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String errorCode(Exception exception) {
        if (exception instanceof AmazonSelectionAnalysisException analysisException) {
            return analysisException.getErrorCode().name();
        }
        return "ANALYSIS_EXECUTION_FAILED";
    }

    private boolean isRetryable(Exception exception) {
        if (exception instanceof AmazonSelectionAnalysisException analysisException) {
            return analysisException.getErrorCode()
                    == AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED
                    || analysisException.getErrorCode()
                    == AmazonSelectionAnalysisException.ErrorCode.MODEL_EMPTY_RESPONSE;
        }
        return false;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }

    private static final class AnalysisLeaseLostException extends RuntimeException {

        private AnalysisLeaseLostException(String analysisRunId) {
            super("市场调研分析执行租约已丢失: " + analysisRunId);
        }

        private AnalysisLeaseLostException(String analysisRunId, Throwable cause) {
            super("市场调研分析执行租约已丢失: " + analysisRunId, cause);
        }
    }
}
