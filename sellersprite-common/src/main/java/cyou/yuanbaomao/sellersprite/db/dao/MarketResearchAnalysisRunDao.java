package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MarketResearchAnalysisRunDao extends IService<MarketResearchAnalysisRun> {

    Optional<MarketResearchAnalysisRun> findByIdAndUserId(String analysisRunId, String userId);

    List<MarketResearchAnalysisRun> listByJobIdAndUserId(String jobId, String userId);

    List<MarketResearchAnalysisRun> listByJobIdsAndUserId(
            Collection<String> jobIds, String userId);

    Optional<MarketResearchAnalysisRun> findLatestByJobIdAndUserId(String jobId, String userId);

    Optional<MarketResearchAnalysisRun> findLatestByJobIdAndUserIdAndRunType(
            String jobId, String userId, String runType);

    List<MarketResearchAnalysisRun> listDispatchCandidates(long now, int limit);

    boolean failWaitingByJobId(
            String jobId, long finishedAt, String errorCode, String errorMessage);

    boolean cancelWaitingByJobId(String jobId, long finishedAt);

    boolean tryClaim(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil);

    boolean tryStartInitial(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil);

    boolean heartbeat(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil);

    boolean updateProgress(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            String currentPhase,
            int progress);

    boolean incrementCounters(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            int modelCallIncrement,
            int eventIncrement);

    boolean saveFinalSummary(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            String finalSummary);

    boolean markSucceeded(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            String finalSummary,
            long finishedAt);

    boolean markRetryWait(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long nextRunAt,
            String errorCode,
            String errorMessage);

    boolean markFailed(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long finishedAt,
            String errorCode,
            String errorMessage);

    boolean markCancelled(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long finishedAt);

    boolean cancelPending(String analysisRunId, String userId, long cancelledAt);

    boolean requestRunningCancel(String analysisRunId, String userId, long requestedAt);
}
