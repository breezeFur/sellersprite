package cyou.yuanbaomao.sellersprite.research.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchJobStateServiceTest {

    private static final String JOB_ID = "job-state-001";
    private static final String OWNER = "worker-a";
    private static final String EXECUTION_TOKEN = "execution-token-a";

    @Mock
    private MarketResearchJobDao jobDao;

    @Mock
    private ResearchSseEventPublisher eventPublisher;

    @Mock
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Mock
    private ResearchStageInputService stageInputService;

    private ResearchProperties properties;
    private ResearchJobStateService stateService;

    @BeforeEach
    void setUp() {
        properties = new ResearchProperties();
        properties.setRetryBaseDelayMs(1_000L);
        properties.setRetryMaxDelayMs(5_000L);
        stateService = new ResearchJobStateService(
                jobDao, properties, eventPublisher, analysisRunDao, stageInputService);
    }

    @Test
    void shouldPrepareImmutableCandidatesBeforeEnteringWaitingState() {
        when(jobDao.markWaitingInput(
                        JOB_ID,
                        OWNER,
                        EXECUTION_TOKEN,
                        "productSelectionGate",
                        "SCREENING",
                        "PRODUCT_SELECTION"))
                .thenReturn(true);

        stateService.markWaitingInput(lease(1));

        InOrder inOrder = inOrder(stageInputService, jobDao);
        inOrder.verify(stageInputService).prepareProductCandidates(JOB_ID);
        inOrder.verify(jobDao).markWaitingInput(
                JOB_ID,
                OWNER,
                EXECUTION_TOKEN,
                "productSelectionGate",
                "SCREENING",
                "PRODUCT_SELECTION");
    }

    @Test
    void shouldScheduleBoundedExponentialRetryForTransientFailure() {
        MarketResearchJob job = runningJob(4, 5);
        when(jobDao.getById(JOB_ID)).thenReturn(job);
        when(jobDao.markRetryWait(
                        eq(JOB_ID),
                        eq(OWNER),
                        eq(EXECUTION_TOKEN),
                        anyLong(),
                        eq(ResearchConstants.ERROR_CODE_EXECUTION_FAILED),
                        eq("temporary")))
                .thenReturn(true);
        long before = System.currentTimeMillis();

        stateService.handleExecutionFailure(lease(4), new IllegalStateException("temporary"));

        verify(jobDao).markRetryWait(
                eq(JOB_ID),
                eq(OWNER),
                eq(EXECUTION_TOKEN),
                org.mockito.ArgumentMatchers.longThat(nextRunAt ->
                        nextRunAt >= before + 5_000L && nextRunAt <= System.currentTimeMillis() + 5_000L),
                eq(ResearchConstants.ERROR_CODE_EXECUTION_FAILED),
                eq("temporary"));
        verify(jobDao, never()).markFailed(
                eq(JOB_ID), eq(OWNER), eq(EXECUTION_TOKEN), anyLong(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldFailImmediatelyForValidationError() {
        MarketResearchJob job = runningJob(1, 5);
        when(jobDao.getById(JOB_ID)).thenReturn(job);
        when(jobDao.markFailed(
                        eq(JOB_ID),
                        eq(OWNER),
                        eq(EXECUTION_TOKEN),
                        anyLong(),
                        eq(ResearchConstants.ERROR_CODE_VALIDATION_FAILED),
                        eq("invalid input")))
                .thenReturn(true);

        stateService.handleExecutionFailure(lease(1), new IllegalArgumentException("invalid input"));

        verify(jobDao).markFailed(
                eq(JOB_ID),
                eq(OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(ResearchConstants.ERROR_CODE_VALIDATION_FAILED),
                eq("invalid input"));
        verify(jobDao, never()).markRetryWait(
                eq(JOB_ID), eq(OWNER), eq(EXECUTION_TOKEN), anyLong(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldFailWhenAutomaticAttemptsAreExhausted() {
        MarketResearchJob job = runningJob(3, 3);
        when(jobDao.getById(JOB_ID)).thenReturn(job);
        when(jobDao.markFailed(
                        eq(JOB_ID),
                        eq(OWNER),
                        eq(EXECUTION_TOKEN),
                        anyLong(),
                        eq(ResearchConstants.ERROR_CODE_EXECUTION_FAILED),
                        eq("still failing")))
                .thenReturn(true);

        stateService.handleExecutionFailure(lease(3), new IllegalStateException("still failing"));

        verify(jobDao).markFailed(
                eq(JOB_ID),
                eq(OWNER),
                eq(EXECUTION_TOKEN),
                anyLong(),
                eq(ResearchConstants.ERROR_CODE_EXECUTION_FAILED),
                eq("still failing"));
    }

    private MarketResearchJob runningJob(int attemptCount, int maxAttempts) {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setAttemptCount(attemptCount);
        job.setMaxAttempts(maxAttempts);
        return job;
    }

    private ResearchExecutionLease lease(int attemptCount) {
        return new ResearchExecutionLease(
                JOB_ID, ResearchConstants.WORKFLOW_VERSION, OWNER, EXECUTION_TOKEN, attemptCount);
    }
}
