package com.yuanbaomao.sellersprite.research.service;

import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将 Spring Batch 的执行状态同步为用户可理解的业务状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchJobStateService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;

    private final MarketResearchJobDao jobDao;

    @Transactional(readOnly = true)
    public MarketResearchJob requireJob(String jobId) {
        return Objects.requireNonNull(jobDao.getById(jobId), "市场调研任务不存在: " + jobId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRunning(String jobId, JobExecution execution) {
        MarketResearchJob job = requireJob(jobId);
        job.setJobStatus(ResearchJobStatus.RUNNING.name());
        job.setCurrentPhase(ResearchPhase.VALIDATE.name());
        job.setProgress(0);
        job.setBatchJobInstanceId(execution.getJobInstance().getInstanceId());
        job.setBatchJobExecutionId(execution.getId());
        job.setStartedAt(System.currentTimeMillis());
        job.setFinishedAt(null);
        job.setErrorCode("");
        job.setErrorMessage("");
        jobDao.updateById(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enter(String jobId, ResearchPhase phase) {
        boolean updated = jobDao.updateStatusAndProgress(
                jobId, ResearchJobStatus.RUNNING.name(), phase.name(), phase.getStartProgress());
        if (!updated) {
            throw new IllegalStateException("更新市场调研任务阶段失败: " + jobId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void advance(String jobId, ResearchPhase phase) {
        boolean updated = jobDao.updateStatusAndProgress(
                jobId, ResearchJobStatus.RUNNING.name(), phase.name(), phase.getProgress());
        if (!updated) {
            throw new IllegalStateException("更新市场调研任务进度失败: " + jobId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSucceeded(String jobId) {
        MarketResearchJob job = requireJob(jobId);
        job.setJobStatus(ResearchJobStatus.SUCCEEDED.name());
        job.setCurrentPhase(ResearchPhase.VALIDATE_AND_PUBLISH.name());
        job.setProgress(ResearchPhase.VALIDATE_AND_PUBLISH.getProgress());
        job.setFinishedAt(System.currentTimeMillis());
        job.setErrorCode("");
        job.setErrorMessage("");
        jobDao.updateById(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String jobId, Throwable cause) {
        MarketResearchJob job = requireJob(jobId);
        job.setJobStatus(ResearchJobStatus.FAILED.name());
        job.setFinishedAt(System.currentTimeMillis());
        job.setErrorCode(ResearchConstants.ERROR_CODE_EXECUTION_FAILED);
        job.setErrorMessage(safeMessage(cause));
        jobDao.updateById(job);
        log.warn("市场调研任务执行失败，jobId={}，phase={}", jobId, job.getCurrentPhase(), cause);
    }

    private String safeMessage(Throwable cause) {
        String message = cause == null ? "市场调研任务执行失败" : cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause == null ? "市场调研任务执行失败" : cause.getClass().getSimpleName();
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
