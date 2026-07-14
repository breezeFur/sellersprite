package com.yuanbaomao.sellersprite.research.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.research.config.ResearchBatchConfiguration;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import com.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;
import com.yuanbaomao.sellersprite.research.service.ResearchInputService;
import com.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import com.yuanbaomao.sellersprite.research.service.ResearchReportService;
import com.yuanbaomao.sellersprite.research.service.ResearchSnapshotService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBatchTest
@SpringBootTest(
        classes = ResearchBatchIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:research-batch;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:research-test-schema.sql",
                "spring.batch.jdbc.initialize-schema=always",
                "spring.batch.job.enabled=false"
        })
class ResearchBatchIntegrationTest {

    private static final String JOB_ID = "job-batch-integration-001";
    private static final Duration MAX_EXECUTION_TIME = Duration.ofSeconds(10);

    @Autowired
    private Job researchJob;

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ResearchJobStateService jobStateService;

    @MockitoBean
    private ResearchInputService inputService;

    @MockitoBean
    private ResearchSnapshotService snapshotService;

    @MockitoBean
    private ResearchDataProvider dataProvider;

    @MockitoBean
    private ResearchReportService reportService;

    @BeforeEach
    void setUp() {
        jobOperatorTestUtils.setJob(researchJob);
        jobRepositoryTestUtils.removeJobExecutions();
        MarketResearchJob job = validJob();
        ResearchInput input = ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace(ResearchConstants.MARKETPLACE_US)
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0TEST0001"))
                .build();
        ResearchDataset dataset = new ResearchDataset(
                "fixture", "FIXTURE_OPERATION", tools.jackson.databind.node.JsonNodeFactory.instance.objectNode(), 1);
        when(jobStateService.requireJob(JOB_ID)).thenReturn(job);
        when(inputService.from(job)).thenReturn(input);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.MOCK);
        when(dataProvider.checkQuota(input)).thenReturn(List.of(dataset));
        when(dataProvider.collectMarketAndProducts(input)).thenReturn(List.of(dataset));
        when(dataProvider.collectKeywords(input)).thenReturn(List.of(dataset));
        when(dataProvider.collectReviews(input)).thenReturn(List.of(dataset));
        when(snapshotService.listByJobId(JOB_ID)).thenReturn(List.of(
                snapshot(ResearchPhase.CHECK_QUOTA),
                snapshot(ResearchPhase.COLLECT_MARKET_AND_PRODUCTS),
                snapshot(ResearchPhase.COLLECT_KEYWORDS),
                snapshot(ResearchPhase.COLLECT_REVIEWS)));
    }

    @Test
    void shouldPersistAndCompleteAllEightWorkflowStepsOnH2() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addString(ResearchConstants.JOB_ID_PARAMETER, JOB_ID)
                .toJobParameters();

        JobExecution execution = jobOperatorTestUtils.startJob(parameters);
        waitForCompletion(execution);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions())
                .hasSize(ResearchPhase.values().length)
                .allSatisfy(stepExecution -> assertThat(stepExecution.getStatus())
                        .isEqualTo(BatchStatus.COMPLETED));
        assertThat(execution.getStepExecutions())
                .extracting(stepExecution -> stepExecution.getStepName())
                .containsExactlyInAnyOrder(
                        "validateResearchStep",
                        "checkResearchQuotaStep",
                        "collectMarketAndProductsStep",
                        "collectKeywordsStep",
                        "collectReviewsStep",
                        "prepareResearchDataStep",
                        "renderResearchExcelStep",
                        "validateAndPublishResearchStep");

        Integer persistedSteps = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID = ?",
                Integer.class,
                execution.getId());
        assertThat(persistedSteps).isEqualTo(ResearchPhase.values().length);
        assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'MARKET_RESEARCH_JOB'",
                        Integer.class))
                .isOne();

        verify(jobStateService).markRunning(JOB_ID, execution);
        verify(jobStateService).markSucceeded(JOB_ID);
        for (ResearchPhase phase : ResearchPhase.values()) {
            verify(jobStateService).enter(JOB_ID, phase);
            verify(jobStateService).advance(JOB_ID, phase);
        }
        verify(snapshotService, org.mockito.Mockito.times(4))
                .saveDatasets(any(), any(), any(), any());
        verify(reportService).renderDraft(JOB_ID);
        verify(reportService).validateAndPublish(JOB_ID);
    }

    @Test
    void shouldRestartFromFailedKeywordStepWithoutRepeatingCompletedCollection() throws Exception {
        ResearchInput input = inputService.from(validJob());
        ResearchDataset dataset = new ResearchDataset(
                "fixture", "FIXTURE_OPERATION", tools.jackson.databind.node.JsonNodeFactory.instance.objectNode(), 1);
        when(dataProvider.collectKeywords(input))
                .thenThrow(new IllegalStateException("模拟关键词接口失败"))
                .thenReturn(List.of(dataset));
        JobParameters parameters = new JobParametersBuilder()
                .addString(ResearchConstants.JOB_ID_PARAMETER, JOB_ID)
                .toJobParameters();

        JobExecution failed = jobOperatorTestUtils.startJob(parameters);
        waitForCompletion(failed);
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);

        JobExecution restarted = jobOperatorTestUtils.getJobOperator().restart(failed);
        waitForCompletion(restarted);

        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(dataProvider).checkQuota(input);
        verify(dataProvider).collectMarketAndProducts(input);
        verify(dataProvider, org.mockito.Mockito.times(2)).collectKeywords(input);
        verify(dataProvider).collectReviews(input);
    }

    private void waitForCompletion(JobExecution execution) throws InterruptedException {
        Instant deadline = Instant.now().plus(MAX_EXECUTION_TIME);
        while (execution.getStatus().isRunning() && Instant.now().isBefore(deadline)) {
            Thread.sleep(25);
        }
    }

    private MarketResearchJob validJob() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setReportName("美容仪市场调研");
        job.setMarketplace(ResearchConstants.MARKETPLACE_US);
        job.setKeyword("facial cleansing device");
        job.setDataSourceMode("MOCK");
        return job;
    }

    private MarketResearchSnapshot snapshot(ResearchPhase phase) {
        MarketResearchSnapshot snapshot = new MarketResearchSnapshot();
        snapshot.setJobId(JOB_ID);
        snapshot.setPhase(phase.name());
        return snapshot;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            ResearchBatchConfiguration.class,
            ResearchWorkflowTasklets.class,
            ResearchJobExecutionListener.class
    })
    static class TestApplication {
    }
}
