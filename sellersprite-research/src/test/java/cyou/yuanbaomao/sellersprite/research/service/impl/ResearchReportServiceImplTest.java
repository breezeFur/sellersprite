package cyou.yuanbaomao.sellersprite.research.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchArtifactStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.excel.ResearchRawWorkbookRenderer;
import cyou.yuanbaomao.sellersprite.research.excel.ResearchWorkbookRenderer;
import cyou.yuanbaomao.sellersprite.research.excel.ResearchWorkbookValidator;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisArtifactService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSseEventPublisher;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchReportServiceImplTest {

    private static final String JOB_ID = "job-terminal-artifacts";
    private static final String USER_ID = "user-terminal-artifacts";
    private static final String EXECUTION_TOKEN = "execution-token";

    @Mock
    private MarketResearchArtifactDao artifactDao;

    @Mock
    private ResearchJobStateService jobStateService;

    @Mock
    private ResearchRawWorkbookRenderer rawWorkbookRenderer;

    @Mock
    private ResearchWorkbookRenderer workbookRenderer;

    @Mock
    private ResearchWorkbookValidator workbookValidator;

    @Mock
    private ResearchAnalysisArtifactService analysisArtifactService;

    @Mock
    private ReportStorage reportStorage;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ResearchSseEventPublisher eventPublisher;

    @TempDir
    private Path temporaryDirectory;

    private ResearchReportServiceImpl service;
    private final Map<String, MarketResearchArtifact> artifacts = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        service = new ResearchReportServiceImpl(
                artifactDao,
                jobStateService,
                rawWorkbookRenderer,
                workbookRenderer,
                workbookValidator,
                analysisArtifactService,
                reportStorage,
                idGenerator,
                eventPublisher);
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        when(jobStateService.requireJob(JOB_ID)).thenReturn(job);
        when(artifactDao.findByJobIdAndType(any(), any())).thenAnswer(invocation ->
                Optional.ofNullable(artifacts.get(invocation.getArgument(1))));
        when(reportStorage.resolve(any())).thenAnswer(invocation ->
                Path.of((String) invocation.getArgument(0)));
    }

    @Test
    void shouldAcceptExactlySevenArtifactsForEnterWithoutRerenderingExcel() throws Exception {
        addPublishedArtifacts(
                ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT,
                ResearchConstants.ARTIFACT_TYPE_STAGE2_RAW_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE2_EVIDENCE_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE2_CONCLUSION_REPORT,
                ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT);
        when(artifactDao.listAvailableByJobIds(List.of(JOB_ID)))
                .thenReturn(List.copyOf(artifacts.values()));

        service.finalizeArtifacts(JOB_ID, EXECUTION_TOKEN, ResearchSelectionDecision.ENTER);

        verify(analysisArtifactService).publishStageConclusionPdf(
                JOB_ID, USER_ID, EvidenceStage.SCREENING);
        verify(analysisArtifactService).publishStageConclusionPdf(
                JOB_ID, USER_ID, EvidenceStage.DEEP_DIVE);
        verify(analysisArtifactService).publishFinalMarkdown(JOB_ID, USER_ID);
        verify(rawWorkbookRenderer, never())
                .render(any(MarketResearchJob.class), any(Path.class), any(EvidenceStage.class));
        verify(workbookRenderer, never())
                .render(any(MarketResearchJob.class), any(Path.class), any(EvidenceStage.class));
    }

    @Test
    void shouldAcceptExactlyThreeArtifactsForAbandon() throws Exception {
        addPublishedArtifacts(
                ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT);
        when(artifactDao.listAvailableByJobIds(List.of(JOB_ID)))
                .thenReturn(List.copyOf(artifacts.values()));

        service.finalizeArtifacts(JOB_ID, EXECUTION_TOKEN, ResearchSelectionDecision.ABANDON);

        verify(analysisArtifactService).publishStageConclusionPdf(
                JOB_ID, USER_ID, EvidenceStage.SCREENING);
        verify(analysisArtifactService, never()).publishStageConclusionPdf(
                JOB_ID, USER_ID, EvidenceStage.DEEP_DIVE);
        verify(analysisArtifactService, never()).publishFinalMarkdown(any(), any());
        verify(rawWorkbookRenderer, never())
                .render(any(MarketResearchJob.class), any(Path.class), any(EvidenceStage.class));
        verify(workbookRenderer, never())
                .render(any(MarketResearchJob.class), any(Path.class), any(EvidenceStage.class));
    }

    @Test
    void shouldRejectUnexpectedPublishedArtifact() throws Exception {
        addPublishedArtifacts(
                ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT,
                "UNEXPECTED_ARTIFACT");
        when(artifactDao.listAvailableByJobIds(List.of(JOB_ID)))
                .thenReturn(List.copyOf(artifacts.values()));

        assertThatThrownBy(() -> service.finalizeArtifacts(
                        JOB_ID, EXECUTION_TOKEN, ResearchSelectionDecision.ABANDON))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("市场调研终态产物集合不正确")
                .hasMessageContaining("UNEXPECTED_ARTIFACT");
    }

    private void addPublishedArtifacts(String... artifactTypes) throws Exception {
        for (String artifactType : artifactTypes) {
            Path file = temporaryDirectory.resolve(artifactType + ".bin");
            Files.writeString(file, artifactType);
            MarketResearchArtifact artifact = new MarketResearchArtifact();
            artifact.setArtifactId("artifact-" + artifactType);
            artifact.setJobId(JOB_ID);
            artifact.setArtifactType(artifactType);
            artifact.setArtifactStatus(ResearchArtifactStatus.PUBLISHED.name());
            artifact.setStorageKey(file.toString());
            artifacts.put(artifactType, artifact);
        }
    }
}
