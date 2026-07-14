package com.yuanbaomao.sellersprite.research.batch;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class ResearchQueuedJobRecoveryTest {

    @Mock
    private MarketResearchJobDao jobDao;

    @Mock
    private ResearchBatchLauncher batchLauncher;

    @Test
    void shouldLaunchQueuedJobsWithoutBatchExecutionOnStartup() {
        MarketResearchJob first = job("job-001");
        MarketResearchJob second = job("job-002");
        when(jobDao.listQueuedWithoutBatchExecution(100)).thenReturn(List.of(first, second));
        ResearchQueuedJobRecovery recovery = new ResearchQueuedJobRecovery(jobDao, batchLauncher);

        recovery.run(new DefaultApplicationArguments(new String[0]));

        verify(batchLauncher).start("job-001");
        verify(batchLauncher).start("job-002");
    }

    private MarketResearchJob job(String jobId) {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(jobId);
        return job;
    }
}
