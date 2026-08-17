package cyou.yuanbaomao.sellersprite.research.service.impl;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchArtifactStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.excel.ResearchRawWorkbookRenderer;
import cyou.yuanbaomao.sellersprite.research.excel.ResearchWorkbookRenderer;
import cyou.yuanbaomao.sellersprite.research.excel.ResearchWorkbookValidator;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisArtifactService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchArtifactFinalizationPort;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchReportService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSseEventPublisher;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Excel 草稿生成、结构校验与原子发布实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchReportServiceImpl
        implements ResearchReportService, ResearchArtifactFinalizationPort {

    private static final int MAX_FILE_BASENAME_LENGTH = 100;

    private final MarketResearchArtifactDao artifactDao;
    private final ResearchJobStateService jobStateService;
    private final ResearchRawWorkbookRenderer rawWorkbookRenderer;
    private final ResearchWorkbookRenderer workbookRenderer;
    private final ResearchWorkbookValidator workbookValidator;
    private final ResearchAnalysisArtifactService analysisArtifactService;
    private final ReportStorage reportStorage;
    private final IdGenerator idGenerator;
    private final ResearchSseEventPublisher eventPublisher;

    @Override
    public void renderRawDraft(String jobId) {
        renderRawDraft(jobId, EvidenceStage.SCREENING);
    }

    @Override
    public void validateAndPublishRaw(String jobId) {
        validateAndPublishRaw(jobId, EvidenceStage.SCREENING);
    }

    @Override
    public void renderEvidenceDraft(String jobId) {
        renderEvidenceDraft(jobId, EvidenceStage.SCREENING);
    }

    @Override
    public void validateAndPublishEvidence(String jobId) {
        validateAndPublishEvidence(jobId, EvidenceStage.SCREENING);
    }

    @Override
    public void finalizeArtifacts(
            String jobId,
            String parentExecutionToken,
            ResearchSelectionDecision decision) {
        jobStateService.assertExecutable(jobId, parentExecutionToken);
        MarketResearchJob job = jobStateService.requireJob(jobId);
        renderAndPublishStage(jobId, EvidenceStage.SCREENING);
        analysisArtifactService.publishStageConclusionPdf(
                jobId, job.getUserId(), EvidenceStage.SCREENING);
        if (decision == ResearchSelectionDecision.ENTER) {
            jobStateService.assertExecutable(jobId, parentExecutionToken);
            renderAndPublishStage(jobId, EvidenceStage.DEEP_DIVE);
            jobStateService.assertExecutable(jobId, parentExecutionToken);
            analysisArtifactService.publishStageConclusionPdf(
                    jobId, job.getUserId(), EvidenceStage.DEEP_DIVE);
            jobStateService.assertExecutable(jobId, parentExecutionToken);
            analysisArtifactService.publishFinalMarkdown(jobId, job.getUserId());
        }
        jobStateService.assertExecutable(jobId, parentExecutionToken);
        assertPublishedArtifacts(jobId, decision);
    }

    private void renderAndPublishStage(String jobId, EvidenceStage stage) {
        renderRawDraft(jobId, stage);
        validateAndPublishRaw(jobId, stage);
        renderEvidenceDraft(jobId, stage);
        validateAndPublishEvidence(jobId, stage);
    }

    private void renderRawDraft(String jobId, EvidenceStage stage) {
        renderDraft(
                jobId,
                rawArtifactType(stage),
                stage == EvidenceStage.SCREENING ? "-stage1-raw" : "-stage2-raw",
                (job, target) -> rawWorkbookRenderer.render(job, target, stage));
    }

    private void validateAndPublishRaw(String jobId, EvidenceStage stage) {
        validateAndPublish(
                jobId,
                rawArtifactType(stage),
                "finalizeArtifacts",
                stageDisplayName(stage) + "原始数据 Excel",
                rawWorkbookRenderer::validate,
                stage);
    }

    private void renderEvidenceDraft(String jobId, EvidenceStage stage) {
        renderDraft(
                jobId,
                evidenceArtifactType(stage),
                stage == EvidenceStage.SCREENING ? "-stage1-evidence" : "-stage2-evidence",
                (job, target) -> workbookRenderer.render(job, target, stage));
    }

    private void validateAndPublishEvidence(String jobId, EvidenceStage stage) {
        validateAndPublish(
                jobId,
                evidenceArtifactType(stage),
                "finalizeArtifacts",
                stageDisplayName(stage) + "证据 Excel",
                target -> workbookValidator.validate(target, stage),
                stage);
    }

    private void renderDraft(
            String jobId,
            String artifactType,
            String fileSuffix,
            WorkbookRenderer renderer) {
        MarketResearchJob job = jobStateService.requireJob(jobId);
        MarketResearchArtifact artifact = findByJobIdAndType(jobId, artifactType);
        if (isPublishedFileAvailable(artifact)) {
            return;
        }
        if (artifact == null) {
            artifact = new MarketResearchArtifact();
            artifact.setArtifactId(idGenerator.nextId());
            artifact.setJobId(jobId);
            artifact.setArtifactScopeId(jobId);
        }
        artifact.setWorkflowVersion(job.getWorkflowVersion());
        artifact.setArtifactType(artifactType);
        artifact.setFileName(fileName(job.getReportName(), fileSuffix));
        artifact.setMediaType(ResearchConstants.EXCEL_MEDIA_TYPE);
        artifact.setArtifactStatus(ResearchArtifactStatus.GENERATING.name());
        artifact.setPublishedAt(null);
        try {
            Path draft = reportStorage.createDraftPath(jobId, artifact.getArtifactId());
            artifact.setStorageKey(reportStorage.storageKey(draft));
            if (!artifactDao.saveOrUpdate(artifact)) {
                throw new IllegalStateException("保存市场调研Excel草稿记录失败");
            }

            renderer.render(job, draft);
            artifact.setFileSize(Files.size(draft));
            artifact.setSha256(ResearchHashUtils.sha256(draft));
            if (!artifactDao.updateById(artifact)) {
                throw new IllegalStateException("更新市场调研Excel草稿记录失败");
            }
            log.info("市场调研Excel草稿已生成，jobId={}，artifactType={}，artifactId={}",
                    jobId, artifactType, artifact.getArtifactId());
        } catch (Exception exception) {
            markFailed(artifact);
            throw new IllegalStateException("生成市场调研Excel草稿失败", exception);
        }
    }

    private void validateAndPublish(
            String jobId,
            String artifactType,
            String nodeCode,
            String displayName,
            WorkbookValidator validator,
            EvidenceStage stage) {
        MarketResearchArtifact artifact = requireArtifact(jobId, artifactType);
        try {
            Path current = reportStorage.resolve(artifact.getStorageKey());
            if (ResearchArtifactStatus.PUBLISHED.name().equals(artifact.getArtifactStatus())
                    && Files.isRegularFile(current)) {
                validator.validate(current);
                return;
            }
            if (Files.isRegularFile(current)) {
                validator.validate(current);
            }
            String publishedKey = reportStorage.publish(artifact.getStorageKey());
            Path published = reportStorage.resolve(publishedKey);
            validator.validate(published);

            artifact.setStorageKey(publishedKey);
            artifact.setFileSize(Files.size(published));
            artifact.setSha256(ResearchHashUtils.sha256(published));
            artifact.setArtifactStatus(ResearchArtifactStatus.PUBLISHED.name());
            artifact.setPublishedAt(System.currentTimeMillis());
            if (!artifactDao.updateById(artifact)) {
                throw new IllegalStateException("更新市场调研Excel发布记录失败");
            }
            eventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(jobId)
                    .scope(ResearchEventScope.ARTIFACT)
                    .eventType(ResearchEventTypes.WORKBOOK_READY)
                    .phase(nodeCode)
                    .nodeCode(nodeCode)
                    .message(displayName + "已生成，可下载")
                    .payload(Map.of(
                            "artifactId", artifact.getArtifactId(),
                            "artifactType", artifact.getArtifactType(),
                            "stageCode", stage.name(),
                            "fileName", artifact.getFileName(),
                            "mediaType", artifact.getMediaType(),
                            "fileSize", artifact.getFileSize(),
                            "sha256", artifact.getSha256(),
                            "downloadUrl", "/api/market-research/jobs/" + jobId
                                    + "/artifacts/" + artifact.getArtifactId() + "/download"))
                    .build());
            log.info("市场调研Excel已发布，jobId={}，artifactType={}，artifactId={}",
                    jobId, artifactType, artifact.getArtifactId());
        } catch (Exception exception) {
            markFailed(artifact);
            throw new IllegalStateException("校验或发布市场调研Excel失败", exception);
        }
    }

    private void assertPublishedArtifacts(
            String jobId, ResearchSelectionDecision decision) {
        List<String> required = decision == ResearchSelectionDecision.ENTER
                ? List.of(
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK,
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK,
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT,
                        ResearchConstants.ARTIFACT_TYPE_STAGE2_RAW_WORKBOOK,
                        ResearchConstants.ARTIFACT_TYPE_STAGE2_EVIDENCE_WORKBOOK,
                        ResearchConstants.ARTIFACT_TYPE_STAGE2_CONCLUSION_REPORT,
                        ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT)
                : List.of(
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK,
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK,
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT);
        List<String> actual = artifactDao.listAvailableByJobIds(List.of(jobId)).stream()
                .filter(this::isPublishedFileAvailable)
                .map(MarketResearchArtifact::getArtifactType)
                .toList();
        if (actual.size() != required.size()
                || !actual.containsAll(required)
                || !required.containsAll(actual)) {
            throw new IllegalStateException(
                    "市场调研终态产物集合不正确: " + jobId
                            + "，期望=" + required + "，实际=" + actual);
        }
    }

    private String rawArtifactType(EvidenceStage stage) {
        return stage == EvidenceStage.SCREENING
                ? ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK
                : ResearchConstants.ARTIFACT_TYPE_STAGE2_RAW_WORKBOOK;
    }

    private String evidenceArtifactType(EvidenceStage stage) {
        return stage == EvidenceStage.SCREENING
                ? ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK
                : ResearchConstants.ARTIFACT_TYPE_STAGE2_EVIDENCE_WORKBOOK;
    }

    private String stageDisplayName(EvidenceStage stage) {
        return stage == EvidenceStage.SCREENING ? "阶段一" : "阶段二";
    }

    private MarketResearchArtifact requireArtifact(String jobId, String artifactType) {
        MarketResearchArtifact artifact = findByJobIdAndType(jobId, artifactType);
        if (artifact == null) {
            throw new IllegalStateException("市场调研Excel产物不存在: " + jobId + "/" + artifactType);
        }
        return artifact;
    }

    private MarketResearchArtifact findByJobIdAndType(String jobId, String artifactType) {
        return artifactDao.findByJobIdAndType(jobId, artifactType).orElse(null);
    }

    private boolean isPublishedFileAvailable(MarketResearchArtifact artifact) {
        return artifact != null
                && ResearchArtifactStatus.PUBLISHED.name().equals(artifact.getArtifactStatus())
                && artifact.getStorageKey() != null
                && Files.isRegularFile(reportStorage.resolve(artifact.getStorageKey()));
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

    private String fileName(String reportName, String suffix) {
        String value = reportName == null ? "市场调研报告" : reportName.trim();
        value = value.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (value.isBlank()) {
            value = "市场调研报告";
        }
        if (value.length() > MAX_FILE_BASENAME_LENGTH) {
            value = value.substring(0, MAX_FILE_BASENAME_LENGTH);
        }
        return value + suffix + ".xlsx";
    }

    @FunctionalInterface
    private interface WorkbookRenderer {

        void render(MarketResearchJob job, Path target) throws Exception;
    }

    @FunctionalInterface
    private interface WorkbookValidator {

        void validate(Path workbook) throws Exception;
    }
}
