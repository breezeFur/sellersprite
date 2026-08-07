package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import java.util.List;
import java.util.Optional;

public interface MarketResearchJobDao extends IService<MarketResearchJob> {

    Optional<MarketResearchJob> findByIdAndUserId(String jobId, String userId);

    Page<MarketResearchJob> pageByUserId(
            String userId,
            String keyword,
            String status,
            String marketplace,
            String month,
            long current,
            long size);

    List<MarketResearchJob> listDispatchCandidates(long now, int limit);

    List<MarketResearchJob> listMarketTrendCacheWarmupCandidates();

    boolean tryClaim(
            String jobId, String executionOwner, String executionToken, long now, long leaseUntil);

    boolean heartbeat(
            String jobId, String executionOwner, String executionToken, long now, long leaseUntil);

    boolean isOwnedRunning(String jobId, String executionToken);

    boolean updateProgress(String jobId, String executionToken, String currentNode, int progress);

    boolean updateStage(String jobId, String executionToken, String currentStage);

    boolean markWaitingInput(
            String jobId,
            String executionOwner,
            String executionToken,
            String currentNode,
            String currentStage,
            String waitingInputType);

    boolean requeueWaitingInput(
            String jobId, String userId, String waitingInputType, long nextRunAt);

    boolean markSucceeded(
            String jobId,
            String executionOwner,
            String executionToken,
            String currentNode,
            long finishedAt);

    boolean markRetryWait(
            String jobId,
            String executionOwner,
            String executionToken,
            long nextRunAt,
            String errorCode,
            String errorMessage);

    boolean markFailed(
            String jobId,
            String executionOwner,
            String executionToken,
            long finishedAt,
            String errorCode,
            String errorMessage);

    boolean markCancelled(
            String jobId, String executionOwner, String executionToken, long finishedAt);

    boolean markAbandoned(
            String jobId,
            String executionOwner,
            String executionToken,
            String currentNode,
            long finishedAt);

    boolean cancelPending(String jobId, String userId, long cancelledAt);

    boolean requestRunningCancel(String jobId, String userId, long requestedAt);

    boolean retryFailed(String jobId, String userId, long nextRunAt);

}
