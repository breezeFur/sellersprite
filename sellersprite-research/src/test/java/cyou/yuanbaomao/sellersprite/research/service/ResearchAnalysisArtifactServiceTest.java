package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.report.MarkdownPdfRenderer;
import cyou.yuanbaomao.sellersprite.research.report.ResearchReportChartPort;
import cyou.yuanbaomao.sellersprite.research.report.StageConclusionMarkdownExtractor;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchAnalysisArtifactServiceTest {

    private static final String JOB_ID = "job-final-artifact";
    private static final String USER_ID = "user-final-artifact";
    private static final String RUN_ID = "run-final-artifact";
    private static final String CONVERSATION_ID = "conversation-final-artifact";
    private static final String ARTIFACT_ID = "artifact-final-pdf";
    private static final String MARKDOWN = "# Final market conclusion\n\nEnter this market carefully.";

    @Mock
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Mock
    private MarketResearchArtifactDao artifactDao;

    @Mock
    private ReportStorage reportStorage;

    @Mock
    private MarkdownPdfRenderer markdownPdfRenderer;

    @Mock
    private StageConclusionMarkdownExtractor stageConclusionMarkdownExtractor;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ResearchSseEventPublisher eventPublisher;

    @Mock
    private ResearchReportChartPort reportChartPort;

    @TempDir
    private Path temporaryDirectory;

    private ResearchAnalysisArtifactService service;

    @BeforeEach
    void setUp() {
        service = new ResearchAnalysisArtifactService(
                analysisRunDao,
                artifactDao,
                reportStorage,
                markdownPdfRenderer,
                stageConclusionMarkdownExtractor,
                idGenerator,
                eventPublisher,
                reportChartPort);
    }

    @Test
    void shouldPublishFinalMarkdownAsPdfOnceAndReusePublishedFile() throws Exception {
        MarketResearchAnalysisRun run = finalRun(MARKDOWN);
        AtomicReference<MarketResearchArtifact> stored = new AtomicReference<>();
        Path draft = temporaryDirectory.resolve("final.pdf");
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.FINAL_ANALYSIS.name()))
                .thenReturn(Optional.of(run));
        when(artifactDao.findByAnalysisRunIdAndType(
                        RUN_ID, ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT))
                .thenAnswer(ignored -> Optional.ofNullable(stored.get()));
        when(idGenerator.nextId()).thenReturn(ARTIFACT_ID);
        when(reportStorage.createDraftPath(JOB_ID, ARTIFACT_ID, "pdf")).thenReturn(draft);
        when(reportStorage.storageKey(draft)).thenReturn(draft.toString());
        when(reportStorage.publish(draft.toString())).thenReturn(draft.toString());
        when(reportStorage.resolve(draft.toString())).thenReturn(draft);
        when(artifactDao.saveOrUpdate(any(MarketResearchArtifact.class))).thenAnswer(invocation -> {
            stored.set(invocation.getArgument(0));
            return true;
        });
        when(artifactDao.updateById(any(MarketResearchArtifact.class))).thenReturn(true);
        org.mockito.Mockito.doAnswer(invocation -> {
            Files.write(draft, "%PDF-1.7\n中文报告".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(markdownPdfRenderer).render(MARKDOWN, List.of(), draft);

        MarketResearchArtifact first = service.publishFinalMarkdown(JOB_ID, USER_ID);
        MarketResearchArtifact second = service.publishFinalMarkdown(JOB_ID, USER_ID);

        assertThat(second).isSameAs(first);
        assertThat(first.getArtifactStatus()).isEqualTo("PUBLISHED");
        assertThat(Files.readString(draft, StandardCharsets.ISO_8859_1))
                .startsWith("%PDF-1.7");
        assertThat(first.getFileName()).endsWith(".pdf");
        assertThat(first.getMediaType()).isEqualTo(ResearchConstants.PDF_MEDIA_TYPE);
        verify(markdownPdfRenderer).render(MARKDOWN, List.of(), draft);
        verify(artifactDao, times(1)).saveOrUpdate(any(MarketResearchArtifact.class));
        verify(artifactDao, times(1)).updateById(any(MarketResearchArtifact.class));

        ArgumentCaptor<ResearchEventCommand> events =
                ArgumentCaptor.forClass(ResearchEventCommand.class);
        verify(eventPublisher, times(2)).publish(events.capture());
        assertThat(events.getAllValues())
                .extracting(ResearchEventCommand::getEventType)
                .containsExactly(ResearchEventTypes.REPORT, ResearchEventTypes.DOWNLOAD);
        assertThat(events.getAllValues())
                .allSatisfy(event -> assertThat(
                                ((Map<?, ?>) event.getPayload()).get("stageCode"))
                        .isEqualTo("FINAL_ANALYSIS"));
    }

    @Test
    void shouldRejectPublishingWhenFinalRunHasNoMarkdown() {
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.FINAL_ANALYSIS.name()))
                .thenReturn(Optional.of(finalRun(" ")));

        assertThatThrownBy(() -> service.publishFinalMarkdown(JOB_ID, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AI分析报告内容不能为空");
    }

    @ParameterizedTest
    @EnumSource(EvidenceStage.class)
    void shouldPublishStageConclusionTableAsSeparatePdf(EvidenceStage stage) throws Exception {
        boolean screening = stage == EvidenceStage.SCREENING;
        ResearchAnalysisRunType runType = screening
                ? ResearchAnalysisRunType.SCREENING
                : ResearchAnalysisRunType.DEEP_DIVE;
        String stageCode = screening ? "SCREENING" : "DEEP_DIVE";
        String stageNumber = screening ? "stage1" : "stage2";
        String scorecardHeading = screening
                ? "阶段一初筛评分速览"
                : "阶段二深挖评分速览";
        String artifactType = screening
                ? ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT
                : ResearchConstants.ARTIFACT_TYPE_STAGE2_CONCLUSION_REPORT;
        String scorecard = "## " + scorecardHeading + "\n\n| 维度 | 评分 | 关键依据 |";
        MarketResearchAnalysisRun run = stageRun(runType, MARKDOWN);
        Path draft = temporaryDirectory.resolve(stageNumber + "-conclusion.pdf");
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, runType.name()))
                .thenReturn(Optional.of(run));
        when(stageConclusionMarkdownExtractor.extract(MARKDOWN, scorecardHeading))
                .thenReturn(scorecard);
        when(artifactDao.findByAnalysisRunIdAndType(
                        RUN_ID, artifactType))
                .thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(ARTIFACT_ID);
        when(reportStorage.createDraftPath(JOB_ID, ARTIFACT_ID, "pdf")).thenReturn(draft);
        when(reportStorage.storageKey(draft)).thenReturn(draft.toString());
        when(reportStorage.publish(draft.toString())).thenReturn(draft.toString());
        when(reportStorage.resolve(draft.toString())).thenReturn(draft);
        when(artifactDao.saveOrUpdate(any(MarketResearchArtifact.class))).thenReturn(true);
        when(artifactDao.updateById(any(MarketResearchArtifact.class))).thenReturn(true);
        org.mockito.Mockito.doAnswer(invocation -> {
            Files.write(draft, ("%PDF-1.7\n" + stageNumber).getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(markdownPdfRenderer).render(scorecard, draft);

        MarketResearchArtifact artifact = service.publishStageConclusionPdf(
                JOB_ID, USER_ID, stage);

        assertThat(artifact.getArtifactType()).isEqualTo(artifactType);
        assertThat(artifact.getFileName()).isEqualTo(
                "market-research-" + stageNumber + "-conclusion-" + RUN_ID + ".pdf");
        assertThat(artifact.getMediaType()).isEqualTo(ResearchConstants.PDF_MEDIA_TYPE);
        verify(markdownPdfRenderer).render(scorecard, draft);

        ArgumentCaptor<ResearchEventCommand> events =
                ArgumentCaptor.forClass(ResearchEventCommand.class);
        verify(eventPublisher).publish(events.capture());
        assertThat(events.getValue().getEventType()).isEqualTo(ResearchEventTypes.DOWNLOAD);
        assertThat(((Map<?, ?>) events.getValue().getPayload()).get("stageCode"))
                .isEqualTo(stageCode);
    }

    private MarketResearchAnalysisRun finalRun(String markdown) {
        return stageRun(ResearchAnalysisRunType.FINAL_ANALYSIS, markdown);
    }

    private MarketResearchAnalysisRun stageRun(
            ResearchAnalysisRunType runType, String markdown) {
        MarketResearchAnalysisRun run = new MarketResearchAnalysisRun();
        run.setAnalysisRunId(RUN_ID);
        run.setJobId(JOB_ID);
        run.setUserId(USER_ID);
        run.setConversationId(CONVERSATION_ID);
        run.setRunType(runType.name());
        run.setRunStatus(ResearchAnalysisRunStatus.SUCCEEDED.name());
        run.setFinalSummary(markdown);
        return run;
    }
}
