package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchNodeExecutionDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchArtifactVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchNodeExecutionVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamStateVo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 构造供REST详情与SSE聚合帧共同使用的权威任务状态。 */
@Service
@RequiredArgsConstructor
public class ResearchStreamSnapshotService {

    private final MarketResearchJobDao jobDao;
    private final MarketResearchArtifactDao artifactDao;
    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final MarketResearchNodeExecutionDao nodeExecutionDao;
    private final ResearchInputService inputService;

    @Transactional(readOnly = true)
    public ResearchStreamStateVo requireOwnedSnapshot(String jobId, String userId) {
        MarketResearchJob job = jobDao.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND));
        return toSnapshot(job);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ResearchStreamStateVo snapshot(String jobId) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND);
        }
        return toSnapshot(job);
    }

    private ResearchStreamStateVo toSnapshot(MarketResearchJob job) {
        List<MarketResearchArtifact> artifacts = artifactDao.listAvailableByJobIds(List.of(job.getJobId()));
        MarketResearchAnalysisRun analysis = analysisRunDao
                .findLatestByJobIdAndUserId(job.getJobId(), job.getUserId())
                .orElse(null);
        List<ResearchNodeExecutionVo> nodes = nodeExecutionDao.listByJobId(job.getJobId()).stream()
                .map(this::toNodeVo)
                .toList();
        return ResearchStreamStateVo.builder()
                .job(toJobVo(job, analysis, artifacts))
                .nodes(nodes)
                .build();
    }

    private ResearchJobDetailVo toJobVo(
            MarketResearchJob job,
            MarketResearchAnalysisRun analysis,
            List<MarketResearchArtifact> artifacts) {
        ResearchInput input = inputService.from(job);
        String nodeName = nodeName(job.getCurrentNode());
        int attemptCount = valueOrZero(job.getAttemptCount());
        int maxAttempts = valueOrZero(job.getMaxAttempts());
        ResearchJobStatus status = ResearchJobStatus.valueOf(job.getJobStatus());
        return ResearchJobDetailVo.builder()
                .jobId(job.getJobId())
                .reportName(job.getReportName())
                .marketplace(job.getMarketplace())
                .nodeIdPath(job.getNodeIdPath())
                .month(job.getResearchMonth())
                .keyword(job.getKeyword())
                .seedAsins(input.getSeedAsins())
                .collectionConfig(input.getCollectionConfig())
                .dataSourceMode(job.getDataSourceMode())
                .workflowVersion(job.getWorkflowVersion())
                .status(job.getJobStatus())
                .currentNode(job.getCurrentNode())
                .currentNodeName(nodeName)
                .currentStage(job.getCurrentStage())
                .waitingInputType(job.getWaitingInputType())
                .progress(job.getProgress())
                .attemptCount(attemptCount)
                .maxAttempts(maxAttempts)
                .remainingAttempts(Math.max(0, maxAttempts - attemptCount))
                .nextRunAt(job.getNextRunAt())
                .leaseUntil(job.getLeaseUntil())
                .heartbeatAt(job.getHeartbeatAt())
                .cancelRequestedAt(job.getCancelRequestedAt())
                .cancellable(status == ResearchJobStatus.QUEUED
                        || status == ResearchJobStatus.RUNNING
                        || status == ResearchJobStatus.RETRY_WAIT)
                .retryable(status == ResearchJobStatus.FAILED)
                .errorCode(job.getErrorCode())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .conversationId(analysis == null ? null : analysis.getConversationId())
                .analysisRunId(analysis == null ? null : analysis.getAnalysisRunId())
                .analysisStatus(analysis == null ? null : analysis.getRunStatus())
                .analysisPhase(analysis == null ? null : analysis.getCurrentPhase())
                .analysisProgress(analysis == null ? null : analysis.getProgress())
                .analysisGoal(analysis == null ? null : analysis.getAnalysisGoal())
                .artifacts(artifacts.stream().map(this::toArtifactVo).toList())
                .build();
    }

    private ResearchArtifactVo toArtifactVo(MarketResearchArtifact artifact) {
        return ResearchArtifactVo.builder()
                .artifactId(artifact.getArtifactId())
                .analysisRunId(artifact.getAnalysisRunId())
                .artifactType(artifact.getArtifactType())
                .fileName(artifact.getFileName())
                .mediaType(artifact.getMediaType())
                .fileSize(artifact.getFileSize())
                .createdAt(artifact.getCreatedAt())
                .build();
    }

    private ResearchNodeExecutionVo toNodeVo(MarketResearchNodeExecution execution) {
        return ResearchNodeExecutionVo.builder()
                .executionId(execution.getExecutionId())
                .graphCode(execution.getGraphCode())
                .nodeCode(execution.getNodeCode())
                .nodeName(execution.getNodeName())
                .jobAttempt(execution.getJobAttempt())
                .nodeAttempt(execution.getNodeAttempt())
                .status(execution.getExecutionStatus())
                .startedAt(execution.getStartedAt())
                .finishedAt(execution.getFinishedAt())
                .durationMs(execution.getDurationMs())
                .errorCode(execution.getErrorCode())
                .errorMessage(execution.getErrorMessage())
                .build();
    }

    private String nodeName(String nodeCode) {
        if (nodeCode == null || nodeCode.isBlank()) {
            return null;
        }
        for (ResearchPhase phase : ResearchPhase.values()) {
            if (phase.getNodeCode().equals(nodeCode)) {
                return phase.getDisplayName();
            }
        }
        return nodeCode;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
