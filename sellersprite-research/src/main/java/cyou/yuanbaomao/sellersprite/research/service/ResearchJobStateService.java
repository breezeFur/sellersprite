package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchWaitingInputType;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchJobCancelledException;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 管理市场调研任务调度租约、进度、终态、重试和取消状态。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchJobStateService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;
    private static final int MAX_BACKOFF_EXPONENT = 20;
    private static final int PRODUCT_CANDIDATE_LIMIT = 20;
    private static final String PRODUCT_SELECTION_GATE_NODE = "productSelectionGate";
    private static final String PRODUCT_CANDIDATES_DATASET_CODE = "selection.productCandidates";

    private final MarketResearchJobDao jobDao;
    private final ResearchProperties properties;
    private final ResearchSseEventPublisher eventPublisher;
    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final ResearchStageInputService stageInputService;

    @Transactional(readOnly = true)
    public MarketResearchJob requireJob(String jobId) {
        return Objects.requireNonNull(jobDao.getById(jobId), "市场调研任务不存在: " + jobId);
    }

    @Transactional(readOnly = true)
    public List<String> listDispatchCandidates() {
        return jobDao.listDispatchCandidates(System.currentTimeMillis(), properties.getDispatchBatchSize())
                .stream()
                .map(MarketResearchJob::getJobId)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ResearchExecutionLease> tryClaim(String jobId, String executionOwner) {
        long now = System.currentTimeMillis();
        long leaseUntil = now + properties.getLeaseDurationMs();
        String executionToken = UUID.randomUUID().toString();
        if (!jobDao.tryClaim(jobId, executionOwner, executionToken, now, leaseUntil)) {
            return Optional.empty();
        }
        MarketResearchJob claimed = requireJob(jobId);
        return Optional.of(new ResearchExecutionLease(
                claimed.getJobId(),
                claimed.getWorkflowVersion(),
                executionOwner,
                executionToken,
                claimed.getAttemptCount()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean heartbeat(ResearchExecutionLease lease) {
        long now = System.currentTimeMillis();
        return jobDao.heartbeat(
                lease.jobId(),
                lease.executionOwner(),
                lease.executionToken(),
                now,
                now + properties.getLeaseDurationMs());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enter(String jobId, String executionToken, ResearchPhase phase) {
        if (!jobDao.updateProgress(jobId, executionToken, phase.getNodeCode(), phase.getStartProgress())) {
            throw new IllegalStateException("进入市场调研节点失败: " + jobId + "/" + phase.getNodeCode());
        }
        publishProgress(jobId, phase, phase.getStartProgress());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void advance(String jobId, String executionToken, ResearchPhase phase) {
        if (!jobDao.updateProgress(jobId, executionToken, phase.getNodeCode(), phase.getProgress())) {
            throw new IllegalStateException("更新市场调研节点进度失败: " + jobId + "/" + phase.getNodeCode());
        }
        publishProgress(jobId, phase, phase.getProgress());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveToStage(ResearchExecutionLease lease, ResearchStageCode stage) {
        if (!jobDao.updateStage(lease.jobId(), lease.executionToken(), stage.name())) {
            throw new IllegalStateException("更新市场调研阶段失败: " + lease.jobId() + "/" + stage);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWaitingInput(ResearchExecutionLease lease) {
        stageInputService.prepareProductCandidates(lease.jobId());
        if (!jobDao.markWaitingInput(
                lease.jobId(),
                lease.executionOwner(),
                lease.executionToken(),
                PRODUCT_SELECTION_GATE_NODE,
                ResearchStageCode.SCREENING.name(),
                ResearchWaitingInputType.PRODUCT_SELECTION.name())) {
            throw new IllegalStateException("市场调研任务执行权已丢失，不能等待人工输入: "
                    + lease.jobId());
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .scope(ResearchEventScope.WORKFLOW)
                .eventType(ResearchEventTypes.PRODUCT_SELECTION_REQUIRED)
                .phase(ResearchStageCode.SCREENING.name())
                .nodeCode(PRODUCT_SELECTION_GATE_NODE)
                .message("阶段一已完成，请从Top20商品中选择1到20个进入阶段二")
                .payload(Map.of(
                        "stageCode", ResearchStageCode.SCREENING.name(),
                        "candidateDatasetCode", PRODUCT_CANDIDATES_DATASET_CODE,
                        "candidateLimit", PRODUCT_CANDIDATE_LIMIT,
                        "waitingInputType", ResearchWaitingInputType.PRODUCT_SELECTION.name()))
                .build());
    }

    @Transactional(readOnly = true)
    public void assertExecutable(String jobId, String executionToken) {
        MarketResearchJob job = requireJob(jobId);
        if (job.getCancelRequestedAt() != null) {
            throw new ResearchJobCancelledException(jobId);
        }
        if (!jobDao.isOwnedRunning(jobId, executionToken)) {
            throw new IllegalStateException("市场调研任务执行租约已丢失: " + jobId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSucceeded(ResearchExecutionLease lease) {
        String terminalNode = "finalizeArtifacts";
        boolean updated = jobDao.markSucceeded(
                lease.jobId(),
                lease.executionOwner(),
                lease.executionToken(),
                terminalNode,
                System.currentTimeMillis());
        if (!updated) {
            throw new IllegalStateException("市场调研任务执行权已丢失，不能标记成功: " + lease.jobId());
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .scope(ResearchEventScope.RESEARCH)
                .eventType(ResearchEventTypes.RESEARCH_COMPLETED)
                .phase(ResearchStageCode.FINAL_ANALYSIS.name())
                .nodeCode(terminalNode)
                .message("市场调研三个阶段及五个终态文件已完成")
                .payload(Map.of(
                        "dataStatus", "SUCCEEDED",
                        "analysisStatus", "SUCCEEDED",
                        "stageCode", ResearchStageCode.FINAL_ANALYSIS.name(),
                        "artifactCount", 5))
                .build());
        MarketResearchJob job = requireJob(lease.jobId());
        MarketResearchAnalysisRun run = analysisRunDao
                .findLatestByJobIdAndUserIdAndRunType(
                        lease.jobId(),
                        job.getUserId(),
                        ResearchAnalysisRunType.FINAL_ANALYSIS.name())
                .orElseThrow(() -> new IllegalStateException(
                        "市场调研成功但最终分析记录不存在: " + lease.jobId()));
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .conversationId(run.getConversationId())
                .analysisRunId(run.getAnalysisRunId())
                .scope(ResearchEventScope.WORKFLOW)
                .eventType(ResearchEventTypes.WORKFLOW_COMPLETED)
                .phase("completed")
                .nodeCode(terminalNode)
                .message("阶段一和阶段二数据及证据工作簿、最终分析报告均已完成")
                .payload(Map.of(
                        "dataStatus", "SUCCEEDED",
                        "analysisStatus", run.getRunStatus(),
                        "stageCode", ResearchStageCode.FINAL_ANALYSIS.name(),
                        "artifactCount", 5))
                .terminal(true)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCancelled(ResearchExecutionLease lease) {
        if (!jobDao.markCancelled(
                lease.jobId(),
                lease.executionOwner(),
                lease.executionToken(),
                System.currentTimeMillis())) {
            log.warn("市场调研任务执行权已丢失，忽略取消终态写入，jobId={}，owner={}",
                    lease.jobId(), lease.executionOwner());
            return;
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .scope(ResearchEventScope.WORKFLOW)
                .eventType(ResearchEventTypes.WORKFLOW_CANCELLED)
                .message("市场调研任务已取消")
                .payload(Map.of("dataStatus", "CANCELLED"))
                .terminal(true)
                .build());
        analysisRunDao.cancelWaitingByJobId(lease.jobId(), System.currentTimeMillis());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAbandoned(ResearchExecutionLease lease) {
        String terminalNode = "finalizeArtifacts";
        long now = System.currentTimeMillis();
        if (!jobDao.markAbandoned(
                lease.jobId(),
                lease.executionOwner(),
                lease.executionToken(),
                terminalNode,
                now)) {
            throw new IllegalStateException("市场调研任务执行权已丢失，不能标记放弃: " + lease.jobId());
        }
        analysisRunDao.cancelWaitingByJobId(lease.jobId(), now);
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(lease.jobId())
                .scope(ResearchEventScope.WORKFLOW)
                .eventType(ResearchEventTypes.MARKET_ABANDONED)
                .phase(ResearchStageCode.SCREENING.name())
                .nodeCode(terminalNode)
                .message("已放弃进入该市场，阶段一文件已生成")
                .payload(Map.of(
                        "decision", "ABANDON",
                        "dataStatus", "ABANDONED",
                        "stageCode", ResearchStageCode.SCREENING.name()))
                .terminal(true)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleExecutionFailure(ResearchExecutionLease lease, Throwable cause) {
        MarketResearchJob job = requireJob(lease.jobId());
        String errorCode = containsIllegalArgument(cause)
                ? ResearchConstants.ERROR_CODE_VALIDATION_FAILED
                : ResearchConstants.ERROR_CODE_EXECUTION_FAILED;
        String errorMessage = safeMessage(cause);
        boolean retryable = !containsIllegalArgument(cause)
                && job.getAttemptCount() < job.getMaxAttempts();
        boolean updated;
        if (retryable) {
            long nextRunAt = System.currentTimeMillis() + retryDelay(job.getAttemptCount());
            updated = jobDao.markRetryWait(
                    lease.jobId(),
                    lease.executionOwner(),
                    lease.executionToken(),
                    nextRunAt,
                    errorCode,
                    errorMessage);
            if (updated) {
                eventPublisher.publish(ResearchEventCommand.builder()
                        .jobId(lease.jobId())
                        .scope(ResearchEventScope.RESEARCH)
                        .eventType(ResearchEventTypes.RESEARCH_RETRY_SCHEDULED)
                        .phase(job.getCurrentNode())
                        .nodeCode(job.getCurrentNode())
                        .message("市场调研 Graph 执行失败，已安排自动重试")
                        .payload(Map.of(
                                "attemptCount", job.getAttemptCount(),
                                "maxAttempts", job.getMaxAttempts(),
                                "nextRunAt", nextRunAt,
                                "errorCode", errorCode,
                                "errorMessage", errorMessage))
                        .build());
                log.warn("市场调研任务等待自动重试，jobId={}，attempt={}/{}，nextRunAt={}",
                        lease.jobId(), job.getAttemptCount(), job.getMaxAttempts(), nextRunAt, cause);
            }
        } else {
            updated = jobDao.markFailed(
                    lease.jobId(),
                    lease.executionOwner(),
                    lease.executionToken(),
                    System.currentTimeMillis(),
                    errorCode,
                    errorMessage);
            if (updated) {
                analysisRunDao.failWaitingByJobId(
                        lease.jobId(), System.currentTimeMillis(), errorCode, errorMessage);
                eventPublisher.publish(ResearchEventCommand.builder()
                        .jobId(lease.jobId())
                        .scope(ResearchEventScope.WORKFLOW)
                        .eventType(ResearchEventTypes.WORKFLOW_FAILED)
                        .phase(job.getCurrentNode())
                        .nodeCode(job.getCurrentNode())
                        .message("市场调研任务失败：" + errorMessage)
                        .payload(Map.of(
                                "dataStatus", "FAILED",
                                "errorCode", errorCode,
                                "errorMessage", errorMessage))
                        .terminal(true)
                        .build());
                log.warn("市场调研任务执行失败，jobId={}，attempt={}/{}",
                        lease.jobId(), job.getAttemptCount(), job.getMaxAttempts(), cause);
            }
        }
        if (!updated) {
            log.warn("市场调研任务执行权已丢失，忽略失败状态写入，jobId={}，owner={}",
                    lease.jobId(), lease.executionOwner());
        }
    }

    private void publishProgress(String jobId, ResearchPhase phase, int progress) {
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(jobId)
                .scope(ResearchEventScope.RESEARCH)
                .eventType(ResearchEventTypes.RESEARCH_NODE_PROGRESS)
                .phase(phase.getNodeCode())
                .nodeCode(phase.getNodeCode())
                .message(phase.getDisplayName() + "，进度 " + progress + "%")
                .payload(Map.of(
                        "graphCode", phase.getGraphCode().getCode(),
                        "nodeName", phase.getDisplayName(),
                        "progress", progress))
                .build());
    }

    private long retryDelay(int attemptCount) {
        int exponent = Math.max(0, Math.min(attemptCount - 1, MAX_BACKOFF_EXPONENT));
        long multiplier = 1L << exponent;
        long delay;
        try {
            delay = Math.multiplyExact(properties.getRetryBaseDelayMs(), multiplier);
        } catch (ArithmeticException exception) {
            delay = properties.getRetryMaxDelayMs();
        }
        return Math.min(delay, properties.getRetryMaxDelayMs());
    }

    private boolean containsIllegalArgument(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof IllegalArgumentException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String safeMessage(Throwable cause) {
        String message = cause == null ? "市场调研任务执行失败" : cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause == null ? "市场调研任务执行失败" : cause.getClass().getSimpleName();
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
