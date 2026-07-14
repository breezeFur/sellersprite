package com.yuanbaomao.sellersprite.research.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.base.context.RequestContext;
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
import com.yuanbaomao.sellersprite.research.storage.ReportStorage;
import com.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
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
    private ReportStorage reportStorage;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @TempDir
    private Path temporaryDirectory;

    private ResearchJobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        jobService = new ResearchJobServiceImpl(
                jobDao,
                artifactDao,
                new ResearchProperties(),
                reportStorage,
                idGenerator,
                new ObjectMapper(),
                eventPublisher);
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
        request.setKeyword("  facial cleansing device  ");
        request.setSeedAsins(List.of(" b0test0001 ", "B0TEST0001", "B0TEST0002"));
        when(idGenerator.nextId()).thenReturn(JOB_ID);
        when(jobDao.save(any(MarketResearchJob.class))).thenReturn(true);

        ResearchJobCreatedVo result = jobService.create(request);

        ArgumentCaptor<MarketResearchJob> jobCaptor = ArgumentCaptor.forClass(MarketResearchJob.class);
        verify(jobDao).save(jobCaptor.capture());
        MarketResearchJob saved = jobCaptor.getValue();
        assertThat(saved.getJobId()).isEqualTo(JOB_ID);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getReportName()).isEqualTo("美容仪美国站市场调研");
        assertThat(saved.getKeyword()).isEqualTo("facial cleansing device");
        assertThat(saved.getSeedAsins()).isEqualTo("[\"B0TEST0001\",\"B0TEST0002\"]");
        assertThat(saved.getMarketplace()).isEqualTo(ResearchConstants.MARKETPLACE_US);
        assertThat(saved.getDataSourceMode()).isEqualTo("MOCK");
        assertThat(saved.getJobStatus()).isEqualTo(ResearchJobStatus.QUEUED.name());
        assertThat(saved.getCurrentPhase()).isEqualTo(ResearchPhase.VALIDATE.name());
        assertThat(result.getJobId()).isEqualTo(JOB_ID);
        assertThat(result.getStatus()).isEqualTo(ResearchJobStatus.QUEUED.name());
        verify(eventPublisher).publishEvent(new ResearchJobCreatedEvent(JOB_ID));
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
    void shouldDownloadOnlyWhenPublishedFileMatchesStoredSizeAndHash() throws Exception {
        Path report = temporaryDirectory.resolve("market-research.xlsx");
        Files.writeString(report, "deterministic-report-content", StandardCharsets.UTF_8);
        MarketResearchJob job = ownedJob();
        MarketResearchArtifact artifact = artifact(report);
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(artifactDao.findAvailableByJobId(JOB_ID)).thenReturn(Optional.of(artifact));
        when(reportStorage.load(artifact.getStorageKey())).thenReturn(new FileSystemResource(report));

        ResearchDownload download = jobService.download(JOB_ID);

        assertThat(download.fileName()).isEqualTo("market-research.xlsx");
        assertThat(download.mediaType()).isEqualTo(ResearchConstants.EXCEL_MEDIA_TYPE);
        assertThat(download.contentLength()).isEqualTo(Files.size(report));
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
        when(artifactDao.findAvailableByJobId(JOB_ID)).thenReturn(Optional.of(artifact));
        when(reportStorage.load(artifact.getStorageKey())).thenReturn(new FileSystemResource(report));

        assertThatThrownBy(() -> jobService.download(JOB_ID))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ResultCode.MARKET_RESEARCH_REPORT_INVALID));
    }

    private MarketResearchJob ownedJob() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        return job;
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
        return artifact;
    }
}
