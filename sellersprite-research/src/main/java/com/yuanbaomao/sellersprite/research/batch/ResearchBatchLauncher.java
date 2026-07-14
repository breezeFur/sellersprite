package com.yuanbaomao.sellersprite.research.batch;

import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

/**
 * 仅由业务服务触发的市场调研 Batch 启动器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResearchBatchLauncher {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final MarketResearchJobDao jobDao;
    private final ResearchJobStateService jobStateService;

    private final Job researchJob;

    public void start(String jobId) {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addString(ResearchConstants.JOB_ID_PARAMETER, jobId)
                    .toJobParameters();
            JobExecution existing = jobRepository.getLastJobExecution(researchJob.getName(), parameters);
            if (existing != null) {
                reconcileExisting(jobId, existing);
                return;
            }
            JobExecution execution = jobOperator.start(researchJob, parameters);
            bindExecution(jobId, execution);
            log.info("市场调研Batch已提交，jobId={}，executionId={}", jobId, execution.getId());
        } catch (Exception exception) {
            jobStateService.markFailed(jobId, exception);
            log.error("市场调研Batch提交失败，jobId={}", jobId, exception);
        }
    }

    public void restart(String jobId) {
        MarketResearchJob job = jobStateService.requireJob(jobId);
        if (job.getBatchJobExecutionId() == null) {
            throw new IllegalStateException("任务尚无可重启的Batch执行记录: " + jobId);
        }
        try {
            JobExecution previous = jobRepository.getJobExecution(job.getBatchJobExecutionId());
            if (previous == null) {
                throw new IllegalStateException("Batch执行记录不存在: " + job.getBatchJobExecutionId());
            }
            JobExecution restarted = jobOperator.restart(previous);
            bindExecution(jobId, restarted);
            log.info("市场调研Batch已重启，jobId={}，executionId={}", jobId, restarted.getId());
        } catch (Exception exception) {
            jobStateService.markFailed(jobId, exception);
            throw new IllegalStateException("重启市场调研任务失败: " + jobId, exception);
        }
    }

    private void reconcileExisting(String jobId, JobExecution existing) throws Exception {
        BatchStatus status = existing.getStatus();
        if (status == BatchStatus.FAILED || status == BatchStatus.STOPPED) {
            JobExecution restarted = jobOperator.restart(existing);
            bindExecution(jobId, restarted);
            log.info("市场调研Batch恢复既有失败执行，jobId={}，executionId={}", jobId, restarted.getId());
            return;
        }
        if (status == BatchStatus.COMPLETED) {
            bindExecution(jobId, existing);
            jobStateService.markSucceeded(jobId);
            log.info("市场调研Batch既有执行已完成，jobId={}，executionId={}", jobId, existing.getId());
            return;
        }
        if (status == BatchStatus.STARTING
                || status == BatchStatus.STARTED
                || status == BatchStatus.STOPPING) {
            bindExecution(jobId, existing);
            log.info("市场调研Batch既有执行仍在运行，jobId={}，executionId={}", jobId, existing.getId());
            return;
        }
        throw new IllegalStateException("Batch既有执行状态不可恢复: " + status);
    }

    private void bindExecution(String jobId, JobExecution execution) {
        if (!jobDao.bindBatchExecution(jobId, execution.getJobInstanceId(), execution.getId())) {
            throw new IllegalStateException("绑定市场调研Batch执行记录失败: " + jobId);
        }
    }
}
