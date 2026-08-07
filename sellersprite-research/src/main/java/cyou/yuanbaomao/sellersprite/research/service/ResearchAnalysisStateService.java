package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchAnalysisCancelledException;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 分析运行的领取、fencing、心跳、重试和终态服务。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchAnalysisStateService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;
    private static final int MAX_BACKOFF_EXPONENT = 20;
    private static final String PARENT_EXECUTION_OWNER_PREFIX = "research-graph:";

    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final MarketResearchJobDao jobDao;
    private final ResearchProperties properties;
    private final ResearchSseEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<String> listDispatchCandidates(int limit) {
        return analysisRunDao.listDispatchCandidates(System.currentTimeMillis(), limit).stream()
                .map(MarketResearchAnalysisRun::getAnalysisRunId)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ResearchAnalysisLease> tryClaim(
            String analysisRunId, String executionOwner, long leaseDurationMs) {
        long now = System.currentTimeMillis();
        String executionToken = UUID.randomUUID().toString();
        if (!analysisRunDao.tryClaim(
                analysisRunId,
                executionOwner,
                executionToken,
                now,
                now + leaseDurationMs)) {
            return Optional.empty();
        }
        MarketResearchAnalysisRun run = requireRun(analysisRunId);
        return Optional.of(new ResearchAnalysisLease(
                run.getAnalysisRunId(),
                run.getJobId(),
                run.getUserId(),
                run.getConversationId(),
                run.getRunType(),
                run.getAnalysisGoal(),
                executionOwner,
                executionToken,
                valueOrZero(run.getAttemptCount()),
                valueOrZero(run.getMaxAttempts())));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ResearchAnalysisLease> tryStartInitial(
            String jobId, String parentExecutionToken) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new IllegalStateException("市场调研任务不存在: " + jobId);
        }
        MarketResearchAnalysisRun run = analysisRunDao
                .findLatestByJobIdAndUserId(jobId, job.getUserId())
                .orElseThrow(() -> new IllegalStateException("市场调研初次分析不存在: " + jobId));
        if (ResearchAnalysisRunStatus.SUCCEEDED.name().equals(run.getRunStatus())) {
            return Optional.empty();
        }
        if (!jobDao.isOwnedRunning(jobId, parentExecutionToken)) {
            throw new IllegalStateException("父Graph执行权已丢失: " + jobId);
        }
        long now = System.currentTimeMillis();
        String executionOwner = PARENT_EXECUTION_OWNER_PREFIX + jobId;
        if (!analysisRunDao.tryStartInitial(
                run.getAnalysisRunId(),
                executionOwner,
                parentExecutionToken,
                now,
                job.getLeaseUntil() == null ? now : job.getLeaseUntil())) {
            return Optional.empty();
        }
        run = requireRun(run.getAnalysisRunId());
        return Optional.of(toLease(run, executionOwner, parentExecutionToken));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ResearchAnalysisLease> tryStartStage(
            String jobId, String parentExecutionToken, ResearchStageCode stage) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new IllegalStateException("市场调研任务不存在: " + jobId);
        }
        MarketResearchAnalysisRun run = analysisRunDao
                .findLatestByJobIdAndUserIdAndRunType(jobId, job.getUserId(), stage.name())
                .orElseThrow(() -> new IllegalStateException(
                        "市场调研阶段分析不存在: " + jobId + "/" + stage));
        if (ResearchAnalysisRunStatus.SUCCEEDED.name().equals(run.getRunStatus())) {
            return Optional.empty();
        }
        if (!jobDao.isOwnedRunning(jobId, parentExecutionToken)) {
            throw new IllegalStateException("父Graph执行权已丢失: " + jobId);
        }
        long now = System.currentTimeMillis();
        String executionOwner = PARENT_EXECUTION_OWNER_PREFIX + jobId + ":" + stage.name();
        if (!analysisRunDao.tryStartInitial(
                run.getAnalysisRunId(),
                executionOwner,
                parentExecutionToken,
                now,
                job.getLeaseUntil() == null ? now : job.getLeaseUntil())) {
            return Optional.empty();
        }
        return Optional.of(toLease(
                requireRun(run.getAnalysisRunId()), executionOwner, parentExecutionToken));
    }

    @Transactional(readOnly = true)
    public boolean isInitialSucceeded(String jobId) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            return false;
        }
        return analysisRunDao.findLatestByJobIdAndUserId(jobId, job.getUserId())
                .map(MarketResearchAnalysisRun::getRunStatus)
                .filter(ResearchAnalysisRunStatus.SUCCEEDED.name()::equals)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isStageSucceeded(String jobId, ResearchStageCode stage) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            return false;
        }
        return analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        jobId, job.getUserId(), stage.name())
                .map(MarketResearchAnalysisRun::getRunStatus)
                .filter(ResearchAnalysisRunStatus.SUCCEEDED.name()::equals)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isStageFailed(String jobId, ResearchStageCode stage) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            return false;
        }
        return analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        jobId, job.getUserId(), stage.name())
                .map(MarketResearchAnalysisRun::getRunStatus)
                .filter(ResearchAnalysisRunStatus.FAILED.name()::equals)
                .isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean heartbeat(ResearchAnalysisLease lease, long leaseDurationMs) {
        long now = System.currentTimeMillis();
        return analysisRunDao.heartbeat(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                now,
                now + leaseDurationMs);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(ResearchAnalysisLease lease, String phase, int progress) {
        if (!analysisRunDao.updateProgress(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                phase,
                progress)) {
            throw new IllegalStateException("市场调研分析执行权已丢失: " + lease.analysisRunId());
        }
    }

    @Transactional(readOnly = true)
    public void assertExecutable(ResearchAnalysisLease lease) {
        MarketResearchAnalysisRun run = requireRun(lease.analysisRunId());
        if (run.getCancelRequestedAt() != null) {
            throw new ResearchAnalysisCancelledException(lease.analysisRunId());
        }
        if (!ResearchAnalysisRunStatus.RUNNING.name().equals(run.getRunStatus())
                || !lease.executionOwner().equals(run.getExecutionOwner())
                || !lease.executionToken().equals(run.getExecutionToken())) {
            throw new IllegalStateException("市场调研分析执行租约已丢失: " + lease.analysisRunId());
        }
        if (isParentManaged(lease.runType())) {
            MarketResearchJob job = jobDao.getById(lease.jobId());
            if (job != null && job.getCancelRequestedAt() != null) {
                throw new ResearchAnalysisCancelledException(lease.analysisRunId());
            }
            if (!jobDao.isOwnedRunning(lease.jobId(), lease.executionToken())) {
                throw new IllegalStateException("父Graph执行权已丢失: " + lease.jobId());
            }
        }
    }

    private boolean isParentManaged(String runType) {
        return ResearchAnalysisRunType.INITIAL.name().equals(runType)
                || ResearchAnalysisRunType.SCREENING.name().equals(runType)
                || ResearchAnalysisRunType.DEEP_DIVE.name().equals(runType)
                || ResearchAnalysisRunType.FINAL_ANALYSIS.name().equals(runType);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementCounters(
            ResearchAnalysisLease lease, int modelCallIncrement, int eventIncrement) {
        if (!analysisRunDao.incrementCounters(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                modelCallIncrement,
                eventIncrement)) {
            throw new IllegalStateException("更新市场调研分析计数器失败: " + lease.analysisRunId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFinalSummary(ResearchAnalysisLease lease, String finalSummary) {
        if (!analysisRunDao.saveFinalSummary(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                finalSummary)) {
            throw new IllegalStateException("市场调研分析执行权已丢失，无法保存最终摘要: "
                    + lease.analysisRunId());
        }
    }

    @Transactional(readOnly = true)
    public Optional<String> findSavedFinalSummary(String analysisRunId) {
        return Optional.ofNullable(requireRun(analysisRunId).getFinalSummary())
                .filter(summary -> !summary.isBlank());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSucceeded(ResearchAnalysisLease lease, String finalSummary) {
        if (!analysisRunDao.markSucceeded(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                finalSummary,
                System.currentTimeMillis())) {
            throw new IllegalStateException("市场调研分析执行权已丢失，不能标记成功: "
                    + lease.analysisRunId());
        }
        publishDone(lease, ResearchAnalysisRunStatus.SUCCEEDED.name(), "市场调研AI分析已完成", "");
        if (isStageRun(lease.runType())) {
            publishStageCompleted(lease);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCancelled(ResearchAnalysisLease lease) {
        if (!analysisRunDao.markCancelled(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                System.currentTimeMillis())) {
            log.warn("分析执行权已丢失，忽略取消终态，analysisRunId={}", lease.analysisRunId());
            return;
        }
        publishDone(lease, ResearchAnalysisRunStatus.CANCELLED.name(), "市场调研AI分析已取消", "");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailure(
            ResearchAnalysisLease lease,
            String errorCode,
            String errorMessage,
            boolean retryable) {
        String safeCode = errorCode == null || errorCode.isBlank()
                ? "ANALYSIS_EXECUTION_FAILED"
                : errorCode;
        String safeMessage = limit(errorMessage);
        if (retryable && lease.attemptCount() < lease.maxAttempts()) {
            long nextRunAt = System.currentTimeMillis() + retryDelay(lease.attemptCount());
            if (analysisRunDao.markRetryWait(
                    lease.analysisRunId(),
                    lease.executionOwner(),
                    lease.executionToken(),
                    nextRunAt,
                    safeCode,
                    safeMessage)) {
                eventPublisher.publish(ResearchEventCommand.builder()
                        .jobId(lease.jobId())
                        .conversationId(lease.conversationId())
                        .analysisRunId(lease.analysisRunId())
                        .scope(ResearchEventScope.ANALYSIS)
                        .eventType(ResearchEventTypes.ANALYSIS_QUEUED)
                        .phase("retry_wait")
                        .message("AI分析失败，已安排自动重试")
                        .payload(Map.of(
                                "analysisStatus", ResearchAnalysisRunStatus.RETRY_WAIT.name(),
                                "nextRunAt", nextRunAt,
                                "errorCode", safeCode,
                                "errorMessage", safeMessage))
                        .build());
                return;
            }
        }
        if (!analysisRunDao.markFailed(
                lease.analysisRunId(),
                lease.executionOwner(),
                lease.executionToken(),
                System.currentTimeMillis(),
                safeCode,
                safeMessage)) {
            log.warn("分析执行权已丢失，忽略失败终态，analysisRunId={}", lease.analysisRunId());
            return;
        }
        publishDone(lease, ResearchAnalysisRunStatus.FAILED.name(), "市场调研AI分析失败", safeMessage);
    }

    private void publishDone(
            ResearchAnalysisLease lease, String status, String message, String errorMessage) {
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .conversationId(lease.conversationId())
                .analysisRunId(lease.analysisRunId())
                .scope(ResearchEventScope.ANALYSIS)
                .eventType(ResearchEventTypes.DONE)
                .phase("completed")
                .message(message)
                .payload(Map.of(
                        "runType", lease.runType(),
                        "stageCode", stageCode(lease.runType()),
                        "analysisStatus", status,
                        "errorMessage", errorMessage))
                .build());
    }

    private void publishStageCompleted(ResearchAnalysisLease lease) {
        String stageCode = stageCode(lease.runType());
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .conversationId(lease.conversationId())
                .analysisRunId(lease.analysisRunId())
                .scope(ResearchEventScope.ANALYSIS)
                .eventType(ResearchEventTypes.STAGE_COMPLETED)
                .phase(stageCode)
                .message(stageCode + "阶段分析已完成")
                .payload(Map.of(
                        "runType", lease.runType(),
                        "stageCode", stageCode,
                        "analysisStatus", ResearchAnalysisRunStatus.SUCCEEDED.name()))
                .build());
    }

    private boolean isStageRun(String runType) {
        return ResearchAnalysisRunType.SCREENING.name().equals(runType)
                || ResearchAnalysisRunType.DEEP_DIVE.name().equals(runType)
                || ResearchAnalysisRunType.FINAL_ANALYSIS.name().equals(runType);
    }

    private String stageCode(String runType) {
        if (ResearchAnalysisRunType.SCREENING.name().equals(runType)) {
            return ResearchStageCode.SCREENING.name();
        }
        if (ResearchAnalysisRunType.DEEP_DIVE.name().equals(runType)) {
            return ResearchStageCode.DEEP_DIVE.name();
        }
        return ResearchStageCode.FINAL_ANALYSIS.name();
    }

    private MarketResearchAnalysisRun requireRun(String analysisRunId) {
        MarketResearchAnalysisRun run = analysisRunDao.getById(analysisRunId);
        if (run == null) {
            throw new IllegalStateException("市场调研分析运行不存在: " + analysisRunId);
        }
        return run;
    }

    private long retryDelay(int attemptCount) {
        int exponent = Math.max(0, Math.min(attemptCount - 1, MAX_BACKOFF_EXPONENT));
        long multiplier = 1L << exponent;
        try {
            return Math.min(
                    Math.multiplyExact(properties.getRetryBaseDelayMs(), multiplier),
                    properties.getRetryMaxDelayMs());
        } catch (ArithmeticException exception) {
            return properties.getRetryMaxDelayMs();
        }
    }

    private String limit(String value) {
        String text = value == null || value.isBlank() ? "市场调研AI分析失败" : value;
        return text.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? text
                : text.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private ResearchAnalysisLease toLease(
            MarketResearchAnalysisRun run, String executionOwner, String executionToken) {
        return new ResearchAnalysisLease(
                run.getAnalysisRunId(),
                run.getJobId(),
                run.getUserId(),
                run.getConversationId(),
                run.getRunType(),
                run.getAnalysisGoal(),
                executionOwner,
                executionToken,
                valueOrZero(run.getAttemptCount()),
                valueOrZero(run.getMaxAttempts()));
    }
}
