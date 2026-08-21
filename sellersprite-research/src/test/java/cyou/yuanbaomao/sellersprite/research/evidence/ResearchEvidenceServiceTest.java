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
    void shouldUseTotalUnitsRatioForGoodsConcentrationEvidence() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        MarketResearchDataset products = new MarketResearchDataset();
        products.setDatasetCode("products");
        ObjectNode productsPayload = objectMapper.createObjectNode();
        productsPayload.putArray("items");

        MarketResearchDataset goodsConcentration = new MarketResearchDataset();
        goodsConcentration.setDatasetCode("market.goods-concentration");
        ObjectNode concentrationPayload = objectMapper.createObjectNode();
        ArrayNode concentrationItems = concentrationPayload.putArray("items");
        concentrationItems.addObject()
                .put("asin", "B0TEST0001")
                .put("totalUnits", 2515)
                .put("totalRevenue", 18837.35)
                .put("totalUnitsRatio", 0.4478)
                .put("totalRevenueRatio", 0.3052);
        concentrationItems.addObject()
                .put("asin", "B0TEST0002")
                .put("totalUnits", 1000)
                .put("totalRevenue", 8000)
                .put("totalUnitsRatio", 0.2000)
                .put("totalRevenueRatio", 0.1500);

        when(datasetService.listByJobId(JOB_ID))
                .thenReturn(List.of(products, goodsConcentration));
        when(datasetService.readPayload(products)).thenReturn(productsPayload);
        when(datasetService.readPayload(goodsConcentration)).thenReturn(concentrationPayload);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);

        ResearchDataset result = service.prepare(
                job(),
                cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_CONCENTRATION_EVIDENCE);

        assertThat(result.getPayload().at("/items/0/集中维度").asText())
                .isEqualTo("商品集中度");
        assertThat(result.getPayload().at("/items/0/占比").decimalValue())
                .isEqualByComparingTo("0.4478");
        assertThat(result.getPayload().at("/items/0/销售额占比").decimalValue())
                .isEqualByComparingTo("0.3052");
        assertThat(result.getPayload().at("/items/1/累计销量占比").decimalValue())
                .isEqualByComparingTo("0.6478");
        assertThat(result.getPayload().at("/items/1/累计销售额占比").decimalValue())
                .isEqualByComparingTo("0.4552");
    }

    @Test
    void shouldBuildDecisionMetricsForReturnBrandAndVocEvidence() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);

        MarketResearchDataset products = dataset("products");
        ObjectNode productsPayload = objectMapper.createObjectNode();
        productsPayload.putArray("items").addObject()
                .put("asin", "B0TEST0001")
                .put("brand", "Example")
                .put("title", "Example product")
                .put("variations", 4)
                .put("fulfillment", "FBA")
                .put("sellerNation", "CN");

        MarketResearchDataset markets = dataset("market.research");
        ObjectNode marketPayload = objectMapper.createObjectNode();
        marketPayload.putArray("items").addObject()
                .put("nodeLabelName", "Example Segment")
                .put("nodeLabelPath", "Home:Example")
                .put("returnRatio", 0.12)
                .put("avgReturnRatio", 0.08);

        MarketResearchDataset demand = dataset("market.demand-trend");
        ObjectNode demandPayload = objectMapper.createObjectNode();
        demandPayload.putArray("items");

        MarketResearchDataset brands = dataset("market.brand-concentration");
        ObjectNode brandPayload = objectMapper.createObjectNode();
        brandPayload.putArray("items").addObject()
                .put("ranking", 1)
                .put("brand", "Example")
                .putArray("asins").add("B0TEST0001").add("B0TEST0002");
        ObjectNode brandItem = (ObjectNode) brandPayload.path("items").get(0);
        brandItem.put("products", 2)
                .put("newProducts", 1)
                .put("totalUnits", 5000)
                .put("totalRevenue", 75000)
                .put("avgPrice", 15)
                .put("rating", 4.5)
                .put("ratings", 800)
                .put("totalUnitsRatio", 0.40)
                .put("totalRevenueRatio", 0.35)
                .put("newUnitsRatio", 0.25)
                .put("newRevenueRatio", 0.20);

        MarketResearchDataset reviews = dataset("reviews.B0TEST0001");
        ObjectNode reviewsPayload = objectMapper.createObjectNode();
        ArrayNode reviewItems = reviewsPayload.putArray("items");
        reviewItems
                .addObject().put("star", 5).put("verified", true).put("likes", 4)
                .putArray("images").add("image.jpg");
        reviewItems.addObject()
                .put("star", 3).put("verified", false).put("likes", 2);
        reviewItems.addObject()
                .put("star", 1).put("verified", true).put("likes", 0)
                .put("title", "Bad").put("content", "Broken");

        List<MarketResearchDataset> datasets = List.of(products, markets, demand, brands, reviews);
        Map<MarketResearchDataset, JsonNode> payloads = Map.of(
                products, productsPayload,
                markets, marketPayload,
                demand, demandPayload,
                brands, brandPayload,
                reviews, reviewsPayload);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(datasets);
        when(datasetService.readPayload(any(MarketResearchDataset.class)))
                .thenAnswer(invocation -> payloads.get(invocation.getArgument(0)));

        ResearchDataset returnEvidence = service.prepare(
                job(), cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_SEGMENT_RETURN_EVIDENCE);
        ResearchDataset brandEvidence = service.prepare(
                job(), cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_BRAND_EVIDENCE);
        ResearchDataset vocEvidence = service.prepare(
                job(), cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_VOC_EVIDENCE);

        assertThat(returnEvidence.getPayload().at("/items/0/退货率差值").decimalValue())
                .isEqualByComparingTo("0.0400");
        assertThat(returnEvidence.getPayload().at("/items/0/相对类目均值").decimalValue())
                .isEqualByComparingTo("0.5000");
        assertThat(returnEvidence.getPayload().at("/items/0/风险等级").asText()).isEqualTo("高");
        assertThat(brandEvidence.getPayload().at("/items/0/ASIN数").asInt()).isEqualTo(2);
        assertThat(brandEvidence.getPayload().at("/items/0/销量份额").decimalValue())
                .isEqualByComparingTo("0.40");
        assertThat(brandEvidence.getPayload().at("/items/0/FBA商品占比").decimalValue())
                .isEqualByComparingTo("1.0000");
        assertThat(vocEvidence.getPayload().at("/items/0/正向占比").decimalValue())
                .isEqualByComparingTo("0.3333");
        assertThat(vocEvidence.getPayload().at("/items/0/中性占比").decimalValue())
                .isEqualByComparingTo("0.3333");
        assertThat(vocEvidence.getPayload().at("/items/0/负向占比").decimalValue())
                .isEqualByComparingTo("0.3333");
        assertThat(vocEvidence.getPayload().at("/items/0/VP评论占比").decimalValue())
                .isEqualByComparingTo("0.6667");
        assertThat(vocEvidence.getPayload().at("/items/0/带图或视频评论占比").decimalValue())
                .isEqualByComparingTo("0.3333");
        assertThat(vocEvidence.getPayload().at("/items/0/平均赞同数").decimalValue())
                .isEqualByComparingTo("2.0000");
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
        assertThat(returnEvidence.path("columns"))
                .extracting(JsonNode::asText)
                .doesNotContain("退货原因", "退货量");
        JsonNode vocEvidence = find(evidence, "evidence.voc").getPayload();
        assertThat(vocEvidence.path("columns"))
                .extracting(JsonNode::asText)
                .doesNotContain("用户画像", "使用场景", "购买动机");

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
        ArrayNode salesTrendPoints = salesPayload.putArray("salesTrendPoints");
        salesTrendPoints
                .addObject().put("month", "2026-07").put("parentUnitSales", 1200)
                .put("childUnitSales", 300).put("averagePrice", 19.99);
        salesTrendPoints.addObject()
                .put("month", "2026-05").put("parentUnitSales", 1000).put("childUnitSales", 200);
        salesTrendPoints.addObject()
                .put("month", "2026-06").put("parentUnitSales", 1100).put("childUnitSales", 250);

        MarketResearchDataset keepa = new MarketResearchDataset();
        keepa.setDatasetCode("asin-keepa-trend.B0TEST0001");
        ObjectNode keepaPayload = objectMapper.createObjectNode();
        keepaPayload.put("asin", "B0TEST0001");
        keepaPayload.put("dataAsin", "B0TEST0001");
        keepaPayload.put("parentAsin", "B0PARENT01");
        keepaPayload.put("nodeLabelPath", "Home & Kitchen:Example");
        ArrayNode pricePoints = keepaPayload.putArray("price");
        pricePoints.addObject()
                .put("timePoint", 1_754_522_400_000L)
                .put("value", 18.99);
        pricePoints.addObject()
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
        assertThat(salesEvidence.getPayload().at("/items/0/月份").asText()).isEqualTo("2026-05");
        assertThat(salesEvidence.getPayload().at("/items/2/父体销量环比").decimalValue())
                .isEqualByComparingTo("0.0909");
        assertThat(salesEvidence.getPayload().at("/items/2/子体销量贡献率").decimalValue())
                .isEqualByComparingTo("0.2500");
        assertThat(salesEvidence.getPayload().at("/items/2/近3月子体月均销量").decimalValue())
                .isEqualByComparingTo("250.0000");
        assertThat(salesEvidence.getPayload().at("/items/2/近3月子体销量波动率").decimalValue())
                .isEqualByComparingTo("0.1633");
        assertThat(keepaEvidence.getPayload().path("items")).hasSize(3);
        assertThat(keepaEvidence.getPayload().at("/items/1/前值").decimalValue())
                .isEqualByComparingTo("18.99");
        assertThat(keepaEvidence.getPayload().at("/items/1/变化量").decimalValue())
                .isEqualByComparingTo("1.0000");
        assertThat(keepaEvidence.getPayload().at("/items/1/变化率").decimalValue())
                .isEqualByComparingTo("0.0527");
        assertThat(keepaEvidence.getPayload().at("/items/1/区间最小值").decimalValue())
                .isEqualByComparingTo("18.9900");
        assertThat(keepaEvidence.getPayload().at("/items/1/区间最大值").decimalValue())
                .isEqualByComparingTo("19.9900");
        assertThat(keepaEvidence.getPayload().path("items"))
                .extracting(item -> item.path("指标").asText())
                .containsExactly("价格", "价格", "大类BSR");
    }

    @Test
    void shouldSampleHighFrequencyKeepaTrendPointsWhenExceedingLimit() {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        MarketResearchDataset keepa = dataset("asin-keepa-trend.B0HEAVY001");
        ObjectNode keepaPayload = objectMapper.createObjectNode();
        keepaPayload.put("asin", "B0HEAVY001");
        keepaPayload.put("brand", "HeavyBrand");
        keepaPayload.put("title", "HeavyTitle");

        var pricePoints = keepaPayload.putArray("price");
        for (int i = 0; i < 200; i++) {
            var point = pricePoints.addObject();
            point.put("timePoint", 1700000000000L + i * 86400000L);
            point.put("value", 10 + i);
        }

        List<MarketResearchDataset> datasets = List.of(keepa);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(datasets);
        when(datasetService.readPayload(keepa)).thenReturn(keepaPayload);
        ResearchEvidenceService service = new ResearchEvidenceService(datasetService, objectMapper);

        ResearchDataset keepaEvidence = service.prepare(
                job(), cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase
                        .PREPARE_ASIN_OPERATION_TREND_EVIDENCE);

        assertThat(keepaEvidence.getPayload().path("items")).hasSize(50);
        assertThat(keepaEvidence.getPayload().at("/items/0/数值").decimalValue())
                .isEqualByComparingTo("10");
        assertThat(keepaEvidence.getPayload().at("/items/49/数值").decimalValue())
                .isEqualByComparingTo("209");
        assertThat(keepaEvidence.getPayload().at("/items/0/区间最小值").decimalValue())
                .isEqualByComparingTo("10");
        assertThat(keepaEvidence.getPayload().at("/items/0/区间最大值").decimalValue())
                .isEqualByComparingTo("209");
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

    private MarketResearchDataset dataset(String code) {
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetCode(code);
        return dataset;
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
