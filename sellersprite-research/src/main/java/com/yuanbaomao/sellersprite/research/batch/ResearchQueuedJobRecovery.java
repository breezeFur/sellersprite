package com.yuanbaomao.sellersprite.research.batch;

import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 应用启动时补偿事务已提交、但尚未创建 BatchExecution 的排队任务。
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "sellersprite.research",
        name = "recovery-enabled",
        havingValue = "true",
        matchIfMissing = true)
@RequiredArgsConstructor
public class ResearchQueuedJobRecovery implements ApplicationRunner {

    private static final int RECOVERY_BATCH_SIZE = 100;

    private final MarketResearchJobDao jobDao;
    private final ResearchBatchLauncher batchLauncher;

    @Override
    public void run(ApplicationArguments arguments) {
        for (MarketResearchJob job : jobDao.listQueuedWithoutBatchExecution(RECOVERY_BATCH_SIZE)) {
            log.info("补偿启动排队中的市场调研任务，jobId={}", job.getJobId());
            batchLauncher.start(job.getJobId());
        }
    }
}
