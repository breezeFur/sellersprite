package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchAnalysisServiceTest {

    private static final String USER_ID = "user-analysis-001";
    private static final String JOB_ID = "job-analysis-001";
    private static final String CONVERSATION_ID = "conversation-analysis-001";
    private static final String RUN_ID = "run-analysis-001";

    @Mock
    private MarketResearchJobDao jobDao;

    @Mock
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Mock
    private AiConversationDao conversationDao;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ResearchSseEventPublisher eventPublisher;

    private ResearchAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new ResearchAnalysisService(
                jobDao,
                analysisRunDao,
                conversationDao,
                idGenerator,
                new ResearchProperties(),
                eventPublisher);
        RequestContextHolder.set(RequestContext.builder()
                .userId(USER_ID)
                .username("analysis-user")
                .build());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldCreateConversationAndWaitingScreeningRun() {
        MarketResearchJob job = job(ResearchJobStatus.QUEUED);
        when(idGenerator.nextId()).thenReturn(CONVERSATION_ID, RUN_ID);
        when(conversationDao.save(any(AiConversation.class))).thenReturn(true);
        when(analysisRunDao.save(any(MarketResearchAnalysisRun.class))).thenReturn(true);

        var result = service.createInitial(job, "  聚焦退货风险  ");

        ArgumentCaptor<MarketResearchAnalysisRun> runCaptor =
                ArgumentCaptor.forClass(MarketResearchAnalysisRun.class);
        verify(analysisRunDao).save(runCaptor.capture());
        MarketResearchAnalysisRun run = runCaptor.getValue();
        assertThat(run.getAnalysisRunId()).isEqualTo(RUN_ID);
        assertThat(run.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(run.getRunType()).isEqualTo(ResearchAnalysisRunType.SCREENING.name());
        assertThat(run.getRunStatus()).isEqualTo(ResearchAnalysisRunStatus.WAITING_RESEARCH.name());
        assertThat(run.getAnalysisGoal())
                .contains("阶段一仅分析7张市场初筛证据表")
                .contains("用户分析目标：聚焦退货风险");
        assertThat(result.getStatus()).isEqualTo(ResearchAnalysisRunStatus.WAITING_RESEARCH.name());
        assertThat(result.getStageCode()).isEqualTo(ResearchStageCode.SCREENING.name());
    }

    @Test
    void shouldCreateNewScreeningRunAfterFailedResearchRetry() {
        MarketResearchJob job = job(ResearchJobStatus.FAILED);
        MarketResearchAnalysisRun previous = run("run-failed", ResearchAnalysisRunStatus.FAILED);
        when(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .thenReturn(Optional.of(previous));
        when(analysisRunDao.listByJobIdAndUserId(JOB_ID, USER_ID)).thenReturn(List.of(previous));
        when(idGenerator.nextId()).thenReturn("run-after-data-retry");
        when(analysisRunDao.save(any(MarketResearchAnalysisRun.class))).thenReturn(true);

        var result = service.prepareForResearchRetry(job);

        ArgumentCaptor<MarketResearchAnalysisRun> runCaptor =
                ArgumentCaptor.forClass(MarketResearchAnalysisRun.class);
        verify(analysisRunDao).save(runCaptor.capture());
        MarketResearchAnalysisRun run = runCaptor.getValue();
        assertThat(run.getAnalysisRunId()).isEqualTo("run-after-data-retry");
        assertThat(run.getParentRunId()).isEqualTo(previous.getAnalysisRunId());
        assertThat(run.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(run.getRunType()).isEqualTo(ResearchAnalysisRunType.SCREENING.name());
        assertThat(run.getRunStatus()).isEqualTo(ResearchAnalysisRunStatus.WAITING_RESEARCH.name());
        assertThat(run.getAnalysisGoal()).contains("阶段一仅分析7张市场初筛证据表");
        assertThat(result.getAnalysisRunId()).isEqualTo("run-after-data-retry");
        verify(eventPublisher).publish(any(ResearchEventCommand.class));
    }

    @Test
    void shouldCreateDeepDiveRunWhenResearchFailsAfterProductSelection() {
        MarketResearchJob job = job(ResearchJobStatus.FAILED);
        job.setCurrentStage(ResearchStageCode.DEEP_DIVE.name());
        MarketResearchAnalysisRun screening =
                run("run-screening", ResearchAnalysisRunStatus.SUCCEEDED, ResearchAnalysisRunType.SCREENING);
        when(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .thenReturn(Optional.of(screening));
        when(analysisRunDao.listByJobIdAndUserId(JOB_ID, USER_ID)).thenReturn(List.of(screening));
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.SCREENING.name()))
                .thenReturn(Optional.of(screening));
        when(idGenerator.nextId()).thenReturn("run-deep-dive-after-data-retry");
        when(analysisRunDao.save(any(MarketResearchAnalysisRun.class))).thenReturn(true);

        var result = service.prepareForResearchRetry(job);

        ArgumentCaptor<MarketResearchAnalysisRun> runCaptor =
                ArgumentCaptor.forClass(MarketResearchAnalysisRun.class);
        verify(analysisRunDao).save(runCaptor.capture());
        MarketResearchAnalysisRun run = runCaptor.getValue();
        assertThat(run.getRunType()).isEqualTo(ResearchAnalysisRunType.DEEP_DIVE.name());
        assertThat(run.getRunStatus()).isEqualTo(ResearchAnalysisRunStatus.WAITING_RESEARCH.name());
        assertThat(run.getAnalysisGoal()).contains("阶段二分析评价、VOC、Keywords、ASIN销售趋势、ASIN运营趋势五张表");
        assertThat(result.getStageCode()).isEqualTo(ResearchStageCode.DEEP_DIVE.name());
    }

    @Test
    void shouldCreateIndependentStageRunsInTheSameConversation() {
        MarketResearchJob job = job(ResearchJobStatus.SUCCEEDED);
        MarketResearchAnalysisRun screening =
                run("run-screening", ResearchAnalysisRunStatus.SUCCEEDED, ResearchAnalysisRunType.SCREENING);
        AtomicReference<MarketResearchAnalysisRun> latest = new AtomicReference<>(screening);
        when(jobDao.getById(JOB_ID)).thenReturn(job);
        when(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .thenAnswer(invocation -> Optional.of(latest.get()));
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.SCREENING.name()))
                .thenReturn(Optional.of(screening));
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.DEEP_DIVE.name()))
                .thenAnswer(invocation -> ResearchAnalysisRunType.DEEP_DIVE.name()
                                .equals(latest.get().getRunType())
                        ? Optional.of(latest.get())
                        : Optional.empty());
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.FINAL_ANALYSIS.name()))
                .thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn("run-deep-dive", "run-final-analysis");
        when(analysisRunDao.save(any(MarketResearchAnalysisRun.class))).thenReturn(true);

        MarketResearchAnalysisRun deepDive =
                service.ensureStageRun(JOB_ID, ResearchStageCode.DEEP_DIVE);
        assertThat(deepDive.getAnalysisRunId()).isEqualTo("run-deep-dive");
        assertThat(deepDive.getParentRunId()).isEqualTo(screening.getAnalysisRunId());
        assertThat(deepDive.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(deepDive.getRunType()).isEqualTo(ResearchAnalysisRunType.DEEP_DIVE.name());
        assertThat(deepDive.getAnalysisGoal())
                .contains("阶段二分析评价、VOC、Keywords、ASIN销售趋势、ASIN运营趋势五张表")
                .contains("不得推算预算、ACOS或ROI");

        deepDive.setRunStatus(ResearchAnalysisRunStatus.SUCCEEDED.name());
        latest.set(deepDive);
        MarketResearchAnalysisRun finalAnalysis =
                service.ensureStageRun(JOB_ID, ResearchStageCode.FINAL_ANALYSIS);

        assertThat(finalAnalysis.getAnalysisRunId()).isEqualTo("run-final-analysis");
        assertThat(finalAnalysis.getParentRunId()).isEqualTo(deepDive.getAnalysisRunId());
        assertThat(finalAnalysis.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(finalAnalysis.getRunType())
                .isEqualTo(ResearchAnalysisRunType.FINAL_ANALYSIS.name());
        assertThat(finalAnalysis.getAnalysisGoal()).contains("阶段三基于完整12组证据");
        assertThat(List.of(
                        screening.getAnalysisRunId(),
                        deepDive.getAnalysisRunId(),
                        finalAnalysis.getAnalysisRunId()))
                .doesNotHaveDuplicates();
    }

    @Test
    void shouldKeepFollowUpTypeAndContextWhenRetryingFailedFollowUp() {
        MarketResearchJob job = job(ResearchJobStatus.SUCCEEDED);
        MarketResearchAnalysisRun previous = run(
                "run-follow-up-failed",
                ResearchAnalysisRunStatus.FAILED,
                ResearchAnalysisRunType.FOLLOW_UP);
        previous.setAnalysisGoal("追问：低预算进入的首要风险是什么？");
        when(analysisRunDao.findByIdAndUserId(previous.getAnalysisRunId(), USER_ID))
                .thenReturn(Optional.of(previous));
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .thenReturn(Optional.of(previous));
        when(idGenerator.nextId()).thenReturn("run-follow-up-retry");
        when(analysisRunDao.save(any(MarketResearchAnalysisRun.class))).thenReturn(true);

        var result = service.retryRun(previous.getAnalysisRunId());

        ArgumentCaptor<MarketResearchAnalysisRun> runCaptor =
                ArgumentCaptor.forClass(MarketResearchAnalysisRun.class);
        verify(analysisRunDao).save(runCaptor.capture());
        MarketResearchAnalysisRun retry = runCaptor.getValue();
        assertThat(retry.getAnalysisRunId()).isEqualTo("run-follow-up-retry");
        assertThat(retry.getRunType()).isEqualTo(ResearchAnalysisRunType.FOLLOW_UP.name());
        assertThat(retry.getConversationId()).isEqualTo(previous.getConversationId());
        assertThat(retry.getParentRunId()).isEqualTo(previous.getAnalysisRunId());
        assertThat(retry.getAnalysisGoal()).isEqualTo(previous.getAnalysisGoal());
        assertThat(result.getRunType()).isEqualTo(ResearchAnalysisRunType.FOLLOW_UP.name());
    }

    @Test
    void shouldUseRetryTypeWhenRetryingOtherFailedAnalysis() {
        MarketResearchJob job = job(ResearchJobStatus.SUCCEEDED);
        MarketResearchAnalysisRun previous = run(
                "run-final-failed",
                ResearchAnalysisRunStatus.FAILED,
                ResearchAnalysisRunType.FINAL_ANALYSIS);
        when(analysisRunDao.findByIdAndUserId(previous.getAnalysisRunId(), USER_ID))
                .thenReturn(Optional.of(previous));
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .thenReturn(Optional.of(previous));
        when(idGenerator.nextId()).thenReturn("run-final-retry");
        when(analysisRunDao.save(any(MarketResearchAnalysisRun.class))).thenReturn(true);

        var result = service.retryRun(previous.getAnalysisRunId());

        ArgumentCaptor<MarketResearchAnalysisRun> runCaptor =
                ArgumentCaptor.forClass(MarketResearchAnalysisRun.class);
        verify(analysisRunDao).save(runCaptor.capture());
        MarketResearchAnalysisRun retry = runCaptor.getValue();
        assertThat(retry.getRunType()).isEqualTo(ResearchAnalysisRunType.RETRY.name());
        assertThat(retry.getConversationId()).isEqualTo(previous.getConversationId());
        assertThat(retry.getParentRunId()).isEqualTo(previous.getAnalysisRunId());
        assertThat(retry.getAnalysisGoal()).isEqualTo(previous.getAnalysisGoal());
        assertThat(result.getRunType()).isEqualTo(ResearchAnalysisRunType.RETRY.name());
    }

    @Test
    void shouldRejectRetryingStaleFailedRun() {
        MarketResearchAnalysisRun stale = run("run-stale", ResearchAnalysisRunStatus.FAILED);
        MarketResearchAnalysisRun latest = run("run-latest", ResearchAnalysisRunStatus.SUCCEEDED);
        MarketResearchJob job = job(ResearchJobStatus.SUCCEEDED);
        when(analysisRunDao.findByIdAndUserId("run-stale", USER_ID)).thenReturn(Optional.of(stale));
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .thenReturn(Optional.of(latest));

        assertThatThrownBy(() -> service.retryRun("run-stale"))
                .isInstanceOf(BizException.class);

        verify(analysisRunDao, never()).save(any(MarketResearchAnalysisRun.class));
    }

    @Test
    void shouldCancelTheExplicitRunInsteadOfLatestRun() {
        MarketResearchAnalysisRun target = run("run-target", ResearchAnalysisRunStatus.QUEUED);
        when(analysisRunDao.findByIdAndUserId("run-target", USER_ID))
                .thenReturn(Optional.of(target));
        when(analysisRunDao.cancelPending(
                        org.mockito.ArgumentMatchers.eq("run-target"),
                        org.mockito.ArgumentMatchers.eq(USER_ID),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);

        service.cancelRun("run-target");

        verify(analysisRunDao).cancelPending(
                org.mockito.ArgumentMatchers.eq("run-target"),
                org.mockito.ArgumentMatchers.eq(USER_ID),
                org.mockito.ArgumentMatchers.anyLong());
        verify(analysisRunDao, never()).findLatestByJobIdAndUserId(any(), any());
    }

    private MarketResearchJob job(ResearchJobStatus status) {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        job.setReportName("市场调研测试");
        job.setJobStatus(status.name());
        job.setCurrentStage(ResearchStageCode.SCREENING.name());
        return job;
    }

    private MarketResearchAnalysisRun run(String runId, ResearchAnalysisRunStatus status) {
        return run(runId, status, ResearchAnalysisRunType.SCREENING);
    }

    private MarketResearchAnalysisRun run(
            String runId, ResearchAnalysisRunStatus status, ResearchAnalysisRunType runType) {
        MarketResearchAnalysisRun run = new MarketResearchAnalysisRun();
        run.setAnalysisRunId(runId);
        run.setJobId(JOB_ID);
        run.setUserId(USER_ID);
        run.setConversationId(CONVERSATION_ID);
        run.setRunType(runType.name());
        run.setAnalysisGoal("分析机会与风险");
        run.setRunStatus(status.name());
        run.setCurrentPhase(status.name().toLowerCase());
        run.setProgress(0);
        run.setAttemptCount(0);
        run.setMaxAttempts(3);
        run.setNextRunAt(System.currentTimeMillis());
        run.setModelCallCount(0);
        run.setEventCount(0);
        run.setErrorCode("");
        run.setErrorMessage("");
        return run;
    }
}
