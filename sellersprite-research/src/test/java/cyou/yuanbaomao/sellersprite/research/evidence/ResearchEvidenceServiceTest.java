package cyou.yuanbaomao.sellersprite.research.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class ResearchEvidenceServiceTest {

    private static final String JOB_ID = "job-evidence-001";

    @Test
    void shouldKeepEmptyEvidenceItemsWithoutPlaceholderRow() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        MarketResearchDataset products = new MarketResearchDataset();
        products.setDatasetCode("products");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items");
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(products));
        when(datasetService.readPayload(products)).thenReturn(payload);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);

        ResearchDataset result = service.prepare(job(), ResearchEvidenceCatalog.DEFINITIONS.getFirst().phase());

        assertThat(result.getPayload().path("columns").size())
                .isEqualTo(ResearchEvidenceCatalog.DEFINITIONS.getFirst().columns().size());
        assertThat(result.getPayload().path("items").isEmpty()).isTrue();
    }

    @Test
    void shouldExposeEmptyMarketDemandResponseInsteadOfReturningBlankSheet() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        MarketResearchDataset marketDemand = new MarketResearchDataset();
        marketDemand.setDatasetCode("market.demand-trend");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items");
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(marketDemand));
        when(datasetService.readPayload(marketDemand)).thenReturn(payload);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);

        ResearchDataset result = service.prepare(
                job(),
                cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_DEMAND_TREND_EVIDENCE);

        assertThat(result.getPayload().path("items")).hasSize(1);
        assertThat(result.getPayload().at("/items/0/来源类型").asText())
                .isEqualTo("SellerSprite类目需求（接口未返回数据）");
        assertThat(result.getPayload().at("/items/0/月份").asText()).isEqualTo("2026-07");
        assertThat(result.getPayload().at("/items/0/浏览量~1搜索量").isNull()).isTrue();
    }

    @Test
    void shouldKeepMarketDemandSummaryWhenMonthlyItemsAreEmpty() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        MarketResearchDataset marketDemand = new MarketResearchDataset();
        marketDemand.setDatasetCode("market.demand-trend");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items");
        payload.put("asinCount", 123);
        payload.put("returnRatio", 0.08);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(marketDemand));
        when(datasetService.readPayload(marketDemand)).thenReturn(payload);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);

        ResearchDataset result = service.prepare(
                job(),
                cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_DEMAND_TREND_EVIDENCE);

        assertThat(result.getPayload().at("/items/0/来源类型").asText())
                .isEqualTo("SellerSprite类目需求（仅类目汇总）");
        assertThat(result.getPayload().at("/items/0/商品数").asInt()).isEqualTo(123);
        assertThat(result.getPayload().at("/items/0/SellerSprite退货率").asDouble())
                .isEqualTo(0.08);
    }

    @Test
    void shouldBuildAndValidateTwelveBusinessEvidenceDatasetsWithoutAgentOutput() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);
        ResearchInput input = input();
        MockResearchDataProvider provider = new MockResearchDataProvider(
                objectMapper,
                new ClassPathResource("research/mock/v1/market-research.json"));
        List<ResearchDataset> sources = new ArrayList<>();
        sources.addAll(provider.checkQuota(input));
        List<ResearchDataset> products = provider.collectProducts(input);
        ArrayNode productItems = (ArrayNode) find(products, "products").getPayload().path("items");
        ObjectNode secondProduct = (ObjectNode) productItems.get(0).deepCopy();
        secondProduct.put("asin", "B0MOCK0002");
        productItems.add(secondProduct);
        sources.addAll(products);
        sources.addAll(provider.collectMarketSalesTrend(input));
        sources.addAll(provider.collectKeywordDemandTrend(input));
        sources.addAll(provider.collectSegmentOpportunity(input));
        sources.addAll(provider.collectReviews(input));
        sources.addAll(provider.collectKeywordIntelligence(input));
        sources.addAll(provider.collectAsinIntelligence(input));

        List<MarketResearchDataset> stored = new ArrayList<>();
        Map<MarketResearchDataset, JsonNode> payloads = new IdentityHashMap<>();
        append(stored, payloads, sources);
        when(datasetService.listByJobId(JOB_ID)).thenAnswer(ignored -> stored);
        when(datasetService.readPayload(any(MarketResearchDataset.class)))
                .thenAnswer(invocation -> payloads.get(invocation.getArgument(0)));

        MarketResearchJob job = job();
        List<ResearchDataset> evidence = ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(definition -> service.prepare(job, definition.phase()))
                .toList();

        assertThat(evidence)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactlyElementsOf(ResearchEvidenceCatalog.DEFINITIONS.stream()
                        .map(ResearchEvidenceCatalog.Definition::datasetCode)
                        .toList());
        for (int index = 0; index < evidence.size(); index++) {
            ResearchEvidenceCatalog.Definition definition = ResearchEvidenceCatalog.DEFINITIONS.get(index);
            JsonNode payload = evidence.get(index).getPayload();
            assertThat(payload.path("sheetName").asText()).isEqualTo(definition.sheetName());
            assertThat(payload.properties().stream().map(Map.Entry::getKey).toList())
                    .containsExactly("sheetName", "columns", "items");
            assertThat(payload.path("items").isArray()).isTrue();
            List<String> columns = arrayTexts(payload.path("columns"));
            assertThat(columns).containsExactlyElementsOf(definition.columns());
            assertThat(columns).noneMatch(column -> column.equals("数据状态")
                    || column.equals("证据范围")
                    || column.equals("来源数据集")
                    || column.equals("数据局限")
                    || column.startsWith("原始."));
            for (JsonNode item : payload.path("items")) {
                assertThat(columns).allMatch(item::has);
                assertThat(item.properties().stream().map(Map.Entry::getKey).toList())
                        .containsExactlyElementsOf(columns);
            }
        }

        JsonNode salesEvidence = find(evidence, "evidence.market-sales-trend").getPayload();
        assertThat(salesEvidence.path("items").size()).isLessThanOrEqualTo(12);

        JsonNode returnEvidence = find(evidence, "evidence.segment-return").getPayload();
        assertThat(returnEvidence.at("/items/0/退货原因").isNull()).isTrue();
        assertThat(returnEvidence.at("/items/0/退货量").isNull()).isTrue();
        JsonNode vocEvidence = find(evidence, "evidence.voc").getPayload();
        for (JsonNode item : vocEvidence.path("items")) {
            assertThat(item.path("用户画像").isNull()).isTrue();
            assertThat(item.path("使用场景").isNull()).isTrue();
            assertThat(item.path("购买动机").isNull()).isTrue();
        }

        append(stored, payloads, evidence);
        assertThatCode(() -> service.validate(JOB_ID)).doesNotThrowAnyException();
    }

    @Test
    void shouldFlattenSelectedAsinSalesAndKeepaTrends() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);
        MarketResearchDataset sales = new MarketResearchDataset();
        sales.setDatasetCode("asin-sales-trend.B0TEST0001");
        ObjectNode salesPayload = objectMapper.createObjectNode();
        salesPayload.putObject("asin")
                .put("asin", "B0TEST0001")
                .put("brand", "Example")
                .put("title", "Example product");
        salesPayload.putArray("salesTrendPoints").addObject()
                .put("month", "2026-07")
                .put("parentUnitSales", 1200)
                .put("childUnitSales", 300)
                .put("averagePrice", 19.99);

        MarketResearchDataset keepa = new MarketResearchDataset();
        keepa.setDatasetCode("asin-keepa-trend.B0TEST0001");
        ObjectNode keepaPayload = objectMapper.createObjectNode();
        keepaPayload.put("asin", "B0TEST0001");
        keepaPayload.put("dataAsin", "B0TEST0001");
        keepaPayload.put("parentAsin", "B0PARENT01");
        keepaPayload.put("nodeLabelPath", "Home & Kitchen:Example");
        keepaPayload.putArray("price").addObject()
                .put("timePoint", 1_754_608_800_000L)
                .put("value", 19.99);
        keepaPayload.putArray("bsr").addObject()
                .put("timePoint", 1_754_608_800_000L)
                .put("value", 2567);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(sales, keepa));
        when(datasetService.readPayload(sales)).thenReturn(salesPayload);
        when(datasetService.readPayload(keepa)).thenReturn(keepaPayload);

        ResearchDataset salesEvidence = service.prepare(
                job(), cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_ASIN_SALES_TREND_EVIDENCE);
        ResearchDataset keepaEvidence = service.prepare(
                job(), cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_ASIN_OPERATION_TREND_EVIDENCE);

        assertThat(salesEvidence.getPayload().at("/items/0/ASIN").asText())
                .isEqualTo("B0TEST0001");
        assertThat(salesEvidence.getPayload().at("/items/0/父体销量").asInt()).isEqualTo(1200);
        assertThat(keepaEvidence.getPayload().path("items")).hasSize(2);
        assertThat(keepaEvidence.getPayload().path("items"))
                .extracting(item -> item.path("指标").asText())
                .containsExactly("价格", "大类BSR");
    }

    private void append(
            List<MarketResearchDataset> stored,
            Map<MarketResearchDataset, JsonNode> payloads,
            List<ResearchDataset> values) {
        for (ResearchDataset value : values) {
            MarketResearchDataset dataset = new MarketResearchDataset();
            dataset.setDatasetId("dataset-" + stored.size());
            dataset.setJobId(JOB_ID);
            dataset.setDatasetCode(value.getDatasetCode());
            dataset.setOperation(value.getOperation());
            stored.add(dataset);
            payloads.put(dataset, value.getPayload());
        }
    }

    private ResearchDataset find(List<ResearchDataset> datasets, String code) {
        return datasets.stream()
                .filter(dataset -> code.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow();
    }

    private List<String> arrayTexts(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add(value.asText()));
        return values;
    }

    private ResearchInput input() {
        return ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace("US")
                .nodeIdPath("172282:281407")
                .month("2026-07")
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0MOCK0001"))
                .collectionConfig(new CollectionGraphConfig())
                .build();
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setResearchMonth("2026-07");
        return job;
    }
}
