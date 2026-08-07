package cyou.yuanbaomao.sellersprite.research.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceService;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import cyou.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchWorkflowStepServiceTest {

    private static final String JOB_ID = "job-evidence-workflow-001";
    private static final String EXECUTION_TOKEN = "execution-token-a";

    @Mock
    private ResearchJobStateService jobStateService;
    @Mock
    private ResearchNodeExecutionService nodeExecutionService;
    @Mock
    private ResearchInputService inputService;
    @Mock
    private ResearchStageInputService stageInputService;
    @Mock
    private ResearchDatasetService datasetService;
    @Mock
    private ResearchDataProvider dataProvider;
    @Mock
    private ResearchReportService reportService;
    @Mock
    private ResearchAnalysisStagePort analysisStagePort;
    @Mock
    private ResearchEvidenceService evidenceService;

    private ObjectMapper objectMapper;
    private ResearchWorkflowStepService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ResearchWorkflowStepService(
                jobStateService,
                nodeExecutionService,
                inputService,
                stageInputService,
                datasetService,
                dataProvider,
                reportService,
                analysisStagePort,
                evidenceService,
                objectMapper);
    }

    @Test
    void shouldDispatchIndependentProductCollection() {
        ResearchPhase phase = ResearchPhase.COLLECT_PRODUCTS;
        MarketResearchJob job = job();
        ResearchInput input = input(List.of("B0TEST0001"));
        List<ResearchDataset> result = List.of(new ResearchDataset(
                "products", "PRODUCT_RESEARCH", objectMapper.createObjectNode(), 1));
        stubExecution(job, phase);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.REMOTE);
        when(inputService.from(job)).thenReturn(input);
        when(dataProvider.collectProducts(input)).thenReturn(result);

        service.execute(JOB_ID, EXECUTION_TOKEN, phase);

        verify(dataProvider).collectProducts(input);
        verify(datasetService).saveDatasets(job, phase, input, result);
        verify(jobStateService).advance(JOB_ID, EXECUTION_TOKEN, phase);
    }

    @Test
    void shouldDispatchMarketSalesTrendCollection() {
        ResearchPhase phase = ResearchPhase.COLLECT_MARKET_SALES_TREND;
        MarketResearchJob job = job();
        ResearchInput input = input(List.of());
        List<ResearchDataset> result = List.of(new ResearchDataset(
                "market.sales-trend", "MARKET_STATISTICS", objectMapper.createArrayNode(), 5));
        stubExecution(job, phase);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.REMOTE);
        when(inputService.from(job)).thenReturn(input);
        when(dataProvider.collectMarketSalesTrend(input)).thenReturn(result);

        service.execute(JOB_ID, EXECUTION_TOKEN, phase);

        verify(dataProvider).collectMarketSalesTrend(input);
        verify(datasetService).saveDatasets(job, phase, input, result);
    }

    @Test
    void shouldAlwaysUseHumanSelectedAsinsForReviewCollection() {
        ResearchPhase phase = ResearchPhase.COLLECT_REVIEWS;
        MarketResearchJob job = job();
        ResearchInput input = input(List.of());
        ResearchInput enriched = input(List.of("B0FIRST001", "B0SECOND02"));
        List<ResearchDataset> reviews = List.of(new ResearchDataset(
                "reviews.B0FIRST001", "REVIEW_LIST", objectMapper.createObjectNode(), 1));
        stubExecution(job, phase);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.REMOTE);
        when(inputService.from(job)).thenReturn(input);
        when(stageInputService.findSelection(JOB_ID)).thenReturn(java.util.Optional.of(
                new ResearchProductSelection(
                        ResearchSelectionDecision.ENTER,
                        List.of("B0FIRST001", "B0SECOND02"))));
        when(inputService.withSeedAsins(input, List.of("B0FIRST001", "B0SECOND02")))
                .thenReturn(enriched);
        when(dataProvider.collectReviews(enriched)).thenReturn(reviews);

        service.execute(JOB_ID, EXECUTION_TOKEN, phase);

        verify(dataProvider).collectReviews(enriched);
        verify(datasetService).saveDatasets(job, phase, enriched, reviews);
    }

    @Test
    void shouldAlwaysUseHumanSelectedAsinsForKeywordIntelligenceCollection() {
        ResearchPhase phase = ResearchPhase.COLLECT_KEYWORD_INTELLIGENCE;
        MarketResearchJob job = job();
        ResearchInput input = input(List.of("B0CREATED01"));
        ResearchInput selectedInput = input(List.of("B0FIRST001", "B0SECOND02"));
        List<ResearchDataset> keywords = List.of(new ResearchDataset(
                "traffic-keywords.B0FIRST001",
                "TRAFFIC_KEYWORD",
                objectMapper.createObjectNode(),
                1));
        stubExecution(job, phase);
        when(dataProvider.sourceMode()).thenReturn(ResearchSourceMode.REMOTE);
        when(inputService.from(job)).thenReturn(input);
        when(stageInputService.findSelection(JOB_ID)).thenReturn(java.util.Optional.of(
                new ResearchProductSelection(
                        ResearchSelectionDecision.ENTER,
                        List.of("B0FIRST001", "B0SECOND02"))));
        when(inputService.withSeedAsins(input, List.of("B0FIRST001", "B0SECOND02")))
                .thenReturn(selectedInput);
        when(dataProvider.collectKeywordIntelligence(selectedInput)).thenReturn(keywords);

        service.execute(JOB_ID, EXECUTION_TOKEN, phase);

        verify(dataProvider).collectKeywordIntelligence(selectedInput);
        verify(datasetService).saveDatasets(job, phase, selectedInput, keywords);
    }

    @Test
    void shouldPersistEvidencePreparedByDeterministicService() {
        ResearchPhase phase = ResearchPhase.PREPARE_US_EVIDENCE;
        MarketResearchJob job = job();
        ResearchInput input = input(List.of());
        ResearchDataset evidence = new ResearchDataset(
                "evidence.products", "PREPARE_EVIDENCE", objectMapper.createObjectNode(), 1);
        stubExecution(job, phase);
        when(inputService.from(job)).thenReturn(input);
        when(evidenceService.prepare(job, phase)).thenReturn(evidence);

        service.execute(JOB_ID, EXECUTION_TOKEN, phase);

        verify(evidenceService).prepare(job, phase);
        verify(datasetService).saveDatasets(job, phase, input, List.of(evidence));
    }

    @Test
    void shouldValidateAllEvidenceBeforeRendering() {
        ResearchPhase phase = ResearchPhase.VALIDATE_EVIDENCE;
        MarketResearchJob job = job();
        stubExecution(job, phase);

        service.execute(JOB_ID, EXECUTION_TOKEN, phase);

        verify(evidenceService).validate(JOB_ID);
    }

    private void stubExecution(MarketResearchJob job, ResearchPhase phase) {
        when(jobStateService.requireJob(JOB_ID)).thenReturn(job);
        when(nodeExecutionService.begin(job, phase)).thenReturn(new MarketResearchNodeExecution());
        if (phase != ResearchPhase.VALIDATE_EVIDENCE) {
            when(datasetService.hasValidNodeDatasets(JOB_ID, phase)).thenReturn(false);
        }
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setDataSourceMode(ResearchSourceMode.REMOTE.name());
        return job;
    }

    private ResearchInput input(List<String> seedAsins) {
        return ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace("US")
                .nodeIdPath("172282:281407")
                .month("2026-07")
                .keyword("facial cleansing device")
                .seedAsins(seedAsins)
                .build();
    }
}
