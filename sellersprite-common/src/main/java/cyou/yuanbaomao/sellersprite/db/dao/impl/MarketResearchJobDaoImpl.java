package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchJobMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchJobDaoImpl extends ServiceImpl<MarketResearchJobMapper, MarketResearchJob>
        implements MarketResearchJobDao {

    private static final int MIN_DISPATCH_LIMIT = 1;
    private static final int MAX_DISPATCH_LIMIT = 1000;
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_RETRY_WAIT = "RETRY_WAIT";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_WAITING_INPUT = "WAITING_INPUT";
    private static final String STATUS_ABANDONED = "ABANDONED";
    private static final String REPORT_NAME_LITERAL_LIKE =
            "report_name LIKE CONCAT('%', {0}, '%') ESCAPE '!'";
    private static final String KEYWORD_LITERAL_LIKE =
            "keyword LIKE CONCAT('%', {0}, '%') ESCAPE '!'";
    private static final String JOB_ID_LITERAL_LIKE =
            "job_id LIKE CONCAT('%', {0}, '%') ESCAPE '!'";

    @Override
    public Optional<MarketResearchJob> findByIdAndUserId(String jobId, String userId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getUserId, userId)
                .one());
    }

    @Override
    public Page<MarketResearchJob> pageByUserId(
            String userId,
            String keyword,
            String status,
            String marketplace,
            String month,
            long current,
            long size) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String normalizedKeyword = hasKeyword ? escapeLikePattern(keyword.trim()) : null;
        return lambdaQuery()
                .eq(MarketResearchJob::getUserId, userId)
                .and(hasKeyword, search -> search
                        .apply(REPORT_NAME_LITERAL_LIKE, normalizedKeyword)
                        .or()
                        .apply(KEYWORD_LITERAL_LIKE, normalizedKeyword)
                        .or()
                        .apply(JOB_ID_LITERAL_LIKE, normalizedKeyword))
                .eq(status != null && !status.isBlank(), MarketResearchJob::getJobStatus, status)
                .eq(
                        marketplace != null && !marketplace.isBlank(),
                        MarketResearchJob::getMarketplace,
                        marketplace)
                .eq(month != null && !month.isBlank(), MarketResearchJob::getResearchMonth, month)
                .orderByDesc(MarketResearchJob::getCreatedAt)
                .orderByDesc(MarketResearchJob::getJobId)
                .page(Page.of(current, size));
    }

    private static String escapeLikePattern(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    @Override
    public List<MarketResearchJob> listDispatchCandidates(long now, int limit) {
        int safeLimit = Math.max(MIN_DISPATCH_LIMIT, Math.min(limit, MAX_DISPATCH_LIMIT));
        return lambdaQuery()
                .and(candidate -> candidate
                        .and(pending -> pending
                                .in(MarketResearchJob::getJobStatus, STATUS_QUEUED, STATUS_RETRY_WAIT)
                                .le(MarketResearchJob::getNextRunAt, now))
                        .or(expired -> expired
                                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                                .lt(MarketResearchJob::getLeaseUntil, now)))
                .apply("attempt_count < max_attempts")
                .orderByAsc(MarketResearchJob::getNextRunAt)
                .orderByAsc(MarketResearchJob::getCreatedAt)
                .last("LIMIT " + safeLimit)
                .list();
    }

    @Override
    public List<MarketResearchJob> listMarketTrendCacheWarmupCandidates() {
        return lambdaQuery()
                .select(
                        MarketResearchJob::getJobId,
                        MarketResearchJob::getMarketplace,
                        MarketResearchJob::getNodeIdPath,
                        MarketResearchJob::getResearchMonth,
                        MarketResearchJob::getCollectionConfig,
                        MarketResearchJob::getCreatedAt)
                .eq(MarketResearchJob::getDataSourceMode, "REMOTE")
                .in(
                        MarketResearchJob::getJobStatus,
                        STATUS_QUEUED,
                        STATUS_RETRY_WAIT,
                        STATUS_RUNNING)
                .isNotNull(MarketResearchJob::getMarketplace)
                .isNotNull(MarketResearchJob::getNodeIdPath)
                .isNotNull(MarketResearchJob::getResearchMonth)
                .isNotNull(MarketResearchJob::getCollectionConfig)
                .orderByAsc(MarketResearchJob::getCreatedAt)
                .list();
    }

    @Override
    public boolean tryClaim(
            String jobId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .and(candidate -> candidate
                        .and(pending -> pending
                                .in(MarketResearchJob::getJobStatus, STATUS_QUEUED, STATUS_RETRY_WAIT)
                                .le(MarketResearchJob::getNextRunAt, now))
                        .or(expired -> expired
                                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                                .lt(MarketResearchJob::getLeaseUntil, now)))
                .apply("attempt_count < max_attempts")
                .set(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .set(MarketResearchJob::getExecutionOwner, executionOwner)
                .set(MarketResearchJob::getExecutionToken, executionToken)
                .set(MarketResearchJob::getHeartbeatAt, now)
                .set(MarketResearchJob::getLeaseUntil, leaseUntil)
                .set(MarketResearchJob::getNextRunAt, now)
                .setSql("attempt_count = attempt_count + 1, "
                        + "started_at = COALESCE(started_at, " + now + ")")
                .update();
    }

    @Override
    public boolean heartbeat(
            String jobId,
            String executionOwner,
            String executionToken,
            long now,
            long leaseUntil) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .eq(MarketResearchJob::getExecutionOwner, executionOwner)
                .eq(MarketResearchJob::getExecutionToken, executionToken)
                .set(MarketResearchJob::getHeartbeatAt, now)
                .set(MarketResearchJob::getLeaseUntil, leaseUntil)
                .update();
    }

    @Override
    public boolean isOwnedRunning(String jobId, String executionToken) {
        return lambdaQuery()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .eq(MarketResearchJob::getExecutionToken, executionToken)
                .count() == 1L;
    }

    @Override
    public boolean updateProgress(
            String jobId, String executionToken, String currentNode, int progress) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .eq(MarketResearchJob::getExecutionToken, executionToken)
                .set(MarketResearchJob::getCurrentNode, currentNode)
                .set(MarketResearchJob::getProgress, progress)
                .update();
    }

    @Override
    public boolean updateStage(String jobId, String executionToken, String currentStage) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .eq(MarketResearchJob::getExecutionToken, executionToken)
                .set(MarketResearchJob::getCurrentStage, currentStage)
                .update();
    }

    @Override
    public boolean markWaitingInput(
            String jobId,
            String executionOwner,
            String executionToken,
            String currentNode,
            String currentStage,
            String waitingInputType) {
        return terminalUpdate(jobId, executionOwner, executionToken)
                .set(MarketResearchJob::getJobStatus, STATUS_WAITING_INPUT)
                .set(MarketResearchJob::getCurrentNode, currentNode)
                .set(MarketResearchJob::getCurrentStage, currentStage)
                .set(MarketResearchJob::getWaitingInputType, waitingInputType)
                .set(MarketResearchJob::getErrorCode, "")
                .set(MarketResearchJob::getErrorMessage, "")
                .update();
    }

    @Override
    public boolean requeueWaitingInput(
            String jobId, String userId, String waitingInputType, long nextRunAt) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getUserId, userId)
                .eq(MarketResearchJob::getJobStatus, STATUS_WAITING_INPUT)
                .eq(MarketResearchJob::getWaitingInputType, waitingInputType)
                .set(MarketResearchJob::getJobStatus, STATUS_QUEUED)
                .set(MarketResearchJob::getWaitingInputType, null)
                .set(MarketResearchJob::getAttemptCount, 0)
                .set(MarketResearchJob::getNextRunAt, nextRunAt)
                .set(MarketResearchJob::getExecutionOwner, null)
                .set(MarketResearchJob::getExecutionToken, null)
                .set(MarketResearchJob::getLeaseUntil, null)
                .set(MarketResearchJob::getHeartbeatAt, null)
                .update();
    }

    @Override
    public boolean markSucceeded(
            String jobId,
            String executionOwner,
            String executionToken,
            String currentNode,
            long finishedAt) {
        return terminalUpdate(jobId, executionOwner, executionToken)
                .set(MarketResearchJob::getJobStatus, STATUS_SUCCEEDED)
                .set(MarketResearchJob::getCurrentNode, currentNode)
                .set(MarketResearchJob::getCurrentStage, "FINAL_ANALYSIS")
                .set(MarketResearchJob::getWaitingInputType, null)
                .set(MarketResearchJob::getProgress, 100)
                .set(MarketResearchJob::getFinishedAt, finishedAt)
                .set(MarketResearchJob::getErrorCode, "")
                .set(MarketResearchJob::getErrorMessage, "")
                .update();
    }

    @Override
    public boolean markRetryWait(
            String jobId,
            String executionOwner,
            String executionToken,
            long nextRunAt,
            String errorCode,
            String errorMessage) {
        return terminalUpdate(jobId, executionOwner, executionToken)
                .set(MarketResearchJob::getJobStatus, STATUS_RETRY_WAIT)
                .set(MarketResearchJob::getNextRunAt, nextRunAt)
                .set(MarketResearchJob::getErrorCode, errorCode)
                .set(MarketResearchJob::getErrorMessage, errorMessage)
                .update();
    }

    @Override
    public boolean markFailed(
            String jobId,
            String executionOwner,
            String executionToken,
            long finishedAt,
            String errorCode,
            String errorMessage) {
        return terminalUpdate(jobId, executionOwner, executionToken)
                .set(MarketResearchJob::getJobStatus, STATUS_FAILED)
                .set(MarketResearchJob::getFinishedAt, finishedAt)
                .set(MarketResearchJob::getErrorCode, errorCode)
                .set(MarketResearchJob::getErrorMessage, errorMessage)
                .update();
    }

    @Override
    public boolean markCancelled(
            String jobId, String executionOwner, String executionToken, long finishedAt) {
        return terminalUpdate(jobId, executionOwner, executionToken)
                .set(MarketResearchJob::getJobStatus, STATUS_CANCELLED)
                .set(MarketResearchJob::getFinishedAt, finishedAt)
                .set(MarketResearchJob::getErrorCode, "")
                .set(MarketResearchJob::getErrorMessage, "")
                .update();
    }

    @Override
    public boolean markAbandoned(
            String jobId,
            String executionOwner,
            String executionToken,
            String currentNode,
            long finishedAt) {
        return terminalUpdate(jobId, executionOwner, executionToken)
                .set(MarketResearchJob::getJobStatus, STATUS_ABANDONED)
                .set(MarketResearchJob::getCurrentNode, currentNode)
                .set(MarketResearchJob::getWaitingInputType, null)
                .set(MarketResearchJob::getFinishedAt, finishedAt)
                .set(MarketResearchJob::getErrorCode, "")
                .set(MarketResearchJob::getErrorMessage, "")
                .update();
    }

    @Override
    public boolean cancelPending(String jobId, String userId, long cancelledAt) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getUserId, userId)
                .in(MarketResearchJob::getJobStatus, STATUS_QUEUED, STATUS_RETRY_WAIT)
                .set(MarketResearchJob::getJobStatus, STATUS_CANCELLED)
                .set(MarketResearchJob::getCancelRequestedAt, cancelledAt)
                .set(MarketResearchJob::getFinishedAt, cancelledAt)
                .set(MarketResearchJob::getExecutionOwner, null)
                .set(MarketResearchJob::getExecutionToken, null)
                .set(MarketResearchJob::getLeaseUntil, null)
                .set(MarketResearchJob::getHeartbeatAt, null)
                .update();
    }

    @Override
    public boolean requestRunningCancel(String jobId, String userId, long requestedAt) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getUserId, userId)
                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .isNull(MarketResearchJob::getCancelRequestedAt)
                .set(MarketResearchJob::getCancelRequestedAt, requestedAt)
                .update();
    }

    @Override
    public boolean retryFailed(String jobId, String userId, long nextRunAt) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getUserId, userId)
                .eq(MarketResearchJob::getJobStatus, STATUS_FAILED)
                .set(MarketResearchJob::getJobStatus, STATUS_QUEUED)
                .set(MarketResearchJob::getAttemptCount, 0)
                .set(MarketResearchJob::getNextRunAt, nextRunAt)
                .set(MarketResearchJob::getExecutionOwner, null)
                .set(MarketResearchJob::getExecutionToken, null)
                .set(MarketResearchJob::getLeaseUntil, null)
                .set(MarketResearchJob::getHeartbeatAt, null)
                .set(MarketResearchJob::getCancelRequestedAt, null)
                .set(MarketResearchJob::getFinishedAt, null)
                .set(MarketResearchJob::getErrorCode, "")
                .set(MarketResearchJob::getErrorMessage, "")
                .update();
    }

    private com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper<MarketResearchJob>
            terminalUpdate(String jobId, String executionOwner, String executionToken) {
        return lambdaUpdate()
                .eq(MarketResearchJob::getJobId, jobId)
                .eq(MarketResearchJob::getJobStatus, STATUS_RUNNING)
                .eq(MarketResearchJob::getExecutionOwner, executionOwner)
                .eq(MarketResearchJob::getExecutionToken, executionToken)
                .set(MarketResearchJob::getExecutionOwner, null)
                .set(MarketResearchJob::getExecutionToken, null)
                .set(MarketResearchJob::getLeaseUntil, null)
                .set(MarketResearchJob::getHeartbeatAt, null);
    }
}
