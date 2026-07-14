package com.yuanbaomao.sellersprite.research.service.impl;

import com.yuanbaomao.base.id.IdGenerator;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchArtifactStatus;
import com.yuanbaomao.sellersprite.research.excel.ResearchWorkbookRenderer;
import com.yuanbaomao.sellersprite.research.excel.ResearchWorkbookValidator;
import com.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import com.yuanbaomao.sellersprite.research.service.ResearchReportService;
import com.yuanbaomao.sellersprite.research.storage.ReportStorage;
import com.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Excel 草稿生成、结构校验与原子发布实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchReportServiceImpl implements ResearchReportService {

    private static final int MAX_FILE_BASENAME_LENGTH = 100;

    private final MarketResearchArtifactDao artifactDao;
    private final ResearchJobStateService jobStateService;
    private final ResearchWorkbookRenderer workbookRenderer;
    private final ResearchWorkbookValidator workbookValidator;
    private final ReportStorage reportStorage;
    private final IdGenerator idGenerator;

    @Override
    public void renderDraft(String jobId) {
        MarketResearchJob job = jobStateService.requireJob(jobId);
        MarketResearchArtifact artifact = findByJobId(jobId);
        if (artifact == null) {
            artifact = new MarketResearchArtifact();
            artifact.setArtifactId(idGenerator.nextId());
            artifact.setJobId(jobId);
        }
        artifact.setFileName(fileName(job.getReportName()));
        artifact.setMediaType(ResearchConstants.EXCEL_MEDIA_TYPE);
        artifact.setArtifactStatus(ResearchArtifactStatus.GENERATING.name());
        artifact.setPublishedAt(null);
        try {
            Path draft = reportStorage.createDraftPath(jobId, artifact.getArtifactId());
            artifact.setStorageKey(reportStorage.storageKey(draft));
            if (!artifactDao.saveOrUpdate(artifact)) {
                throw new IllegalStateException("保存市场调研Excel草稿记录失败");
            }

            workbookRenderer.render(job, draft);
            artifact.setFileSize(Files.size(draft));
            artifact.setSha256(ResearchHashUtils.sha256(draft));
            if (!artifactDao.updateById(artifact)) {
                throw new IllegalStateException("更新市场调研Excel草稿记录失败");
            }
            log.info("市场调研Excel草稿已生成，jobId={}，artifactId={}", jobId, artifact.getArtifactId());
        } catch (Exception exception) {
            markFailed(artifact);
            throw new IllegalStateException("生成市场调研Excel草稿失败", exception);
        }
    }

    @Override
    public void validateAndPublish(String jobId) {
        MarketResearchArtifact artifact = requireArtifact(jobId);
        try {
            Path current = reportStorage.resolve(artifact.getStorageKey());
            if (Files.isRegularFile(current)) {
                workbookValidator.validate(current);
            }
            String publishedKey = reportStorage.publish(artifact.getStorageKey());
            Path published = reportStorage.resolve(publishedKey);
            workbookValidator.validate(published);

            artifact.setStorageKey(publishedKey);
            artifact.setFileSize(Files.size(published));
            artifact.setSha256(ResearchHashUtils.sha256(published));
            artifact.setArtifactStatus(ResearchArtifactStatus.PUBLISHED.name());
            artifact.setPublishedAt(System.currentTimeMillis());
            if (!artifactDao.updateById(artifact)) {
                throw new IllegalStateException("更新市场调研Excel发布记录失败");
            }
            log.info("市场调研Excel已发布，jobId={}，artifactId={}", jobId, artifact.getArtifactId());
        } catch (Exception exception) {
            markFailed(artifact);
            throw new IllegalStateException("校验或发布市场调研Excel失败", exception);
        }
    }

    private MarketResearchArtifact requireArtifact(String jobId) {
        MarketResearchArtifact artifact = findByJobId(jobId);
        if (artifact == null) {
            throw new IllegalStateException("市场调研Excel产物不存在: " + jobId);
        }
        return artifact;
    }

    private MarketResearchArtifact findByJobId(String jobId) {
        return artifactDao.lambdaQuery()
                .eq(MarketResearchArtifact::getJobId, jobId)
                .one();
    }

    private void markFailed(MarketResearchArtifact artifact) {
        if (artifact.getArtifactId() == null) {
            return;
        }
        try {
            artifact.setArtifactStatus(ResearchArtifactStatus.FAILED.name());
            artifactDao.saveOrUpdate(artifact);
        } catch (RuntimeException exception) {
            log.warn("记录市场调研Excel失败状态异常，artifactId={}", artifact.getArtifactId(), exception);
        }
    }

    private String fileName(String reportName) {
        String value = reportName == null ? "市场调研报告" : reportName.trim();
        value = value.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (value.isBlank()) {
            value = "市场调研报告";
        }
        if (value.length() > MAX_FILE_BASENAME_LENGTH) {
            value = value.substring(0, MAX_FILE_BASENAME_LENGTH);
        }
        return value + ".xlsx";
    }
}
