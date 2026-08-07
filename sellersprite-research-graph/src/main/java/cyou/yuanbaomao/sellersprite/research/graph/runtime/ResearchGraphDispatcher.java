package cyou.yuanbaomao.sellersprite.research.graph.runtime;

import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchMarketTrendCacheWarmup;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 以数据库任务表为事实源轮询、原子抢占并提交Graph执行。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "sellersprite.research",
        name = "dispatcher-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ResearchGraphDispatcher {

    private final String executionOwner = "research-" + UUID.randomUUID();

    private final ResearchJobStateService jobStateService;
    private final ResearchGraphExecutor graphExecutor;
    private final ResearchMarketTrendCacheWarmup marketTrendCacheWarmup;

    @Scheduled(fixedDelayString = "${sellersprite.research.poll-interval-ms:2000}")
    public void poll() {
        if (!marketTrendCacheWarmup.isReadyForDispatch()) {
            return;
        }
        for (String jobId : jobStateService.listDispatchCandidates()) {
            dispatchNow(jobId);
        }
    }

    public void dispatchNow(String jobId) {
        if (!marketTrendCacheWarmup.isReadyForDispatch()) {
            return;
        }
        jobStateService.tryClaim(jobId, executionOwner).ifPresent(this::submit);
    }

    private void submit(ResearchExecutionLease lease) {
        try {
            graphExecutor.submit(lease);
        } catch (RuntimeException exception) {
            log.error("提交市场调研Graph执行失败，jobId={}，owner={}",
                    lease.jobId(), lease.executionOwner(), exception);
            jobStateService.handleExecutionFailure(lease, exception);
        }
    }
}
