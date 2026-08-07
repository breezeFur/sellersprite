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
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 将模型真实生成的 Markdown 保存到现有受控报告存储和 artifact 表。 */
@Service
@RequiredArgsConstructor
public class ResearchAnalysisArtifactService {

    private static final String MARKDOWN_EXTENSION = "md";

    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final MarketResearchArtifactDao artifactDao;
    private final ReportStorage reportStorage;
    private final IdGenerator idGenerator;
    private final ResearchSseEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public boolean hasPublishedMarkdown(String analysisRunId) {
        return artifactDao.findAvailableByAnalysisRunIdAndType(
                        analysisRunId,
                        ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT)
                .isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public MarketResearchArtifact publishMarkdown(ResearchAnalysisLease lease, String markdown) {
        return publishMarkdown(
                new ArtifactContext(
                        lease.jobId(), lease.analysisRunId(), lease.conversationId()),
                markdown);
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
                run.getFinalSummary());
    }

    private MarketResearchArtifact publishMarkdown(ArtifactContext context, String markdown) {
        if (markdown == null || markdown.isBlank()) {
            throw new IllegalArgumentException("AI分析报告内容不能为空");
        }
        MarketResearchArtifact artifact = artifactDao
                .findByAnalysisRunIdAndType(
                        context.analysisRunId(), ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT)
                .orElseGet(MarketResearchArtifact::new);
        if (ResearchArtifactStatus.PUBLISHED.name().equals(artifact.getArtifactStatus())
                && artifact.getStorageKey() != null
                && Files.isRegularFile(reportStorage.resolve(artifact.getStorageKey()))) {
            return artifact;
        }
        if (artifact.getArtifactId() == null) {
            artifact.setArtifactId(idGenerator.nextId());
            artifact.setJobId(context.jobId());
            artifact.setAnalysisRunId(context.analysisRunId());
            artifact.setArtifactScopeId(context.analysisRunId());
        }
        artifact.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        artifact.setArtifactType(ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT);
        artifact.setFileName("market-research-analysis-" + context.analysisRunId() + ".md");
        artifact.setMediaType(ResearchConstants.MARKDOWN_MEDIA_TYPE);
        artifact.setArtifactStatus(ResearchArtifactStatus.GENERATING.name());
        artifact.setPublishedAt(null);
        try {
            Path draft = reportStorage.createDraftPath(
                    context.jobId(), artifact.getArtifactId(), MARKDOWN_EXTENSION);
            artifact.setStorageKey(reportStorage.storageKey(draft));
            if (!artifactDao.saveOrUpdate(artifact)) {
                throw new IllegalStateException("保存AI分析报告草稿记录失败");
            }
            Files.writeString(draft, markdown, StandardCharsets.UTF_8);
            String publishedKey = reportStorage.publish(artifact.getStorageKey());
            Path published = reportStorage.resolve(publishedKey);
            artifact.setStorageKey(publishedKey);
            artifact.setFileSize(Files.size(published));
            artifact.setSha256(ResearchHashUtils.sha256(published));
            artifact.setArtifactStatus(ResearchArtifactStatus.PUBLISHED.name());
            artifact.setPublishedAt(System.currentTimeMillis());
            if (!artifactDao.updateById(artifact)) {
                throw new IllegalStateException("发布AI分析报告记录失败");
            }
            publishArtifactEvents(context, artifact, markdown);
            return artifact;
        } catch (Exception exception) {
            artifact.setArtifactStatus(ResearchArtifactStatus.FAILED.name());
            if (artifact.getArtifactId() != null) {
                artifactDao.saveOrUpdate(artifact);
            }
            throw new IllegalStateException("保存AI分析Markdown报告失败", exception);
        }
    }

    private void publishArtifactEvents(
            ArtifactContext context, MarketResearchArtifact artifact, String markdown) {
        Map<String, Object> payload = Map.of(
                "artifactId", artifact.getArtifactId(),
                "analysisRunId", context.analysisRunId(),
                "artifactType", artifact.getArtifactType(),
                "stageCode", ResearchStageCode.FINAL_ANALYSIS.name(),
                "fileName", artifact.getFileName(),
                "mediaType", artifact.getMediaType(),
                "fileSize", artifact.getFileSize(),
                "downloadUrl", "/api/market-research/jobs/" + context.jobId()
                        + "/artifacts/" + artifact.getArtifactId() + "/download");
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(context.jobId())
                .conversationId(context.conversationId())
                .analysisRunId(context.analysisRunId())
                .scope(ResearchEventScope.ARTIFACT)
                .eventType(ResearchEventTypes.REPORT)
                .phase(ResearchStageCode.FINAL_ANALYSIS.name())
                .message(markdown)
                .payload(payload)
                .build());
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(context.jobId())
                .conversationId(context.conversationId())
                .analysisRunId(context.analysisRunId())
                .scope(ResearchEventScope.ARTIFACT)
                .eventType(ResearchEventTypes.DOWNLOAD)
                .phase(ResearchStageCode.FINAL_ANALYSIS.name())
                .message("AI分析报告已生成，可下载")
                .payload(payload)
                .build());
    }

    private record ArtifactContext(
            String jobId, String analysisRunId, String conversationId) {
    }
}
