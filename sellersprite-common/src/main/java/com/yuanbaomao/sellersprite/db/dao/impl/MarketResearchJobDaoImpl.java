package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.mapper.MarketResearchJobMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchJobDaoImpl extends ServiceImpl<MarketResearchJobMapper, MarketResearchJob>
        implements MarketResearchJobDao {

    @Override
    public Optional<MarketResearchJob> findByIdAndUserId(String jobId, String userId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getUserId, userId)
                .one());
    }

    @Override
    public Optional<MarketResearchJob> findByBatchJobExecutionId(Long batchJobExecutionId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchJob::getBatchJobExecutionId, batchJobExecutionId)
                .one());
    }

    @Override
    public List<MarketResearchJob> listQueuedWithoutBatchExecution(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        return lambdaQuery()
                .eq(MarketResearchJob::getJobStatus, "QUEUED")
                .isNull(MarketResearchJob::getBatchJobExecutionId)
                .orderByAsc(MarketResearchJob::getCreatedAt)
                .last("LIMIT " + safeLimit)
                .list();
    }

    @Override
    public boolean updateStatusAndProgress(
            String jobId, String jobStatus, String currentPhase, int progress) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .set(MarketResearchJob::getJobStatus, jobStatus)
                .set(MarketResearchJob::getCurrentPhase, currentPhase)
                .set(MarketResearchJob::getProgress, progress)
                .update();
    }

    @Override
    public boolean bindBatchExecution(
            String jobId, Long batchJobInstanceId, Long batchJobExecutionId) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .set(MarketResearchJob::getBatchJobInstanceId, batchJobInstanceId)
                .set(MarketResearchJob::getBatchJobExecutionId, batchJobExecutionId)
                .update();
    }
}
