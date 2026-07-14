package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import java.util.List;
import java.util.Optional;

public interface MarketResearchJobDao extends IService<MarketResearchJob> {

    Optional<MarketResearchJob> findByIdAndUserId(String jobId, String userId);

    Optional<MarketResearchJob> findByBatchJobExecutionId(Long batchJobExecutionId);

    List<MarketResearchJob> listQueuedWithoutBatchExecution(int limit);

    boolean updateStatusAndProgress(String jobId, String jobStatus, String currentPhase, int progress);

    boolean bindBatchExecution(String jobId, Long batchJobInstanceId, Long batchJobExecutionId);
}
