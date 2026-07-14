package com.yuanbaomao.sellersprite.research.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

@ExtendWith(MockitoExtension.class)
class ResearchBatchLauncherTest {

    private static final String JOB_ID = "job-launcher-001";

    @Mock
    private JobOperator jobOperator;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private MarketResearchJobDao jobDao;

    @Mock
    private ResearchJobStateService jobStateService;

    @Mock
    private Job researchJob;

    @Mock
    private JobExecution execution;

    private ResearchBatchLauncher launcher;

    @BeforeEach
    void setUp() {
        launcher = new ResearchBatchLauncher(
                jobOperator, jobRepository, jobDao, jobStateService, researchJob);
        when(researchJob.getName()).thenReturn("marketResearchJob");
    }

    @Test
    void shouldLaunchAndBindWhenNoExecutionExists() throws Exception {
        when(jobRepository.getLastJobExecution(eq("marketResearchJob"), any(JobParameters.class)))
                .thenReturn(null);
        when(jobOperator.start(eq(researchJob), any(JobParameters.class))).thenReturn(execution);
        stubExecutionIds();

        launcher.start(JOB_ID);

        verify(jobDao).bindBatchExecution(JOB_ID, 11L, 22L);
        verify(jobStateService, never()).markFailed(eq(JOB_ID), any());
    }

    @Test
    void shouldReconcileCompletedExecutionWithoutStartingDuplicate() throws Exception {
        when(jobRepository.getLastJobExecution(eq("marketResearchJob"), any(JobParameters.class)))
                .thenReturn(execution);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        stubExecutionIds();

        launcher.start(JOB_ID);

        verify(jobOperator, never()).start(eq(researchJob), any(JobParameters.class));
        verify(jobDao).bindBatchExecution(JOB_ID, 11L, 22L);
        verify(jobStateService).markSucceeded(JOB_ID);
    }

    private void stubExecutionIds() {
        when(execution.getJobInstanceId()).thenReturn(11L);
        when(execution.getId()).thenReturn(22L);
        when(jobDao.bindBatchExecution(JOB_ID, 11L, 22L)).thenReturn(true);
    }
}
