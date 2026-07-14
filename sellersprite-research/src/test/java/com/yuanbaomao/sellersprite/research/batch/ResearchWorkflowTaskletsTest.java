package com.yuanbaomao.sellersprite.research.batch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ResearchWorkflowTaskletsTest {

    private static final String JOB_ID = "job-workflow-001";

    @Mock
    private ResearchJobStateService jobStateService;

    @Mock
    private ResearchInputService inputService;

    @Mock
    private ResearchSnapshotService snapshotService;

    @Mock
    private ResearchDataProvider dataProvider;

    @Mock
    private ResearchReportService reportService;

    @Mock
    private StepContribution contribution;

    @Mock
    private ChunkContext chunkContext;

    private ResearchWorkflowTasklets tasklets;
    private MarketResearchJob job;
    private ResearchInput input;

    @BeforeEach
    void setUp() {
        tasklets = new ResearchWorkflowTasklets(
                jobStateService,
                inputService,
                snapshotService,
                dataProvider,
                reportService,
                new ObjectMapper());
        job = validJob();
        input = ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace(ResearchConstants.MARKETPLACE_US)
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0TEST0001"))
                .build();
        stubJobId();
    }

    @Test
    void shouldExecuteAllEightFixedPhasesInOrder() {
        ResearchDataset dataset = new ResearchDataset(
                "fixture", "FIXTURE_OPERATION", new ObjectMapper().createObjectNode(), 1);
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

        tasklets.validate(contribution, chunkContext);
        tasklets.checkQuota(contribution, chunkContext);
        tasklets.collectMarketAndProducts(contribution, chunkContext);
        tasklets.collectKeywords(contribution, chunkContext);
        tasklets.collectReviews(contribution, chunkContext);
        tasklets.prepareData(contribution, chunkContext);
        tasklets.renderExcel(contribution, chunkContext);
        tasklets.validateAndPublish(contribution, chunkContext);

        InOrder stateOrder = inOrder(jobStateService);
        for (ResearchPhase phase : ResearchPhase.values()) {
            stateOrder.verify(jobStateService).enter(JOB_ID, phase);
            stateOrder.verify(jobStateService).advance(JOB_ID, phase);
        }
        verify(snapshotService).saveDatasets(
                eq(job), eq(ResearchPhase.CHECK_QUOTA), eq(input), any());
        verify(snapshotService).saveDatasets(
                eq(job), eq(ResearchPhase.COLLECT_MARKET_AND_PRODUCTS), eq(input), any());
        verify(snapshotService).saveDatasets(
                eq(job), eq(ResearchPhase.COLLECT_KEYWORDS), eq(input), any());
        verify(snapshotService).saveDatasets(
                eq(job), eq(ResearchPhase.COLLECT_REVIEWS), eq(input), any());
        verify(reportService).renderDraft(JOB_ID);
        verify(reportService).validateAndPublish(JOB_ID);
    }

    @Test
    void shouldSkipProviderWhenPhaseSnapshotAlreadyExists() {
        when(snapshotService.hasPhaseSnapshot(JOB_ID, ResearchPhase.COLLECT_KEYWORDS))
                .thenReturn(true);

        tasklets.collectKeywords(contribution, chunkContext);

        verify(jobStateService).enter(JOB_ID, ResearchPhase.COLLECT_KEYWORDS);
        verify(jobStateService).advance(JOB_ID, ResearchPhase.COLLECT_KEYWORDS);
        verify(dataProvider, never()).collectKeywords(any());
        verify(snapshotService, never()).saveDatasets(any(), any(), any(), any());
        verify(jobStateService, never()).requireJob(JOB_ID);
    }

    @Test
    void shouldNotAdvancePhaseWhenCollectionFails() {
        when(jobStateService.requireJob(JOB_ID)).thenReturn(job);
        when(inputService.from(job)).thenReturn(input);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.MOCK);
        when(dataProvider.collectMarketAndProducts(input))
                .thenThrow(new IllegalStateException("remote unavailable"));

        assertThatThrownBy(() -> tasklets.collectMarketAndProducts(contribution, chunkContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("remote unavailable");

        verify(jobStateService).enter(JOB_ID, ResearchPhase.COLLECT_MARKET_AND_PRODUCTS);
        verify(jobStateService, never())
                .advance(JOB_ID, ResearchPhase.COLLECT_MARKET_AND_PRODUCTS);
        verify(snapshotService, never()).saveDatasets(any(), any(), any(), any());
    }

    @Test
    void shouldRejectProviderModeChangedAfterJobCreation() {
        when(jobStateService.requireJob(JOB_ID)).thenReturn(job);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.REMOTE);

        assertThatThrownBy(() -> tasklets.collectMarketAndProducts(contribution, chunkContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("任务数据源模式为MOCK")
                .hasMessageContaining("当前服务数据源模式为REMOTE");

        verify(inputService, never()).from(any());
        verify(dataProvider, never()).collectMarketAndProducts(any());
        verify(snapshotService, never()).saveDatasets(any(), any(), any(), any());
    }

    @Test
    void shouldNotAdvancePhaseWhenExcelRenderingFails() {
        doThrow(new IllegalStateException("template invalid"))
                .when(reportService).renderDraft(JOB_ID);

        assertThatThrownBy(() -> tasklets.renderExcel(contribution, chunkContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("template invalid");

        verify(jobStateService).enter(JOB_ID, ResearchPhase.RENDER_EXCEL);
        verify(jobStateService, never()).advance(JOB_ID, ResearchPhase.RENDER_EXCEL);
    }

    private void stubJobId() {
        StepExecution stepExecution = org.mockito.Mockito.mock(StepExecution.class);
        JobExecution jobExecution = org.mockito.Mockito.mock(JobExecution.class);
        JobParameters jobParameters = org.mockito.Mockito.mock(JobParameters.class);
        when(contribution.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getJobParameters()).thenReturn(jobParameters);
        when(jobParameters.getString(ResearchConstants.JOB_ID_PARAMETER)).thenReturn(JOB_ID);
    }

    private MarketResearchJob validJob() {
        MarketResearchJob value = new MarketResearchJob();
        value.setJobId(JOB_ID);
        value.setReportName("美容仪市场调研");
        value.setMarketplace(ResearchConstants.MARKETPLACE_US);
        value.setKeyword("facial cleansing device");
        value.setDataSourceMode("MOCK");
        return value;
    }

    private MarketResearchSnapshot snapshot(ResearchPhase phase) {
        MarketResearchSnapshot value = new MarketResearchSnapshot();
        value.setJobId(JOB_ID);
        value.setPhase(phase.name());
        return value;
    }
}
