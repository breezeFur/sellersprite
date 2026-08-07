package cyou.yuanbaomao.sellersprite.research.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.event.ResearchJobCreatedEvent;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDownload;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobPageRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchAnalysisRunVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobHistoryVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchInputService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchNodeExecutionService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSseEventPublisher;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ResearchJobServiceImplTest {

    private static final String USER_ID = "user-research-001";
    private static final String JOB_ID = "job-service-001";

    @Mock
    private MarketResearchJobDao jobDao;

    @Mock
    private MarketResearchArtifactDao artifactDao;

    @Mock
    private ResearchNodeExecutionService nodeExecutionService;

    @Mock
    private ReportStorage reportStorage;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ResearchSseEventPublisher sseEventPublisher;

    @Mock
    private ResearchAnalysisService analysisService;

    @Mock
    private MarketResearchAnalysisRunDao analysisRunDao;

    @TempDir
    private Path temporaryDirectory;

    private ResearchJobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        jobService = new ResearchJobServiceImpl(
                jobDao,
                artifactDao,
                nodeExecutionService,
                new ResearchInputService(objectMapper),
                new ResearchProperties(),
                reportStorage,
                idGenerator,
                objectMapper,
                eventPublisher,
                sseEventPublisher,
                analysisService,
                analysisRunDao);
        RequestContextHolder.set(RequestContext.builder()
                .userId(USER_ID)
                .username("research-user")
                .build());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldCreateQueuedMockJobWithNormalizedDistinctAsins() {
        ResearchJobCreateRequest request = new ResearchJobCreateRequest();
        request.setReportName("  美容仪美国站市场调研  ");
        request.setMarketplace(SellerSpriteMarketplace.JP);
        request.setNodeIdPath("  2017304051:2017305051  ");
        request.setMonth("2026-07");
        request.setKeyword("  facial cleansing device  ");
        request.setSeedAsins(List.of(" b0test0001 ", "B0TEST0001", "B0TEST0002"));
        request.setCollectionConfig(collectionConfig());
        when(idGenerator.nextId()).thenReturn(JOB_ID);
        when(jobDao.save(any(MarketResearchJob.class))).thenReturn(true);
        stubInitialAnalysis();

        ResearchJobCreatedVo result = jobService.create(request);

        ArgumentCaptor<MarketResearchJob> jobCaptor = ArgumentCaptor.forClass(MarketResearchJob.class);
        verify(jobDao).save(jobCaptor.capture());
        MarketResearchJob saved = jobCaptor.getValue();
        assertThat(saved.getJobId()).isEqualTo(JOB_ID);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getReportName()).isEqualTo("美容仪美国站市场调研");
        assertThat(saved.getMarketplace()).isEqualTo("JP");
        assertThat(saved.getNodeIdPath()).isEqualTo("2017304051:2017305051");
        assertThat(saved.getResearchMonth()).isEqualTo("2026-07");
        assertThat(saved.getKeyword()).isEqualTo("facial cleansing device");
        assertThat(saved.getSeedAsins()).isEqualTo("[\"B0TEST0001\",\"B0TEST0002\"]");
        assertThat(saved.getCollectionConfig())
                .contains("\"monthCount\":12")
                .contains("\"targetCountPerAsin\":20");
        assertThat(saved.getDataSourceMode()).isEqualTo("MOCK");
        assertThat(saved.getJobStatus()).isEqualTo(ResearchJobStatus.QUEUED.name());
        assertThat(saved.getWorkflowVersion()).isEqualTo(ResearchConstants.WORKFLOW_VERSION);
        assertThat(saved.getCurrentNode()).isEqualTo(ResearchPhase.VALIDATE.getNodeCode());
        assertThat(saved.getAttemptCount()).isZero();
        assertThat(saved.getMaxAttempts()).isEqualTo(3);
        assertThat(saved.getNextRunAt()).isPositive();
        assertThat(result.getJobId()).isEqualTo(JOB_ID);
        assertThat(result.getStatus()).isEqualTo(ResearchJobStatus.QUEUED.name());
        verify(eventPublisher).publishEvent(new ResearchJobCreatedEvent(JOB_ID));
    }

    @Test
    void shouldCreateJobWithoutOptionalKeyword() {
        ResearchJobCreateRequest request = new ResearchJobCreateRequest();
        request.setReportName("日本站类目选品");
        request.setMarketplace(SellerSpriteMarketplace.JP);
        request.setNodeIdPath("2017304051:2017305051");
        request.setMonth("2026-07");
        request.setKeyword("  ");
        request.setCollectionConfig(collectionConfig());
        when(idGenerator.nextId()).thenReturn(JOB_ID);
        when(jobDao.save(any(MarketResearchJob.class))).thenReturn(true);
        stubInitialAnalysis();

        jobService.create(request);

        ArgumentCaptor<MarketResearchJob> jobCaptor = ArgumentCaptor.forClass(MarketResearchJob.class);
        verify(jobDao).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getKeyword()).isNull();
    }

    private CollectionGraphConfig collectionConfig() {
        CollectionGraphConfig config = new CollectionGraphConfig();
        config.getCollectMarketSalesTrend().setMonthCount(12);
        config.getCollectReviews().getPagination().setTargetCountPerAsin(20);
        return config;
    }

    @Test
    void shouldPageOwnedJobsAndAggregateLatestAnalysisAndPublishedArtifacts() {
        ResearchJobPageRequest request = new ResearchJobPageRequest();
        request.setCurrent(2L);
        request.setSize(20L);
        request.setKeyword("  facial device  ");
        request.setStatus(ResearchJobStatus.SUCCEEDED);
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setMonth("2026-07");
        MarketResearchJob job = historyJob();
        Page<MarketResearchJob> sourcePage = Page.of(2L, 20L, 21L);
        sourcePage.setRecords(List.of(job));
        when(jobDao.pageByUserId(
                        USER_ID,
                        "facial device",
                        ResearchJobStatus.SUCCEEDED.name(),
                        SellerSpriteMarketplace.US.getCode(),
                        "2026-07",
                        2L,
                        20L))
                .thenReturn(sourcePage);
        when(analysisRunDao.listByJobIdsAndUserId(List.of(JOB_ID), USER_ID))
                .thenReturn(List.of(
                        historyAnalysisRun("analysis-run-old", 100L, "FAILED", "failed", 60),
                        historyAnalysisRun("analysis-run-latest", 200L, "SUCCEEDED", "completed", 100)));
        when(artifactDao.listAvailableByJobIds(List.of(JOB_ID)))
                .thenReturn(List.of(historyArtifact()));

        PageResult<ResearchJobHistoryVo> result = jobService.page(request);

        assertThat(result.getCurrent()).isEqualTo(2L);
        assertThat(result.getSize()).isEqualTo(20L);
        assertThat(result.getTotal()).isEqualTo(21L);
        assertThat(result.getRecords()).singleElement().satisfies(record -> {
            assertThat(record.getJobId()).isEqualTo(JOB_ID);
            assertThat(record.getReportName()).isEqualTo("美容仪美国站调研");
            assertThat(record.getAnalysisRunId()).isEqualTo("analysis-run-latest");
            assertThat(record.getAnalysisStatus()).isEqualTo("SUCCEEDED");
            assertThat(record.getAnalysisPhase()).isEqualTo("completed");
            assertThat(record.getAnalysisProgress()).isEqualTo(100);
            assertThat(record.getArtifacts()).singleElement().satisfies(artifact -> {
                assertThat(artifact.getArtifactId()).isEqualTo("artifact-history-001");
                assertThat(artifact.getFileName()).isEqualTo("market-research.xlsx");
                assertThat(artifact.getFileSize()).isEqualTo(1024L);
            });
        });
        verify(jobDao).pageByUserId(
                USER_ID,
                "facial device",
                ResearchJobStatus.SUCCEEDED.name(),
                SellerSpriteMarketplace.US.getCode(),
                "2026-07",
                2L,
                20L);
    }

    @Test
    void shouldSkipAssociationQueriesForEmptyHistoryPage() {
        ResearchJobPageRequest request = new ResearchJobPageRequest();
        Page<MarketResearchJob> sourcePage = Page.of(1L, 20L, 0L);
        when(jobDao.pageByUserId(USER_ID, null, null, null, null, 1L, 20L))
                .thenReturn(sourcePage);

        PageResult<ResearchJobHistoryVo> result = jobService.page(request);

        assertThat(result.getRecords()).isEmpty();
        verify(analysisRunDao, never()).listByJobIdsAndUserId(any(), any());
        verify(artifactDao, never()).listAvailableByJobIds(any());
    }

    @Test
    void shouldScopeDetailLookupToCurrentOwner() {
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.detail(JOB_ID))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND));

        verify(jobDao).findByIdAndUserId(JOB_ID, USER_ID);
    }

    @Test
    void shouldExposePersistedTaskInputInDetail() {
        MarketResearchJob job = ownedJob();
        job.setReportName("美容仪市场调研");
        job.setMarketplace("US");
        job.setNodeIdPath("172282:281407");
        job.setResearchMonth("2026-07");
        job.setKeyword("facial device");
        job.setSeedAsins("[\"B000000001\",\"B000000002\"]");
        job.setCollectionConfig("{\"collectMarketSalesTrend\":{\"monthCount\":12}}");
        job.setJobStatus(ResearchJobStatus.RUNNING.name());
        job.setCurrentNode(ResearchPhase.COLLECT_PRODUCTS.getNodeCode());
        job.setAttemptCount(1);
        job.setMaxAttempts(3);
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(artifactDao.listAvailableByJobIds(List.of(JOB_ID))).thenReturn(List.of());

        ResearchJobDetailVo detail = jobService.detail(JOB_ID);

        assertThat(detail.getSeedAsins()).containsExactly("B000000001", "B000000002");
        assertThat(detail.getCollectionConfig().getCollectMarketSalesTrend().getMonthCount())
                .isEqualTo(12);
    }

    @Test
    void shouldDownloadOnlyWhenPublishedFileMatchesStoredSizeAndHash() throws Exception {
        Path report = temporaryDirectory.resolve("market-research.xlsx");
        Files.writeString(report, "deterministic-report-content", StandardCharsets.UTF_8);
        MarketResearchJob job = ownedJob();
        MarketResearchArtifact artifact = artifact(report);
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(artifactDao.getById("artifact-001")).thenReturn(artifact);
        FileSystemResource resource = new FileSystemResource(report);
        when(reportStorage.load(artifact.getStorageKey())).thenReturn(resource);

        ResearchDownload download = jobService.downloadArtifact(JOB_ID, "artifact-001");

        assertThat(download.fileName()).isEqualTo("market-research.xlsx");
        assertThat(download.mediaType()).isEqualTo(ResearchConstants.EXCEL_MEDIA_TYPE);
        assertThat(download.contentLength()).isEqualTo(Files.size(report));
        assertThat(download.resource()).isSameAs(resource);
        assertThat(download.resource().isReadable()).isTrue();
        assertThat(download.resource().getContentAsByteArray())
                .isEqualTo(Files.readAllBytes(report));
    }

    @Test
    void shouldRejectDownloadWhenPublishedFileHashWasTampered() throws Exception {
        Path report = temporaryDirectory.resolve("tampered.xlsx");
        Files.writeString(report, "tampered-content", StandardCharsets.UTF_8);
        MarketResearchArtifact artifact = artifact(report);
        artifact.setSha256("0".repeat(64));
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(ownedJob()));
        when(artifactDao.getById("artifact-001")).thenReturn(artifact);
        when(reportStorage.load(artifact.getStorageKey())).thenReturn(new FileSystemResource(report));

        assertThatThrownBy(() -> jobService.downloadArtifact(JOB_ID, "artifact-001"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ResultCode.MARKET_RESEARCH_REPORT_INVALID));
    }

    @Test
    void shouldCancelOwnedPendingJob() {
        MarketResearchJob job = ownedJob();
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(jobDao.cancelPending(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(USER_ID),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);

        jobService.cancel(JOB_ID);

        verify(jobDao).cancelPending(
                org.mockito.ArgumentMatchers.eq(JOB_ID),
                org.mockito.ArgumentMatchers.eq(USER_ID),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void shouldRetryOwnedFailedJobAndWakeDispatcher() {
        MarketResearchJob job = ownedJob();
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(jobDao.retryFailed(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(USER_ID),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(true);
        when(analysisService.prepareForResearchRetry(job)).thenReturn(analysisRun());

        jobService.retry(JOB_ID);

        verify(eventPublisher).publishEvent(new ResearchJobCreatedEvent(JOB_ID));
    }

    private MarketResearchJob ownedJob() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        job.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        return job;
    }

    private MarketResearchJob historyJob() {
        MarketResearchJob job = ownedJob();
        job.setReportName("美容仪美国站调研");
        job.setMarketplace("US");
        job.setNodeIdPath("172282:281407");
        job.setResearchMonth("2026-07");
        job.setKeyword("facial device");
        job.setJobStatus(ResearchJobStatus.SUCCEEDED.name());
        job.setProgress(100);
        job.setCreatedAt(1_722_470_400_000L);
        job.setFinishedAt(1_722_470_460_000L);
        return job;
    }

    private MarketResearchAnalysisRun historyAnalysisRun(
            String analysisRunId, long createdAt, String status, String phase, int progress) {
        MarketResearchAnalysisRun analysisRun = new MarketResearchAnalysisRun();
        analysisRun.setAnalysisRunId(analysisRunId);
        analysisRun.setJobId(JOB_ID);
        analysisRun.setUserId(USER_ID);
        analysisRun.setRunStatus(status);
        analysisRun.setCurrentPhase(phase);
        analysisRun.setProgress(progress);
        analysisRun.setCreatedAt(createdAt);
        return analysisRun;
    }

    private MarketResearchArtifact historyArtifact() {
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId("artifact-history-001");
        artifact.setJobId(JOB_ID);
        artifact.setArtifactType(ResearchConstants.ARTIFACT_TYPE_EVIDENCE_WORKBOOK);
        artifact.setFileName("market-research.xlsx");
        artifact.setMediaType(ResearchConstants.EXCEL_MEDIA_TYPE);
        artifact.setFileSize(1024L);
        artifact.setCreatedAt(1_722_470_450_000L);
        return artifact;
    }

    private void stubInitialAnalysis() {
        when(analysisService.createInitial(any(MarketResearchJob.class), any()))
                .thenReturn(analysisRun());
    }

    private ResearchAnalysisRunVo analysisRun() {
        return ResearchAnalysisRunVo.builder()
                .analysisRunId("analysis-run-001")
                .jobId(JOB_ID)
                .conversationId("conversation-001")
                .status("WAITING_RESEARCH")
                .build();
    }

    private MarketResearchArtifact artifact(Path report) throws Exception {
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId("artifact-001");
        artifact.setJobId(JOB_ID);
        artifact.setFileName("market-research.xlsx");
        artifact.setStorageKey("job-service-001/artifact-001.xlsx");
        artifact.setMediaType(ResearchConstants.EXCEL_MEDIA_TYPE);
        artifact.setFileSize(Files.size(report));
        artifact.setSha256(ResearchHashUtils.sha256(report));
        artifact.setArtifactStatus("PUBLISHED");
        return artifact;
    }
}
