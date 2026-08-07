package cyou.yuanbaomao.sellersprite.research.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.event.ResearchJobCreatedEvent;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDownload;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobPageRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchAnalysisRunVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchArtifactVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobHistoryVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchNodeExecutionVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchInputService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchNodeExecutionService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSseEventPublisher;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import cyou.yuanbaomao.sellersprite.research.support.ResearchMonthUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ResearchJobServiceImpl implements ResearchJobService {

    private static final Comparator<MarketResearchAnalysisRun> ANALYSIS_RUN_RECENCY = Comparator
            .comparing(
                    MarketResearchAnalysisRun::getCreatedAt,
                    Comparator.nullsFirst(Long::compareTo))
            .thenComparing(
                    MarketResearchAnalysisRun::getAnalysisRunId,
                    Comparator.nullsFirst(String::compareTo));

    private final MarketResearchJobDao jobDao;
    private final MarketResearchArtifactDao artifactDao;
    private final ResearchNodeExecutionService nodeExecutionService;
    private final ResearchInputService inputService;
    private final ResearchProperties properties;
    private final ReportStorage reportStorage;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ResearchSseEventPublisher sseEventPublisher;
    private final ResearchAnalysisService analysisService;
    private final MarketResearchAnalysisRunDao analysisRunDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResearchJobCreatedVo create(ResearchJobCreateRequest request) {
        String userId = currentUserId();
        List<String> seedAsins = normalizeAsins(request.getSeedAsins());
        long now = System.currentTimeMillis();
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(idGenerator.nextId());
        job.setUserId(userId);
        job.setReportName(requireText(request.getReportName(), "报告名称"));
        job.setMarketplace(Objects.requireNonNull(request.getMarketplace(), "市场不能为空").getCode());
        job.setNodeIdPath(requireText(request.getNodeIdPath(), "类目节点路径"));
        job.setResearchMonth(ResearchMonthUtils.normalize(request.getMonth()));
        job.setKeyword(normalizeOptional(request.getKeyword()));
        job.setSeedAsins(writeJson(seedAsins));
        job.setCollectionConfig(writeJson(Objects.requireNonNull(
                request.getCollectionConfig(), "采集配置不能为空")));
        job.setTemplateCode(ResearchConstants.TEMPLATE_CODE);
        job.setDataSourceMode(properties.getSourceMode().name());
        job.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        job.setJobStatus(ResearchJobStatus.QUEUED.name());
        job.setCurrentNode(ResearchPhase.VALIDATE.getNodeCode());
        job.setCurrentStage(ResearchStageCode.SCREENING.name());
        job.setWaitingInputType(null);
        job.setProgress(0);
        job.setAttemptCount(0);
        job.setMaxAttempts(properties.getMaxAttempts());
        job.setNextRunAt(now);
        job.setErrorCode("");
        job.setErrorMessage("");
        if (!jobDao.save(job)) {
            throw new IllegalStateException("保存市场调研任务失败");
        }
        ResearchAnalysisRunVo analysis = analysisService.createInitial(
                job,
                request.getAnalysisGoal());
        sseEventPublisher.publish(ResearchEventCommand.builder()
                .jobId(job.getJobId())
                .conversationId(analysis.getConversationId())
                .analysisRunId(analysis.getAnalysisRunId())
                .scope(ResearchEventScope.WORKFLOW)
                .eventType(ResearchEventTypes.WORKFLOW_STARTED)
                .phase(job.getCurrentNode())
                .nodeCode(job.getCurrentNode())
                .message("市场调研任务已创建，等待数据 Graph 执行")
                .payload(Map.of(
                        "status", job.getJobStatus(),
                        "workflowVersion", job.getWorkflowVersion(),
                        "dataSourceMode", job.getDataSourceMode(),
                        "analysisStatus", analysis.getStatus()))
                .build());
        eventPublisher.publishEvent(new ResearchJobCreatedEvent(job.getJobId()));
        return ResearchJobCreatedVo.builder()
                .jobId(job.getJobId())
                .status(job.getJobStatus())
                .dataSourceMode(job.getDataSourceMode())
                .workflowVersion(job.getWorkflowVersion())
                .conversationId(analysis.getConversationId())
                .analysisRunId(analysis.getAnalysisRunId())
                .analysisStatus(analysis.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ResearchJobHistoryVo> page(ResearchJobPageRequest request) {
        String userId = currentUserId();
        Page<MarketResearchJob> page = jobDao.pageByUserId(
                userId,
                normalizeOptional(request.getKeyword()),
                request.getStatus() == null ? null : request.getStatus().name(),
                request.getMarketplace() == null ? null : request.getMarketplace().getCode(),
                normalizeOptional(request.getMonth()),
                request.getCurrent(),
                request.getSize());
        if (page.getRecords().isEmpty()) {
            return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), List.of());
        }

        List<String> jobIds = page.getRecords().stream()
                .map(MarketResearchJob::getJobId)
                .toList();
        Map<String, MarketResearchAnalysisRun> latestAnalysisRuns = latestAnalysisRuns(
                analysisRunDao.listByJobIdsAndUserId(jobIds, userId));
        Map<String, List<ResearchArtifactVo>> artifactsByJob = artifactsByJob(
                artifactDao.listAvailableByJobIds(jobIds));
        List<ResearchJobHistoryVo> records = page.getRecords().stream()
                .map(job -> toHistoryVo(
                        job,
                        latestAnalysisRuns.get(job.getJobId()),
                        artifactsByJob.getOrDefault(job.getJobId(), List.of())))
                .toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public ResearchJobDetailVo detail(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        ResearchInput input = inputService.from(job);
        List<ResearchArtifactVo> artifacts = artifactDao
                .listAvailableByJobIds(List.of(jobId)).stream()
                .map(this::toArtifactVo)
                .toList();
        ResearchAnalysisRunVo analysis = analysisService.toVo(
                analysisService.findLatest(jobId, job.getUserId()));
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
                .retryable(status == ResearchJobStatus.FAILED
                        && ResearchConstants.WORKFLOW_VERSION.equals(job.getWorkflowVersion()))
                .errorCode(job.getErrorCode())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .conversationId(analysis == null ? null : analysis.getConversationId())
                .analysisRunId(analysis == null ? null : analysis.getAnalysisRunId())
                .analysisStatus(analysis == null ? null : analysis.getStatus())
                .analysisPhase(analysis == null ? null : analysis.getCurrentPhase())
                .analysisProgress(analysis == null ? null : analysis.getProgress())
                .analysisGoal(analysis == null ? null : analysis.getAnalysisGoal())
                .artifacts(artifacts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResearchNodeExecutionVo> nodes(String jobId) {
        requireOwnedJob(jobId);
        return nodeExecutionService.listByJobId(jobId).stream()
                .map(this::toNodeVo)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        long now = System.currentTimeMillis();
        if (jobDao.cancelPending(jobId, job.getUserId(), now)) {
            analysisRunDao.cancelWaitingByJobId(jobId, now);
            sseEventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(jobId)
                    .scope(ResearchEventScope.WORKFLOW)
                    .eventType(ResearchEventTypes.WORKFLOW_CANCELLED)
                    .phase(job.getCurrentNode())
                    .nodeCode(job.getCurrentNode())
                    .message("市场调研任务已取消")
                    .payload(Map.of("dataStatus", ResearchJobStatus.CANCELLED.name()))
                    .terminal(true)
                    .build());
            return;
        }
        if (jobDao.requestRunningCancel(jobId, job.getUserId(), now)) {
            sseEventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(jobId)
                    .scope(ResearchEventScope.WORKFLOW)
                    .eventType(ResearchEventTypes.ANALYSIS_CANCEL_REQUESTED)
                    .phase(job.getCurrentNode())
                    .nodeCode(job.getCurrentNode())
                    .message("已请求取消市场调研，当前步骤将在安全边界停止")
                    .payload(Map.of("requestedAt", now))
                    .build());
            return;
        }
        throw new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_CANCELLABLE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        if (!jobDao.retryFailed(jobId, job.getUserId(), System.currentTimeMillis())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_RETRYABLE);
        }
        ResearchAnalysisRunVo analysis = analysisService.prepareForResearchRetry(job);
        sseEventPublisher.publish(ResearchEventCommand.builder()
                .jobId(jobId)
                .conversationId(analysis.getConversationId())
                .analysisRunId(analysis.getAnalysisRunId())
                .scope(ResearchEventScope.RESEARCH)
                .eventType(ResearchEventTypes.RESEARCH_RETRY_SCHEDULED)
                .phase(job.getCurrentNode())
                .nodeCode(job.getCurrentNode())
                .message("市场调研数据 Graph 已重新排队")
                .payload(Map.of(
                        "workflowVersion", job.getWorkflowVersion(),
                        "analysisStatus", analysis.getStatus()))
                .build());
        eventPublisher.publishEvent(new ResearchJobCreatedEvent(jobId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResearchDownload downloadArtifact(String jobId, String artifactId) {
        requireOwnedJob(jobId);
        MarketResearchArtifact artifact = artifactDao.getById(artifactId);
        if (artifact == null
                || !jobId.equals(artifact.getJobId())
                || !"PUBLISHED".equals(artifact.getArtifactStatus())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_NOT_READY);
        }
        return verifiedDownload(artifact);
    }

    private ResearchDownload verifiedDownload(MarketResearchArtifact artifact) {
        try {
            Resource resource = reportStorage.load(artifact.getStorageKey());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_INVALID);
            }
            long actualSize = resource.contentLength();
            String actualSha256;
            try (java.io.InputStream inputStream = resource.getInputStream()) {
                actualSha256 = ResearchHashUtils.sha256(inputStream);
            }
            if (!Long.valueOf(actualSize).equals(artifact.getFileSize())
                    || !actualSha256.equalsIgnoreCase(artifact.getSha256())) {
                throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_INVALID);
            }
            return new ResearchDownload(
                    resource,
                    artifact.getFileName(),
                    artifact.getMediaType(),
                    actualSize);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_INVALID);
        }
    }

    private Map<String, MarketResearchAnalysisRun> latestAnalysisRuns(
            List<MarketResearchAnalysisRun> analysisRuns) {
        Map<String, MarketResearchAnalysisRun> latestByJob = new HashMap<>();
        for (MarketResearchAnalysisRun analysisRun : analysisRuns) {
            latestByJob.merge(
                    analysisRun.getJobId(),
                    analysisRun,
                    (existing, candidate) -> ANALYSIS_RUN_RECENCY.compare(existing, candidate) >= 0
                            ? existing
                            : candidate);
        }
        return latestByJob;
    }

    private Map<String, List<ResearchArtifactVo>> artifactsByJob(
            List<MarketResearchArtifact> artifacts) {
        Map<String, List<ResearchArtifactVo>> artifactsByJob = new HashMap<>();
        for (MarketResearchArtifact artifact : artifacts) {
            artifactsByJob
                    .computeIfAbsent(artifact.getJobId(), ignored -> new ArrayList<>())
                    .add(toArtifactVo(artifact));
        }
        return artifactsByJob;
    }

    private ResearchJobHistoryVo toHistoryVo(
            MarketResearchJob job,
            MarketResearchAnalysisRun analysisRun,
            List<ResearchArtifactVo> artifacts) {
        return ResearchJobHistoryVo.builder()
                .jobId(job.getJobId())
                .reportName(job.getReportName())
                .marketplace(job.getMarketplace())
                .nodeIdPath(job.getNodeIdPath())
                .month(job.getResearchMonth())
                .keyword(job.getKeyword())
                .status(job.getJobStatus())
                .currentStage(job.getCurrentStage())
                .waitingInputType(job.getWaitingInputType())
                .progress(job.getProgress())
                .analysisRunId(analysisRun == null ? null : analysisRun.getAnalysisRunId())
                .analysisStatus(analysisRun == null ? null : analysisRun.getRunStatus())
                .analysisPhase(analysisRun == null ? null : analysisRun.getCurrentPhase())
                .analysisProgress(analysisRun == null ? null : analysisRun.getProgress())
                .createdAt(job.getCreatedAt())
                .finishedAt(job.getFinishedAt())
                .artifacts(List.copyOf(artifacts))
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

    private MarketResearchJob requireOwnedJob(String jobId) {
        return jobDao.findByIdAndUserId(jobId, currentUserId())
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND));
    }

    private String currentUserId() {
        return RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(userId -> userId != null && !userId.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    }

    private List<String> normalizeAsins(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        if (normalized.size() > ResearchConstants.MAX_SEED_ASINS) {
            throw new BizException(ResultCode.PARAM_INVALID, "种子ASIN不能超过20个");
        }
        return normalized;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception exception) {
            throw new IllegalStateException("序列化种子ASIN失败", exception);
        }
    }

    private String writeJson(CollectionGraphConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception exception) {
            throw new IllegalStateException("序列化采集配置失败", exception);
        }
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

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
