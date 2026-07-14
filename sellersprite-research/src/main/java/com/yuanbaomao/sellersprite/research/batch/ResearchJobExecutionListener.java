package com.yuanbaomao.sellersprite.research.batch;

import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 同步 Batch 执行结果到市场调研业务任务。
 */
@Component
@RequiredArgsConstructor
public class ResearchJobExecutionListener implements JobExecutionListener {

    private final ResearchJobStateService jobStateService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobStateService.markRunning(jobId(jobExecution), jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobId = jobId(jobExecution);
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            jobStateService.markSucceeded(jobId);
            return;
        }
        List<Throwable> failures = jobExecution.getAllFailureExceptions();
        Throwable cause = failures.isEmpty()
                ? new IllegalStateException("Spring Batch执行状态为" + jobExecution.getStatus())
                : failures.getFirst();
        jobStateService.markFailed(jobId, cause);
    }

    private String jobId(JobExecution jobExecution) {
        String jobId = jobExecution.getJobParameters()
                .getString(ResearchConstants.JOB_ID_PARAMETER);
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalStateException("Spring Batch参数缺少jobId");
        }
        return jobId;
    }
}
