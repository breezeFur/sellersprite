package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchDatasetDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchDatasetValidationStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchDatasetServiceTest {

    private static final String JOB_ID = "job-dataset-001";
    private static final String DATASET_ID = "dataset-001";

    @Mock
    private MarketResearchDatasetDao datasetDao;

    @Mock
    private IdGenerator idGenerator;

    private ObjectMapper objectMapper;
    private ResearchDatasetService datasetService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        datasetService = new ResearchDatasetService(datasetDao, idGenerator, objectMapper);
    }

    @Test
    void shouldPersistImmutableDatasetWithStableHashesAndSchemaVersion() throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items").addObject().put("asin", "B0TEST0001");
        ResearchDataset source = new ResearchDataset("products", "PRODUCT_RESEARCH", payload, 1);
        when(datasetDao.findByIdempotencyKey(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(ResearchPhase.COLLECT_PRODUCTS.getNodeCode()),
                        org.mockito.ArgumentMatchers.eq("PRODUCT_RESEARCH"),
                        org.mockito.ArgumentMatchers.eq("products"),
                        anyString()))
                .thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(DATASET_ID);
        when(datasetDao.save(any(MarketResearchDataset.class))).thenReturn(true);

        datasetService.saveDatasets(
                job(), ResearchPhase.COLLECT_PRODUCTS, input(), List.of(source));

        ArgumentCaptor<MarketResearchDataset> captor =
                ArgumentCaptor.forClass(MarketResearchDataset.class);
        verify(datasetDao).save(captor.capture());
        MarketResearchDataset saved = captor.getValue();
        String expectedPayload = objectMapper.writeValueAsString(payload);
        String expectedRequest = objectMapper.writeValueAsString(input());
        assertThat(saved.getDatasetId()).isEqualTo(DATASET_ID);
        assertThat(saved.getJobId()).isEqualTo(JOB_ID);
        assertThat(saved.getNodeCode())
                .isEqualTo(ResearchPhase.COLLECT_PRODUCTS.getNodeCode());
        assertThat(saved.getDatasetCode()).isEqualTo("products");
        assertThat(saved.getRequestHash()).isEqualTo(ResearchHashUtils.sha256(expectedRequest));
        assertThat(saved.getRequestPayload()).isEqualTo(expectedRequest);
        assertThat(saved.getSourcePayload()).isEqualTo(expectedPayload);
        assertThat(saved.getSha256()).isEqualTo(ResearchHashUtils.sha256(expectedPayload));
        assertThat(saved.getSchemaVersion()).isEqualTo(ResearchConstants.DATASET_SCHEMA_VERSION);
        assertThat(saved.getValidationStatus())
                .isEqualTo(ResearchDatasetValidationStatus.VALID.name());
    }

    @Test
    void shouldReuseOnlyAnIntactDatasetForTheSameIdempotencyKey() {
        MarketResearchDataset existing = intactDataset("{}");
        when(datasetDao.findByIdempotencyKey(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(ResearchPhase.COLLECT_PRODUCTS.getNodeCode()),
                        org.mockito.ArgumentMatchers.eq("PRODUCT_RESEARCH"),
                        org.mockito.ArgumentMatchers.eq("products"),
                        anyString()))
                .thenReturn(Optional.of(existing));

        datasetService.saveDatasets(
                job(),
                ResearchPhase.COLLECT_PRODUCTS,
                input(),
                List.of(new ResearchDataset(
                        "products", "PRODUCT_RESEARCH", objectMapper.createObjectNode(), 0)));

        verify(datasetDao, never()).save(any());
        verify(datasetDao, never()).updateById(any());
        verify(idGenerator, never()).nextId();
    }

    @Test
    void shouldIsolateStageTwoRequestHashesBySelectedAsinSnapshot() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items");
        ResearchDataset source = new ResearchDataset(
                "reviews.selection", "REVIEW_LIST", payload, 0);
        when(datasetDao.findByIdempotencyKey(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(ResearchPhase.COLLECT_REVIEWS.getNodeCode()),
                        org.mockito.ArgumentMatchers.eq("REVIEW_LIST"),
                        org.mockito.ArgumentMatchers.eq("reviews.selection"),
                        anyString()))
                .thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn("dataset-selection-a", "dataset-selection-b");
        when(datasetDao.save(any(MarketResearchDataset.class))).thenReturn(true);

        datasetService.saveDatasets(
                job(),
                ResearchPhase.COLLECT_REVIEWS,
                input(List.of("B0FIRST001")),
                List.of(source));
        datasetService.saveDatasets(
                job(),
                ResearchPhase.COLLECT_REVIEWS,
                input(List.of("B0SECOND02")),
                List.of(source));

        ArgumentCaptor<MarketResearchDataset> captor =
                ArgumentCaptor.forClass(MarketResearchDataset.class);
        verify(datasetDao, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(MarketResearchDataset::getRequestHash)
                .doesNotHaveDuplicates();
        assertThat(captor.getAllValues().get(0).getRequestPayload()).contains("B0FIRST001");
        assertThat(captor.getAllValues().get(1).getRequestPayload()).contains("B0SECOND02");
    }

    @Test
    void shouldRejectTamperedDatasetInsteadOfMutatingHistory() {
        MarketResearchDataset corrupted = intactDataset("broken");
        corrupted.setSha256("0".repeat(64));
        when(datasetDao.findByIdempotencyKey(
                        org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.eq(ResearchPhase.COLLECT_PRODUCTS.getNodeCode()),
                        org.mockito.ArgumentMatchers.eq("PRODUCT_RESEARCH"),
                        org.mockito.ArgumentMatchers.eq("products"),
                        anyString()))
                .thenReturn(Optional.of(corrupted));

        assertThatThrownBy(() -> datasetService.saveDatasets(
                        job(),
                        ResearchPhase.COLLECT_PRODUCTS,
                        input(),
                        List.of(new ResearchDataset(
                                "products", "PRODUCT_RESEARCH", objectMapper.createObjectNode(), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("完整性校验失败");
        verify(datasetDao, never()).save(any());
        verify(datasetDao, never()).updateById(any());
    }

    @Test
    void shouldRejectTamperedPayloadAndNodeReuse() {
        MarketResearchDataset corrupted = intactDataset("{\"items\":[]}");
        corrupted.setNodeCode(ResearchPhase.COLLECT_KEYWORD_INTELLIGENCE.getNodeCode());
        corrupted.setSha256(ResearchHashUtils.sha256("{\"items\":[1]}"));
        when(datasetDao.listByJobId(JOB_ID)).thenReturn(List.of(corrupted));

        assertThat(datasetService.hasValidNodeDatasets(JOB_ID, ResearchPhase.COLLECT_KEYWORD_INTELLIGENCE))
                .isFalse();
        assertThatThrownBy(() -> datasetService.readPayload(corrupted))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHA-256不匹配");
    }

    @Test
    void shouldReadOnlyTheTwelveEvidenceDatasetScopes() {
        String payload = "{\"sheetName\":\"US\",\"columns\":[\"ASIN\"],\"items\":[]}";
        MarketResearchDataset evidence = intactDataset(payload);
        evidence.setNodeCode(ResearchPhase.PREPARE_US_EVIDENCE.getNodeCode());
        evidence.setDatasetCode("evidence.products");
        evidence.setOperation("PREPARE_EVIDENCE");
        evidence.setRecordCount(0);
        List<String> evidenceNodeCodes = ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(definition -> definition.phase().getNodeCode())
                .toList();
        List<String> evidenceDatasetCodes = ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        when(datasetDao.listByJobIdAndNodeCodesAndDatasetCodes(
                        JOB_ID, evidenceNodeCodes, evidenceDatasetCodes))
                .thenReturn(List.of(evidence));

        List<ResearchDataset> datasets = datasetService.readEvidenceDatasets(JOB_ID);

        assertThat(evidenceNodeCodes).hasSize(12);
        assertThat(evidenceDatasetCodes).hasSize(12);
        assertThat(datasets).singleElement().satisfies(dataset -> {
            assertThat(dataset.getDatasetCode()).isEqualTo("evidence.products");
            assertThat(dataset.getPayload().path("sheetName").asText()).isEqualTo("US");
        });
        verify(datasetDao, never()).listByJobId(JOB_ID);
    }

    @Test
    void shouldReadEvidenceDatasetScopesByStage() {
        List<String> screeningNodeCodes = ResearchEvidenceCatalog.SCREENING_DEFINITIONS.stream()
                .map(definition -> definition.phase().getNodeCode())
                .toList();
        List<String> screeningDatasetCodes = ResearchEvidenceCatalog.SCREENING_DEFINITIONS.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        List<String> deepDiveNodeCodes = ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS.stream()
                .map(definition -> definition.phase().getNodeCode())
                .toList();
        List<String> deepDiveDatasetCodes = ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        when(datasetDao.listByJobIdAndNodeCodesAndDatasetCodes(
                        JOB_ID, screeningNodeCodes, screeningDatasetCodes))
                .thenReturn(List.of());
        when(datasetDao.listByJobIdAndNodeCodesAndDatasetCodes(
                        JOB_ID, deepDiveNodeCodes, deepDiveDatasetCodes))
                .thenReturn(List.of());

        assertThat(datasetService.readEvidenceDatasets(JOB_ID, EvidenceStage.SCREENING)).isEmpty();
        assertThat(datasetService.readEvidenceDatasets(JOB_ID, EvidenceStage.DEEP_DIVE)).isEmpty();

        assertThat(screeningNodeCodes).hasSize(7);
        assertThat(screeningDatasetCodes).hasSize(7);
        assertThat(deepDiveNodeCodes).hasSize(5);
        assertThat(deepDiveDatasetCodes).hasSize(5);
        verify(datasetDao).listByJobIdAndNodeCodesAndDatasetCodes(
                JOB_ID, screeningNodeCodes, screeningDatasetCodes);
        verify(datasetDao).listByJobIdAndNodeCodesAndDatasetCodes(
                JOB_ID, deepDiveNodeCodes, deepDiveDatasetCodes);
    }

    @Test
    void shouldRejectTamperedEvidenceWhileReadingEvidenceScope() {
        MarketResearchDataset corrupted = intactDataset("{\"items\":[]}");
        corrupted.setNodeCode(ResearchPhase.PREPARE_US_EVIDENCE.getNodeCode());
        corrupted.setDatasetCode("evidence.products");
        corrupted.setSha256("0".repeat(64));
        List<String> evidenceNodeCodes = ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(definition -> definition.phase().getNodeCode())
                .toList();
        List<String> evidenceDatasetCodes = ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        when(datasetDao.listByJobIdAndNodeCodesAndDatasetCodes(
                        JOB_ID, evidenceNodeCodes, evidenceDatasetCodes))
                .thenReturn(List.of(corrupted));

        assertThatThrownBy(() -> datasetService.readEvidenceDatasets(JOB_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHA-256不匹配");
    }

    private MarketResearchDataset intactDataset(String payload) {
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetId(DATASET_ID);
        dataset.setSourcePayload(payload);
        dataset.setSchemaVersion(ResearchConstants.DATASET_SCHEMA_VERSION);
        dataset.setValidationStatus(ResearchDatasetValidationStatus.VALID.name());
        dataset.setSha256(ResearchHashUtils.sha256(payload));
        return dataset;
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setDataSourceMode("MOCK");
        return job;
    }

    private ResearchInput input() {
        return input(List.of("B0TEST0001"));
    }

    private ResearchInput input(List<String> seedAsins) {
        return ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace("US")
                .nodeIdPath("172282:281407")
                .month("2026-07")
                .keyword("facial cleansing device")
                .seedAsins(seedAsins)
                .collectionConfig(new CollectionGraphConfig())
                .build();
    }
}
