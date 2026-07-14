package com.yuanbaomao.sellersprite.research.service.impl;

import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.id.IdGenerator;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.config.ResearchProperties;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import com.yuanbaomao.sellersprite.research.event.ResearchJobCreatedEvent;
import com.yuanbaomao.sellersprite.research.model.ResearchDownload;
import com.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import com.yuanbaomao.sellersprite.research.service.ResearchJobService;
import com.yuanbaomao.sellersprite.research.storage.ReportStorage;
import com.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ResearchJobServiceImpl implements ResearchJobService {

    private final MarketResearchJobDao jobDao;
    private final MarketResearchArtifactDao artifactDao;
    private final ResearchProperties properties;
    private final ReportStorage reportStorage;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResearchJobCreatedVo create(ResearchJobCreateRequest request) {
        String userId = currentUserId();
        List<String> seedAsins = normalizeAsins(request.getSeedAsins());
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(idGenerator.nextId());
        job.setUserId(userId);
        job.setReportName(request.getReportName().trim());
        job.setMarketplace(ResearchConstants.MARKETPLACE_US);
        job.setKeyword(request.getKeyword().trim());
        job.setSeedAsins(writeJson(seedAsins));
        job.setTemplateCode(ResearchConstants.TEMPLATE_CODE);
        job.setDataSourceMode(properties.getSourceMode().name());
        job.setJobStatus(ResearchJobStatus.QUEUED.name());
        job.setCurrentPhase(ResearchPhase.VALIDATE.name());
        job.setProgress(0);
        job.setErrorCode("");
        job.setErrorMessage("");
        if (!jobDao.save(job)) {
            throw new IllegalStateException("保存市场调研任务失败");
        }
        eventPublisher.publishEvent(new ResearchJobCreatedEvent(job.getJobId()));
        return ResearchJobCreatedVo.builder()
                .jobId(job.getJobId())
                .status(job.getJobStatus())
                .dataSourceMode(job.getDataSourceMode())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResearchJobDetailVo detail(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        MarketResearchArtifact artifact = artifactDao.findAvailableByJobId(jobId).orElse(null);
        return ResearchJobDetailVo.builder()
                .jobId(job.getJobId())
                .reportName(job.getReportName())
                .marketplace(job.getMarketplace())
                .keyword(job.getKeyword())
                .dataSourceMode(job.getDataSourceMode())
                .status(job.getJobStatus())
                .currentPhase(job.getCurrentPhase())
                .currentPhaseName(phaseName(job.getCurrentPhase()))
                .progress(job.getProgress())
                .batchJobExecutionId(job.getBatchJobExecutionId())
                .errorCode(job.getErrorCode())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .downloadable(artifact != null)
                .fileName(artifact == null ? null : artifact.getFileName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResearchDownload download(String jobId) {
        requireOwnedJob(jobId);
        MarketResearchArtifact artifact = artifactDao.findAvailableByJobId(jobId)
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_REPORT_NOT_READY));
        try {
            Resource resource = reportStorage.load(artifact.getStorageKey());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_INVALID);
            }
            byte[] content;
            try (java.io.InputStream inputStream = resource.getInputStream()) {
                content = inputStream.readAllBytes();
            }
            long actualSize = content.length;
            String actualSha256 = ResearchHashUtils.sha256(content);
            if (!Long.valueOf(actualSize).equals(artifact.getFileSize())
                    || !actualSha256.equalsIgnoreCase(artifact.getSha256())) {
                throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_INVALID);
            }
            return new ResearchDownload(
                    new ByteArrayResource(content),
                    artifact.getFileName(),
                    artifact.getMediaType(),
                    actualSize);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ResultCode.MARKET_RESEARCH_REPORT_INVALID);
        }
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

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception exception) {
            throw new IllegalStateException("序列化种子ASIN失败", exception);
        }
    }

    private String phaseName(String phase) {
        if (phase == null || phase.isBlank()) {
            return null;
        }
        try {
            return ResearchPhase.valueOf(phase).getDisplayName();
        } catch (IllegalArgumentException exception) {
            return phase;
        }
    }
}
