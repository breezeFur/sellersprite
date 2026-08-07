package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchNodeExecutionDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchNodeExecutionStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 记录每个固定Graph节点的每次执行尝试。 */
@Service
@RequiredArgsConstructor
public class ResearchNodeExecutionService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;

    private final MarketResearchNodeExecutionDao executionDao;
    private final IdGenerator idGenerator;
    private final ResearchSseEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MarketResearchNodeExecution begin(MarketResearchJob job, ResearchPhase phase) {
        int jobAttempt = job.getAttemptCount() == null ? 0 : job.getAttemptCount();
        MarketResearchNodeExecution execution = new MarketResearchNodeExecution();
        execution.setExecutionId(idGenerator.nextId());
        execution.setJobId(job.getJobId());
        execution.setGraphCode(phase.getGraphCode().getCode());
        execution.setNodeCode(phase.getNodeCode());
        execution.setNodeName(phase.getDisplayName());
        execution.setJobAttempt(jobAttempt);
        execution.setNodeAttempt(executionDao.nextNodeAttempt(
                job.getJobId(), phase.getNodeCode(), jobAttempt));
        execution.setExecutionStatus(ResearchNodeExecutionStatus.RUNNING.name());
        execution.setStartedAt(System.currentTimeMillis());
        execution.setErrorCode("");
        execution.setErrorMessage("");
        if (!executionDao.save(execution)) {
            throw new IllegalStateException("创建市场调研节点执行记录失败: " + phase.getNodeCode());
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(job.getJobId())
                .scope(ResearchEventScope.RESEARCH)
                .eventType(ResearchEventTypes.RESEARCH_NODE_STARTED)
                .phase(phase.getNodeCode())
                .nodeCode(phase.getNodeCode())
                .message("开始执行：" + phase.getDisplayName())
                .payload(Map.of(
                        "nodeName", phase.getDisplayName(),
                        "graphCode", phase.getGraphCode().getCode(),
                        "jobAttempt", jobAttempt,
                        "nodeAttempt", execution.getNodeAttempt(),
                        "progress", phase.getStartProgress()))
                .build());
        return execution;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSucceeded(MarketResearchNodeExecution execution) {
        finish(execution, ResearchNodeExecutionStatus.SUCCEEDED, "", "");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCancelled(MarketResearchNodeExecution execution, Throwable cause) {
        finish(execution, ResearchNodeExecutionStatus.CANCELLED,
                ResearchConstants.ERROR_CODE_CANCELLED, safeMessage(cause));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(MarketResearchNodeExecution execution, Throwable cause) {
        String errorCode = cause instanceof IllegalArgumentException
                ? ResearchConstants.ERROR_CODE_VALIDATION_FAILED
                : ResearchConstants.ERROR_CODE_EXECUTION_FAILED;
        finish(execution, ResearchNodeExecutionStatus.FAILED, errorCode, safeMessage(cause));
    }

    @Transactional(readOnly = true)
    public List<MarketResearchNodeExecution> listByJobId(String jobId) {
        return executionDao.listByJobId(jobId);
    }

    private void finish(
            MarketResearchNodeExecution execution,
            ResearchNodeExecutionStatus status,
            String errorCode,
            String errorMessage) {
        long finishedAt = System.currentTimeMillis();
        execution.setExecutionStatus(status.name());
        execution.setFinishedAt(finishedAt);
        execution.setDurationMs(Math.max(0L, finishedAt - execution.getStartedAt()));
        execution.setErrorCode(errorCode);
        execution.setErrorMessage(errorMessage);
        if (!executionDao.updateById(execution)) {
            throw new IllegalStateException("更新市场调研节点执行记录失败: " + execution.getExecutionId());
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(execution.getJobId())
                .scope(ResearchEventScope.RESEARCH)
                .eventType(eventType(status))
                .phase(execution.getNodeCode())
                .nodeCode(execution.getNodeCode())
                .message(eventMessage(execution, status, errorMessage))
                .payload(Map.of(
                        "nodeName", execution.getNodeName(),
                        "graphCode", execution.getGraphCode(),
                        "status", status.name(),
                        "durationMs", execution.getDurationMs(),
                        "errorCode", errorCode == null ? "" : errorCode,
                        "errorMessage", errorMessage == null ? "" : errorMessage))
                .build());
    }

    private String eventType(ResearchNodeExecutionStatus status) {
        return switch (status) {
            case SUCCEEDED -> ResearchEventTypes.RESEARCH_NODE_COMPLETED;
            case FAILED -> ResearchEventTypes.RESEARCH_NODE_FAILED;
            case CANCELLED -> ResearchEventTypes.RESEARCH_NODE_CANCELLED;
            case RUNNING -> ResearchEventTypes.RESEARCH_NODE_STARTED;
        };
    }

    private String eventMessage(
            MarketResearchNodeExecution execution,
            ResearchNodeExecutionStatus status,
            String errorMessage) {
        return switch (status) {
            case SUCCEEDED -> "执行完成：" + execution.getNodeName();
            case CANCELLED -> "执行已取消：" + execution.getNodeName();
            case FAILED -> "执行失败：" + execution.getNodeName()
                    + (errorMessage == null || errorMessage.isBlank() ? "" : "（" + errorMessage + "）");
            case RUNNING -> "开始执行：" + execution.getNodeName();
        };
    }

    private String safeMessage(Throwable cause) {
        String message = cause == null ? "" : cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause == null ? "" : cause.getClass().getSimpleName();
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
