package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ResearchRawDataInspectionServiceTest {

    private static final String JOB_ID = "job-1";

    @Mock
    private ResearchDatasetService datasetService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResearchRawDataInspectionService inspectionService;
    private MarketResearchDataset products;
    private MarketResearchDataset reviews;
    private JsonNode productsPayload;
    private JsonNode reviewsPayload;

    @BeforeEach
    void setUp() throws Exception {
        inspectionService = new ResearchRawDataInspectionService(datasetService);
        products = dataset(
                "products",
                "PRODUCT_RESEARCH",
                ResearchPhase.COLLECT_PRODUCTS.getNodeCode());
        reviews = dataset(
                "reviews.B000TEST",
                "REVIEW_LIST",
                ResearchPhase.COLLECT_REVIEWS.getNodeCode());
        productsPayload = objectMapper.readTree("""
                {
                  "items": [
                    {"asin": "B000ONE", "title": "One", "customSignal": 7},
                    {"asin": "B000TWO", "title": "Two", "customSignal": 9}
                  ],
                  "requestId": "request-1"
                }
                """);
        reviewsPayload = objectMapper.readTree("""
                {"items": [{"asin": "B000TEST", "content": "works well"}]}
                """);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(products, reviews));
        lenient().when(datasetService.readPayload(products)).thenReturn(productsPayload);
    }

    @Test
    void shouldDescribeNormalizedFieldsForCurrentStageOnly() {
        String catalog = inspectionService.describeCatalog(JOB_ID, ResearchStageCode.SCREENING);

        assertThat(catalog)
                .contains("datasetCode（1 个）：products")
                .contains("items[].asin")
                .contains("items[].customSignal")
                .contains("[证据已引用]")
                .contains("[尚未引用]")
                .doesNotContain("reviews.B000TEST");
    }

    @Test
    void shouldReturnOnlyRequestedFieldsWithActualArrayIndexes() {
        String result = inspectionService.queryFields(
                JOB_ID,
                ResearchStageCode.SCREENING,
                "products",
                List.of("items[].asin", "items[].customSignal"),
                1);

        assertThat(result)
                .contains("items[0].asin = B000ONE")
                .contains("items[0].customSignal = 7")
                .doesNotContain("items[1]", "title =");
    }

    @Test
    void shouldRejectDatasetOutsideCurrentStage() {
        assertThatThrownBy(() -> inspectionService.queryFields(
                JOB_ID,
                ResearchStageCode.SCREENING,
                "reviews.B000TEST",
                List.of("items[].content"),
                10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无权读取数据集");
    }

    @Test
    void shouldAllowBothStageDatasetsForFinalAnalysis() {
        when(datasetService.readPayload(reviews)).thenReturn(reviewsPayload);

        String catalog = inspectionService.describeCatalog(JOB_ID, ResearchStageCode.FINAL_ANALYSIS);

        assertThat(catalog)
                .contains("products")
                .contains("reviews.B000TEST")
                .contains("items[].content");
    }

    private MarketResearchDataset dataset(String datasetCode, String operation, String nodeCode) {
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetCode(datasetCode);
        dataset.setOperation(operation);
        dataset.setNodeCode(nodeCode);
        return dataset;
    }
}
