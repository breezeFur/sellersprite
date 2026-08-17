package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchArtifactStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.report.MarkdownPdfRenderer;
import cyou.yuanbaomao.sellersprite.research.report.ResearchReportChartPort;
import cyou.yuanbaomao.sellersprite.research.report.StageConclusionMarkdownExtractor;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 将模型真实生成的 Markdown 转为 PDF 后保存到受控报告存储和 artifact 表。 */
@Service
@RequiredArgsConstructor
public class ResearchAnalysisArtifactService {

    private static final String PDF_EXTENSION = "pdf";

    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final MarketResearchArtifactDao artifactDao;
    private final ReportStorage reportStorage;
    private final MarkdownPdfRenderer markdownPdfRenderer;
    private final StageConclusionMarkdownExtractor stageConclusionMarkdownExtractor;
    private final IdGenerator idGenerator;
    private final ResearchSseEventPublisher eventPublisher;
    private final ResearchReportChartPort reportChartPort;

    @Transactional(readOnly = true)
    public boolean hasPublishedMarkdown(String analysisRunId) {
        return artifactDao.findAvailableByAnalysisRunIdAndType(
                        analysisRunId,
                        ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT)
                .filter(this::isPublishedPdf)
                .isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public MarketResearchArtifact publishMarkdown(ResearchAnalysisLease lease, String markdown) {
        return publishMarkdown(
                new ArtifactContext(
                        lease.jobId(), lease.analysisRunId(), lease.conversationId()),
                markdown,
                PdfArtifactDescriptor.finalReport(lease.analysisRunId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public MarketResearchArtifact publishFinalMarkdown(String jobId, String userId) {
        MarketResearchAnalysisRun run = analysisRunDao
                .findLatestByJobIdAndUserIdAndRunType(
                        jobId, userId, ResearchAnalysisRunType.FINAL_ANALYSIS.name())
                .filter(value -> ResearchAnalysisRunStatus.SUCCEEDED.name()
                        .equals(value.getRunStatus()))
                .orElseThrow(() -> new IllegalStateException(
                        "最终分析尚未成功，不能发布Markdown: " + jobId));
        return publishMarkdown(
                new ArtifactContext(jobId, run.getAnalysisRunId(), run.getConversationId()),
                run.getFinalSummary(),
                PdfArtifactDescriptor.finalReport(run.getAnalysisRunId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public MarketResearchArtifact publishStageConclusionPdf(
            String jobId, String userId, EvidenceStage stage) {
        PdfArtifactDescriptor descriptor = PdfArtifactDescriptor.stageConclusion(stage);
        MarketResearchAnalysisRun run = analysisRunDao
                .findLatestByJobIdAndUserIdAndRunType(
                        jobId, userId, descriptor.runType().name())
                .filter(value -> ResearchAnalysisRunStatus.SUCCEEDED.name()
                        .equals(value.getRunStatus()))
                .orElseThrow(() -> new IllegalStateException(
                        descriptor.displayName() + "对应的阶段分析尚未成功: " + jobId));
        String markdown = stageConclusionMarkdownExtractor.extract(
                run.getFinalSummary(), descriptor.scorecardHeading());
        return publishMarkdown(
                new ArtifactContext(jobId, run.getAnalysisRunId(), run.getConversationId()),
                markdown,
                descriptor.withAnalysisRunId(run.getAnalysisRunId()));
    }

    private MarketResearchArtifact publishMarkdown(
            ArtifactContext context, String markdown, PdfArtifactDescriptor descriptor) {
        if (markdown == null || markdown.isBlank()) {
            throw new IllegalArgumentException(descriptor.displayName() + "内容不能为空");
        }
        MarketResearchArtifact artifact = artifactDao
                .findByAnalysisRunIdAndType(
                        context.analysisRunId(), descriptor.artifactType())
                .orElseGet(MarketResearchArtifact::new);
        if (isPublishedPdf(artifact)) {
            return artifact;
        }
        if (artifact.getArtifactId() == null) {
            artifact.setArtifactId(idGenerator.nextId());
            artifact.setJobId(context.jobId());
            artifact.setAnalysisRunId(context.analysisRunId());
            artifact.setArtifactScopeId(context.analysisRunId());
        }
        artifact.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        artifact.setArtifactType(descriptor.artifactType());
        artifact.setFileName(descriptor.fileName());
        artifact.setMediaType(ResearchConstants.PDF_MEDIA_TYPE);
        artifact.setArtifactStatus(ResearchArtifactStatus.GENERATING.name());
        artifact.setPublishedAt(null);
        try {
            Path draft = reportStorage.createDraftPath(
                    context.jobId(), artifact.getArtifactId(), PDF_EXTENSION);
            artifact.setStorageKey(reportStorage.storageKey(draft));
            if (!artifactDao.saveOrUpdate(artifact)) {
                throw new IllegalStateException("保存" + descriptor.displayName() + "草稿记录失败");
            }
            if (descriptor.runType() == ResearchAnalysisRunType.FINAL_ANALYSIS) {
                markdownPdfRenderer.render(markdown, reportChartPort.buildCharts(context.jobId()), draft);
            } else {
                markdownPdfRenderer.render(markdown, draft);
            }
            String publishedKey = reportStorage.publish(artifact.getStorageKey());
            Path published = reportStorage.resolve(publishedKey);
            artifact.setStorageKey(publishedKey);
            artifact.setFileSize(Files.size(published));
            artifact.setSha256(ResearchHashUtils.sha256(published));
            artifact.setArtifactStatus(ResearchArtifactStatus.PUBLISHED.name());
            artifact.setPublishedAt(System.currentTimeMillis());
            if (!artifactDao.updateById(artifact)) {
                throw new IllegalStateException("发布" + descriptor.displayName() + "记录失败");
            }
            publishArtifactEvents(context, artifact, markdown, descriptor);
            return artifact;
        } catch (Exception exception) {
            artifact.setArtifactStatus(ResearchArtifactStatus.FAILED.name());
            if (artifact.getArtifactId() != null) {
                artifactDao.saveOrUpdate(artifact);
            }
            throw new IllegalStateException("保存" + descriptor.displayName() + "失败", exception);
        }
    }

    private boolean isPublishedPdf(MarketResearchArtifact artifact) {
        return artifact != null
                && ResearchArtifactStatus.PUBLISHED.name().equals(artifact.getArtifactStatus())
                && ResearchConstants.PDF_MEDIA_TYPE.equals(artifact.getMediaType())
                && artifact.getStorageKey() != null
                && Files.isRegularFile(reportStorage.resolve(artifact.getStorageKey()));
    }

    private void publishArtifactEvents(
            ArtifactContext context,
            MarketResearchArtifact artifact,
            String markdown,
            PdfArtifactDescriptor descriptor) {
        Map<String, Object> payload = Map.of(
                "artifactId", artifact.getArtifactId(),
                "analysisRunId", context.analysisRunId(),
                "artifactType", artifact.getArtifactType(),
                "stageCode", descriptor.stageCode().name(),
                "fileName", artifact.getFileName(),
                "mediaType", artifact.getMediaType(),
                "fileSize", artifact.getFileSize(),
                "downloadUrl", "/api/market-research/jobs/" + context.jobId()
                        + "/artifacts/" + artifact.getArtifactId() + "/download");
        if (descriptor.publishReportEvent()) {
            eventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(context.jobId())
                    .conversationId(context.conversationId())
                    .analysisRunId(context.analysisRunId())
                    .scope(ResearchEventScope.ARTIFACT)
                    .eventType(ResearchEventTypes.REPORT)
                    .phase(descriptor.stageCode().name())
                    .message(markdown)
                    .payload(payload)
                    .build());
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(context.jobId())
                .conversationId(context.conversationId())
                .analysisRunId(context.analysisRunId())
                .scope(ResearchEventScope.ARTIFACT)
                .eventType(ResearchEventTypes.DOWNLOAD)
                .phase(descriptor.stageCode().name())
                .message(descriptor.displayName() + "已生成，可下载")
                .payload(payload)
                .build());
    }

    private record ArtifactContext(
            String jobId, String analysisRunId, String conversationId) {
    }

    private record PdfArtifactDescriptor(
            String artifactType,
            ResearchAnalysisRunType runType,
            ResearchStageCode stageCode,
            String scorecardHeading,
            String fileName,
            String displayName,
            boolean publishReportEvent) {

        private static PdfArtifactDescriptor finalReport(String analysisRunId) {
            return new PdfArtifactDescriptor(
                    ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT,
                    ResearchAnalysisRunType.FINAL_ANALYSIS,
                    ResearchStageCode.FINAL_ANALYSIS,
                    "最终决策评分速览",
                    "market-research-analysis-" + analysisRunId + ".pdf",
                    "AI分析报告",
                    true);
        }

        private static PdfArtifactDescriptor stageConclusion(EvidenceStage stage) {
            if (stage == EvidenceStage.SCREENING) {
                return new PdfArtifactDescriptor(
                        ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT,
                        ResearchAnalysisRunType.SCREENING,
                        ResearchStageCode.SCREENING,
                        "阶段一初筛评分速览",
                        null,
                        "阶段一结论表 PDF",
                        false);
            }
            return new PdfArtifactDescriptor(
                    ResearchConstants.ARTIFACT_TYPE_STAGE2_CONCLUSION_REPORT,
                    ResearchAnalysisRunType.DEEP_DIVE,
                    ResearchStageCode.DEEP_DIVE,
                    "阶段二深挖评分速览",
                    null,
                    "阶段二结论表 PDF",
                    false);
        }

        private PdfArtifactDescriptor withAnalysisRunId(String analysisRunId) {
            String stage = stageCode == ResearchStageCode.SCREENING ? "stage1" : "stage2";
            return new PdfArtifactDescriptor(
                    artifactType,
                    runType,
                    stageCode,
                    scorecardHeading,
                    "market-research-" + stage + "-conclusion-" + analysisRunId + ".pdf",
                    displayName,
                    publishReportEvent);
        }
    }
}
