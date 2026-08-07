package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchAnalysisRunMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchAnalysisRunDaoImpl
        extends ServiceImpl<MarketResearchAnalysisRunMapper, MarketResearchAnalysisRun>
        implements MarketResearchAnalysisRunDao {

    private static final int MIN_DISPATCH_LIMIT = 1;
    private static final int MAX_DISPATCH_LIMIT = 1000;
    private static final int MIN_PROGRESS = 0;
    private static final int MAX_PROGRESS = 100;
    private static final String STATUS_WAITING_RESEARCH = "WAITING_RESEARCH";
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_RETRY_WAIT = "RETRY_WAIT";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String RUN_TYPE_INITIAL = "INITIAL";
    private static final List<String> PARENT_MANAGED_RUN_TYPES = List.of(
            "SCREENING", "DEEP_DIVE", "FINAL_ANALYSIS", RUN_TYPE_INITIAL);
    private static final String PHASE_QUEUED = "queued";
    private static final String PHASE_COMPLETED = "completed";
    private static final String PHASE_FAILED = "failed";
    private static final String PHASE_CANCELLED = "cancelled";

    /** 分析候选必须关联已经成功完成的数据任务，避免绕过“先数据、后分析”的阶段边界。 */
    private static final String SUCCEEDED_RESEARCH_JOB_PREDICATE =
            "EXISTS (SELECT 1 FROM market_research_job research_job "
                    + "WHERE research_job.job_id = market_research_analysis_run.job_id "
                    + "AND research_job.job_status = 'SUCCEEDED' AND research_job.deleted = 0)";

    @Override
    public Optional<MarketResearchAnalysisRun> findByIdAndUserId(
            String analysisRunId, String userId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchAnalysisRun::getAnalysisRunId, analysisRunId)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .one());
    }

    @Override
    public List<MarketResearchAnalysisRun> listByJobIdAndUserId(String jobId, String userId) {
        return lambdaQuery()
                .eq(MarketResearchAnalysisRun::getJobId, jobId)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .orderByAsc(MarketResearchAnalysisRun::getCreatedAt)
                .orderByAsc(MarketResearchAnalysisRun::getAnalysisRunId)
                .list();
    }

    @Override
    public List<MarketResearchAnalysisRun> listByJobIdsAndUserId(
            Collection<String> jobIds, String userId) {
        if (jobIds == null || jobIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .in(MarketResearchAnalysisRun::getJobId, jobIds)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .orderByDesc(MarketResearchAnalysisRun::getCreatedAt)
                .orderByDesc(MarketResearchAnalysisRun::getAnalysisRunId)
                .list();
    }

    @Override
    public Optional<MarketResearchAnalysisRun> findLatestByJobIdAndUserId(
            String jobId, String userId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchAnalysisRun::getJobId, jobId)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .orderByDesc(MarketResearchAnalysisRun::getCreatedAt)
                .orderByDesc(MarketResearchAnalysisRun::getAnalysisRunId)
                .last("LIMIT 1")
                .one());
    }

    @Override
    public Optional<MarketResearchAnalysisRun> findLatestByJobIdAndUserIdAndRunType(
            String jobId, String userId, String runType) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchAnalysisRun::getJobId, jobId)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .eq(MarketResearchAnalysisRun::getRunType, runType)
                .orderByDesc(MarketResearchAnalysisRun::getCreatedAt)
                .orderByDesc(MarketResearchAnalysisRun::getAnalysisRunId)
                .last("LIMIT 1")
                .one());
    }

    @Override
    public List<MarketResearchAnalysisRun> listDispatchCandidates(long now, int limit) {
        int safeLimit = Math.max(MIN_DISPATCH_LIMIT, Math.min(limit, MAX_DISPATCH_LIMIT));
        return lambdaQuery()
                .notIn(MarketResearchAnalysisRun::getRunType, PARENT_MANAGED_RUN_TYPES)
                .and(candidate -> candidate
                        .and(pending -> pending
                                .in(
                                        MarketResearchAnalysisRun::getRunStatus,
                                        STATUS_QUEUED,
                                        STATUS_RETRY_WAIT)
                                .le(MarketResearchAnalysisRun::getNextRunAt, now))
                        .or(expired -> expired
                                .eq(MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)
                                .lt(MarketResearchAnalysisRun::getLeaseUntil, now)))
                .apply(SUCCEEDED_RESEARCH_JOB_PREDICATE)
                .apply("attempt_count < max_attempts")
                .orderByAsc(MarketResearchAnalysisRun::getNextRunAt)
                .orderByAsc(MarketResearchAnalysisRun::getCreatedAt)
                .last("LIMIT " + safeLimit)
                .list();
    }

    @Override
    public boolean failWaitingByJobId(
            String jobId, long finishedAt, String errorCode, String errorMessage) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getJobId, jobId)
                .eq(MarketResearchAnalysisRun::getRunStatus, STATUS_WAITING_RESEARCH)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_FAILED)
                .set(MarketResearchAnalysisRun::getCurrentPhase, PHASE_FAILED)
                .set(MarketResearchAnalysisRun::getFinishedAt, finishedAt)
                .set(MarketResearchAnalysisRun::getErrorCode, errorCode)
                .set(MarketResearchAnalysisRun::getErrorMessage, errorMessage)
                .update();
    }

    @Override
    public boolean cancelWaitingByJobId(String jobId, long finishedAt) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getJobId, jobId)
                .eq(MarketResearchAnalysisRun::getRunStatus, STATUS_WAITING_RESEARCH)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_CANCELLED)
                .set(MarketResearchAnalysisRun::getCurrentPhase, PHASE_CANCELLED)
                .set(MarketResearchAnalysisRun::getCancelRequestedAt, finishedAt)
                .set(MarketResearchAnalysisRun::getFinishedAt, finishedAt)
                .update();
    }

    @Override
    public boolean tryClaim(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getAnalysisRunId, analysisRunId)
                .notIn(MarketResearchAnalysisRun::getRunType, PARENT_MANAGED_RUN_TYPES)
                .and(candidate -> candidate
                        .and(pending -> pending
                                .in(
                                        MarketResearchAnalysisRun::getRunStatus,
                                        STATUS_QUEUED,
                                        STATUS_RETRY_WAIT)
                                .le(MarketResearchAnalysisRun::getNextRunAt, now))
                        .or(expired -> expired
                                .eq(MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)
                                .lt(MarketResearchAnalysisRun::getLeaseUntil, now)))
                .apply(SUCCEEDED_RESEARCH_JOB_PREDICATE)
                .apply("attempt_count < max_attempts")
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)
                .set(MarketResearchAnalysisRun::getExecutionOwner, executionOwner)
                .set(MarketResearchAnalysisRun::getExecutionToken, executionToken)
                .set(MarketResearchAnalysisRun::getHeartbeatAt, now)
                .set(MarketResearchAnalysisRun::getLeaseUntil, leaseUntil)
                .set(MarketResearchAnalysisRun::getNextRunAt, now)
                .setSql("attempt_count = attempt_count + 1, "
                        + "started_at = COALESCE(started_at, " + now + ")")
                .update();
    }

    @Override
    public boolean tryStartInitial(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getAnalysisRunId, analysisRunId)
                .in(MarketResearchAnalysisRun::getRunType, PARENT_MANAGED_RUN_TYPES)
                .and(candidate -> candidate
                        .in(
                                MarketResearchAnalysisRun::getRunStatus,
                                STATUS_WAITING_RESEARCH,
                                STATUS_FAILED)
                        .or(running -> running.eq(
                                MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)))
                .apply("attempt_count < max_attempts")
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)
                .set(MarketResearchAnalysisRun::getExecutionOwner, executionOwner)
                .set(MarketResearchAnalysisRun::getExecutionToken, executionToken)
                .set(MarketResearchAnalysisRun::getHeartbeatAt, now)
                .set(MarketResearchAnalysisRun::getLeaseUntil, leaseUntil)
                .set(MarketResearchAnalysisRun::getNextRunAt, now)
                .set(MarketResearchAnalysisRun::getFinishedAt, null)
                .set(MarketResearchAnalysisRun::getErrorCode, "")
                .set(MarketResearchAnalysisRun::getErrorMessage, "")
                .setSql("attempt_count = attempt_count + 1, "
                        + "started_at = COALESCE(started_at, " + now + ")")
                .update();
    }

    @Override
    public boolean heartbeat(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil) {
        return ownedRunningUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getHeartbeatAt, now)
                .set(MarketResearchAnalysisRun::getLeaseUntil, leaseUntil)
                .update();
    }

    @Override
    public boolean updateProgress(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            String currentPhase,
            int progress) {
        int safeProgress = Math.max(MIN_PROGRESS, Math.min(progress, MAX_PROGRESS));
        return ownedRunningUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getCurrentPhase, currentPhase)
                .set(MarketResearchAnalysisRun::getProgress, safeProgress)
                .update();
    }

    @Override
    public boolean incrementCounters(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            int modelCallIncrement,
            int eventIncrement) {
        int safeModelCallIncrement = Math.max(0, modelCallIncrement);
        int safeEventIncrement = Math.max(0, eventIncrement);
        return ownedRunningUpdate(analysisRunId, executionOwner, executionToken)
                .setSql("model_call_count = model_call_count + " + safeModelCallIncrement)
                .setSql("event_count = event_count + " + safeEventIncrement)
                .update();
    }

    @Override
    public boolean saveFinalSummary(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            String finalSummary) {
        return ownedRunningUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getFinalSummary, finalSummary)
                .update();
    }

    @Override
    public boolean markSucceeded(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            String finalSummary,
            long finishedAt) {
        return terminalUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_SUCCEEDED)
                .set(MarketResearchAnalysisRun::getCurrentPhase, PHASE_COMPLETED)
                .set(MarketResearchAnalysisRun::getProgress, MAX_PROGRESS)
                .set(MarketResearchAnalysisRun::getFinalSummary, finalSummary)
                .set(MarketResearchAnalysisRun::getFinishedAt, finishedAt)
                .set(MarketResearchAnalysisRun::getErrorCode, "")
                .set(MarketResearchAnalysisRun::getErrorMessage, "")
                .update();
    }

    @Override
    public boolean markRetryWait(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long nextRunAt,
            String errorCode,
            String errorMessage) {
        return terminalUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_RETRY_WAIT)
                .set(MarketResearchAnalysisRun::getNextRunAt, nextRunAt)
                .set(MarketResearchAnalysisRun::getErrorCode, errorCode)
                .set(MarketResearchAnalysisRun::getErrorMessage, errorMessage)
                .update();
    }

    @Override
    public boolean markFailed(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long finishedAt,
            String errorCode,
            String errorMessage) {
        return terminalUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_FAILED)
                .set(MarketResearchAnalysisRun::getCurrentPhase, PHASE_FAILED)
                .set(MarketResearchAnalysisRun::getFinishedAt, finishedAt)
                .set(MarketResearchAnalysisRun::getErrorCode, errorCode)
                .set(MarketResearchAnalysisRun::getErrorMessage, errorMessage)
                .update();
    }

    @Override
    public boolean markCancelled(
            String analysisRunId,
            String executionOwner,
            String executionToken,
            long finishedAt) {
        return terminalUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_CANCELLED)
                .set(MarketResearchAnalysisRun::getCurrentPhase, PHASE_CANCELLED)
                .set(MarketResearchAnalysisRun::getFinishedAt, finishedAt)
                .set(MarketResearchAnalysisRun::getErrorCode, "")
                .set(MarketResearchAnalysisRun::getErrorMessage, "")
                .update();
    }

    @Override
    public boolean cancelPending(String analysisRunId, String userId, long cancelledAt) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getAnalysisRunId, analysisRunId)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .in(
                        MarketResearchAnalysisRun::getRunStatus,
                        STATUS_WAITING_RESEARCH,
                        STATUS_QUEUED,
                        STATUS_RETRY_WAIT)
                .set(MarketResearchAnalysisRun::getRunStatus, STATUS_CANCELLED)
                .set(MarketResearchAnalysisRun::getCurrentPhase, PHASE_CANCELLED)
                .set(MarketResearchAnalysisRun::getCancelRequestedAt, cancelledAt)
                .set(MarketResearchAnalysisRun::getFinishedAt, cancelledAt)
                .set(MarketResearchAnalysisRun::getExecutionOwner, null)
                .set(MarketResearchAnalysisRun::getExecutionToken, null)
                .set(MarketResearchAnalysisRun::getLeaseUntil, null)
                .set(MarketResearchAnalysisRun::getHeartbeatAt, null)
                .update();
    }

    @Override
    public boolean requestRunningCancel(String analysisRunId, String userId, long requestedAt) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getAnalysisRunId, analysisRunId)
                .eq(MarketResearchAnalysisRun::getUserId, userId)
                .eq(MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)
                .isNull(MarketResearchAnalysisRun::getCancelRequestedAt)
                .set(MarketResearchAnalysisRun::getCancelRequestedAt, requestedAt)
                .update();
    }

    private LambdaUpdateChainWrapper<MarketResearchAnalysisRun> ownedRunningUpdate(
            String analysisRunId, String executionOwner, String executionToken) {
        return lambdaUpdate()
                .eq(MarketResearchAnalysisRun::getAnalysisRunId, analysisRunId)
                .eq(MarketResearchAnalysisRun::getRunStatus, STATUS_RUNNING)
                .eq(MarketResearchAnalysisRun::getExecutionOwner, executionOwner)
                .eq(MarketResearchAnalysisRun::getExecutionToken, executionToken);
    }

    private LambdaUpdateChainWrapper<MarketResearchAnalysisRun> terminalUpdate(
            String analysisRunId, String executionOwner, String executionToken) {
        return ownedRunningUpdate(analysisRunId, executionOwner, executionToken)
                .set(MarketResearchAnalysisRun::getExecutionOwner, null)
                .set(MarketResearchAnalysisRun::getExecutionToken, null)
                .set(MarketResearchAnalysisRun::getLeaseUntil, null)
                .set(MarketResearchAnalysisRun::getHeartbeatAt, null);
    }
}
