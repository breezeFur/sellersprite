package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchDatasetDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchStageInputDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchStageInput;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchWaitingInputType;
import cyou.yuanbaomao.sellersprite.research.event.ResearchJobCreatedEvent;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchProductSelectionRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchStageInputServiceTest {

    private static final String JOB_ID = "job-stage-input-001";
    private static final String USER_ID = "user-stage-input-001";

    @Mock
    private MarketResearchJobDao jobDao;
    @Mock
    private MarketResearchStageInputDao stageInputDao;
    @Mock
    private MarketResearchDatasetDao datasetDao;
    @Mock
    private ResearchDatasetService datasetService;
    @Mock
    private ResearchInputService inputService;
    @Mock
    private ResearchSseEventPublisher eventPublisher;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private IdGenerator idGenerator;

    private ObjectMapper objectMapper;
    private ResearchStageInputService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ResearchStageInputService(
                jobDao,
                stageInputDao,
                datasetDao,
                datasetService,
                inputService,
                eventPublisher,
                applicationEventPublisher,
                idGenerator,
                objectMapper);
        RequestContextHolder.set(RequestContext.builder().userId(USER_ID).build());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldPersistSelectedTopProductsAndRequeueInOneServiceCall() {
        MarketResearchJob job = waitingJob();
        MarketResearchDataset products = new MarketResearchDataset();
        products.setDatasetCode("selection.productCandidates");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items")
                .addObject()
                .put("rank", 1)
                .put("asin", "B0TOP00001")
                .put("title", "Top product")
                .put("units", 321);
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(stageInputDao.find(JOB_ID, "SCREENING", "PRODUCT_SELECTION"))
                .thenReturn(Optional.empty());
        when(datasetDao.listByJobId(JOB_ID)).thenReturn(List.of(products));
        when(datasetService.readPayload(products)).thenReturn(payload);
        when(idGenerator.nextId()).thenReturn("stage-input-001");
        when(stageInputDao.save(any(MarketResearchStageInput.class))).thenReturn(true);
        when(jobDao.requeueWaitingInput(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(USER_ID),
                        org.mockito.ArgumentMatchers.eq(
                                ResearchWaitingInputType.PRODUCT_SELECTION.name()),
                        anyLong()))
                .thenReturn(true);
        ResearchProductSelectionRequest request = new ResearchProductSelectionRequest();
        request.setDecision(ResearchSelectionDecision.ENTER);
        request.setSelectedAsins(List.of(" b0top00001 "));

        var result = service.submitForCurrentUser(JOB_ID, request);

        ArgumentCaptor<MarketResearchStageInput> inputCaptor =
                ArgumentCaptor.forClass(MarketResearchStageInput.class);
        verify(stageInputDao).save(inputCaptor.capture());
        assertThat(inputCaptor.getValue().getDecision()).isEqualTo("ENTER");
        assertThat(inputCaptor.getValue().getInputPayload()).contains("B0TOP00001");
        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        assertThat(result.getSelectedAsins()).containsExactly("B0TOP00001");
        assertThat(result.getCandidates()).singleElement().satisfies(candidate -> {
            assertThat(candidate.getRank()).isEqualTo(1);
            assertThat(candidate.getAsin()).isEqualTo("B0TOP00001");
            assertThat(candidate.getUnits()).isEqualTo("321");
        });
        verify(applicationEventPublisher).publishEvent(new ResearchJobCreatedEvent(JOB_ID));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void shouldPersistFirstTwentyCandidatesInEvidenceOrderOnlyOnce() {
        MarketResearchJob job = waitingJob();
        MarketResearchDataset evidenceProducts = new MarketResearchDataset();
        evidenceProducts.setDatasetCode("evidence.products");
        ObjectNode evidencePayload = objectMapper.createObjectNode();
        var sourceItems = evidencePayload.putArray("items");
        for (int index = 1; index <= 21; index++) {
            sourceItems.addObject()
                    .put("ASIN", String.format("B0TOP%05d", index))
                    .put("标题", "Product " + index)
                    .put("月销量", index * 10);
        }
        MarketResearchDataset prepared = new MarketResearchDataset();
        prepared.setDatasetCode("selection.productCandidates");
        ResearchInput input = ResearchInput.builder().jobId(JOB_ID).build();
        when(datasetDao.listByJobId(JOB_ID))
                .thenReturn(List.of(evidenceProducts), List.of(prepared));
        when(jobDao.getById(JOB_ID)).thenReturn(job);
        when(datasetService.readPayload(evidenceProducts)).thenReturn(evidencePayload);
        when(inputService.from(job)).thenReturn(input);

        service.prepareProductCandidates(JOB_ID);
        service.prepareProductCandidates(JOB_ID);

        ArgumentCaptor<List<ResearchDataset>> datasetsCaptor =
                ArgumentCaptor.forClass((Class) List.class);
        verify(datasetService).saveDatasets(
                org.mockito.ArgumentMatchers.eq(job),
                org.mockito.ArgumentMatchers.eq(ResearchPhase.PREPARE_US_EVIDENCE),
                org.mockito.ArgumentMatchers.eq(input),
                datasetsCaptor.capture());
        assertThat(datasetsCaptor.getValue()).singleElement().satisfies(dataset -> {
            assertThat(dataset.getDatasetCode()).isEqualTo("selection.productCandidates");
            assertThat(dataset.getOperation()).isEqualTo("PREPARE_PRODUCT_CANDIDATES");
            assertThat(dataset.getRecordCount()).isEqualTo(20);
            assertThat(dataset.getPayload().path("items")).hasSize(20);
            List<String> asins = new ArrayList<>();
            dataset.getPayload().path("items").forEach(row -> asins.add(row.path("asin").asText()));
            assertThat(asins).containsExactly(
                    "B0TOP00001", "B0TOP00002", "B0TOP00003", "B0TOP00004", "B0TOP00005",
                    "B0TOP00006", "B0TOP00007", "B0TOP00008", "B0TOP00009", "B0TOP00010",
                    "B0TOP00011", "B0TOP00012", "B0TOP00013", "B0TOP00014", "B0TOP00015",
                    "B0TOP00016", "B0TOP00017", "B0TOP00018", "B0TOP00019", "B0TOP00020");
            assertThat(dataset.getPayload().at("/items/0/rank").asInt()).isEqualTo(1);
            assertThat(dataset.getPayload().at("/items/19/rank").asInt()).isEqualTo(20);
        });
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void shouldDeduplicateChildVariantsByParentAsin() {
        MarketResearchJob job = waitingJob();
        MarketResearchDataset evidenceProducts = new MarketResearchDataset();
        evidenceProducts.setDatasetCode("evidence.products");
        ObjectNode evidencePayload = objectMapper.createObjectNode();
        var sourceItems = evidencePayload.putArray("items");

        sourceItems.addObject().put("ASIN", "B0CHILD01A").put("父体ASIN", "B0PARENT01").put("变体数", 3);
        sourceItems.addObject().put("ASIN", "B0CHILD01B").put("父体ASIN", "B0PARENT01").put("变体数", 3);
        sourceItems.addObject().put("ASIN", "B0CHILD01C").put("父体ASIN", "B0PARENT01").put("变体数", 3);
        sourceItems.addObject().put("ASIN", "B0SINGLE02").put("父体ASIN", "").put("变体数", 1);
        sourceItems.addObject().put("ASIN", "B0CHILD03A").put("父体ASIN", "B0PARENT03").put("变体数", 2);
        sourceItems.addObject().put("ASIN", "B0CHILD03B").put("父体ASIN", "B0PARENT03").put("变体数", 2);

        when(datasetDao.listByJobId(JOB_ID)).thenReturn(List.of(evidenceProducts));
        when(jobDao.getById(JOB_ID)).thenReturn(job);
        when(datasetService.readPayload(evidenceProducts)).thenReturn(evidencePayload);
        when(inputService.from(job)).thenReturn(ResearchInput.builder().jobId(JOB_ID).build());

        service.prepareProductCandidates(JOB_ID);

        ArgumentCaptor<List<ResearchDataset>> datasetsCaptor =
                ArgumentCaptor.forClass((Class) List.class);
        verify(datasetService).saveDatasets(
                any(), any(), any(), datasetsCaptor.capture());

        List<ResearchDataset> saved = datasetsCaptor.getValue();
        assertThat(saved).singleElement().satisfies(dataset -> {
            assertThat(dataset.getRecordCount()).isEqualTo(3);
            List<String> asins = new ArrayList<>();
            dataset.getPayload().path("items").forEach(row -> asins.add(row.path("asin").asText()));
            assertThat(asins).containsExactly("B0CHILD01A", "B0SINGLE02", "B0CHILD03A");
        });
    }

    @Test
    void shouldRejectSelectionWhenJobIsNotWaitingForProductInput() {
        MarketResearchJob job = waitingJob();
        job.setJobStatus(ResearchJobStatus.RUNNING.name());
        when(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(stageInputDao.find(JOB_ID, "SCREENING", "PRODUCT_SELECTION"))
                .thenReturn(Optional.empty());
        ResearchProductSelectionRequest request = new ResearchProductSelectionRequest();
        request.setDecision(ResearchSelectionDecision.ABANDON);

        assertThatThrownBy(() -> service.submitForCurrentUser(JOB_ID, request))
                .isInstanceOfSatisfying(BizException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ResultCode.MARKET_RESEARCH_INPUT_NOT_ACCEPTABLE));
    }

    private MarketResearchJob waitingJob() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        job.setJobStatus(ResearchJobStatus.WAITING_INPUT.name());
        job.setWaitingInputType(ResearchWaitingInputType.PRODUCT_SELECTION.name());
        return job;
    }
}
