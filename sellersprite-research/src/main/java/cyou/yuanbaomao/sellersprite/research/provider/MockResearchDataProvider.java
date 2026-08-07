package cyou.yuanbaomao.sellersprite.research.provider;

import java.io.IOException;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;

import lombok.Data;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 从版本化 classpath fixture 读取数据的离线 Provider。
 */
public class MockResearchDataProvider implements ResearchDataProvider {

    private final ObjectMapper objectMapper;
    private final MockFixture fixture;

    public MockResearchDataProvider(ObjectMapper objectMapper, Resource fixtureResource) {
        Assert.notNull(objectMapper, "objectMapper 不能为空");
        Assert.notNull(fixtureResource, "fixtureResource 不能为空");
        this.objectMapper = objectMapper;
        this.fixture = readFixture(objectMapper, fixtureResource);
    }

    @Override
    public ResearchSourceMode sourceMode() {
        return ResearchSourceMode.MOCK;
    }

    @Override
    public List<ResearchDataset> checkQuota(ResearchInput input) {
        validateInput(input);
        return copyDatasets(fixture.getQuota());
    }

    @Override
    public List<ResearchDataset> collectProducts(ResearchInput input) {
        validateInput(input);
        int targetCount = input.getCollectionConfig()
                .getCollectProducts().getPagination().getTargetCount();
        List<String> enrichmentAsins = limitedSeedAsins(
                input,
                input.getCollectionConfig().getCollectProducts().getEnrichmentAsinLimit());
        return copyDatasets(fixture.getMarketAndProducts(), dataset -> {
                    String code = dataset.getDatasetCode();
                    return ResearchConstants.PRODUCTS_DATASET_CODE.equals(code)
                            || enrichmentAsins.stream().anyMatch(asin -> code.startsWith("asins." + asin + "."));
                }).stream()
                .map(dataset -> ResearchConstants.PRODUCTS_DATASET_CODE.equals(dataset.getDatasetCode())
                        ? trimItems(dataset, targetCount)
                        : dataset)
                .toList();
    }

    @Override
    public List<ResearchDataset> collectMarketSalesTrend(ResearchInput input) {
        validateInput(input);
        List<ResearchDataset> source = copyDatasets(
                fixture.getMarketAndProducts(),
                dataset -> ResearchConstants.MARKET_SALES_TREND_DATASET_CODE.equals(
                        dataset.getDatasetCode()));
        ResearchDataset fixtureTrend = source.getFirst();
        int monthCount = input.getCollectionConfig().getCollectMarketSalesTrend().getMonthCount();
        YearMonth researchMonth = YearMonth.parse(input.getMonth());
        Map<String, JsonNode> pointsByMonth = fixtureTrend.getPayload().isArray()
                ? trendPointsByMonth(fixtureTrend.getPayload())
                : Map.of();
        JsonNode template = pointsByMonth.values().stream().findFirst()
                .orElseGet(objectMapper::createObjectNode);
        JsonNode statistics = fixture.getMarketAndProducts().stream()
                .filter(dataset -> ResearchConstants.MARKET_STATISTICS_DATASET_CODE.equals(
                        dataset.getDatasetCode()))
                .map(ResearchDataset::getPayload)
                .findFirst()
                .orElseGet(objectMapper::createObjectNode);
        ArrayNode trend = objectMapper.createArrayNode();
        List<ResearchDataset> datasets = new ArrayList<>(monthCount);
        for (int offset = monthCount - 1; offset >= 0; offset--) {
            String month = researchMonth.minusMonths(offset).toString();
            JsonNode point = pointsByMonth.getOrDefault(month, template).deepCopy();
            if (point instanceof ObjectNode objectPoint) {
                objectPoint.put("month", month);
            }
            trend.add(point);
            if (offset > 0) {
                datasets.add(new ResearchDataset(
                        ResearchConstants.HISTORICAL_MARKET_STATISTICS_DATASET_CODE_PREFIX + month,
                        "MARKET_STATISTICS",
                        statistics.deepCopy(),
                        1));
            }
        }
        datasets.addFirst(new ResearchDataset(
                ResearchConstants.MARKET_SALES_TREND_DATASET_CODE,
                fixtureTrend.getOperation(),
                trend,
                monthCount));
        return List.copyOf(datasets);
    }

    @Override
    public List<ResearchDataset> collectKeywordDemandTrend(ResearchInput input) {
        validateInput(input);
        return Stream.concat(
                        copyDatasets(fixture.getMarketAndProducts(), dataset ->
                                ResearchConstants.MARKET_DEMAND_TREND_DATASET_CODE.equals(
                                        dataset.getDatasetCode())).stream(),
                        copyDatasets(fixture.getKeywords(), dataset ->
                                ResearchConstants.KEYWORD_TREND_DATASET_CODE.equals(
                                        dataset.getDatasetCode())).stream())
                .toList();
    }

    @Override
    public List<ResearchDataset> collectSegmentOpportunity(ResearchInput input) {
        validateInput(input);
        int targetCount = input.getCollectionConfig()
                .getCollectSegmentOpportunity().getPagination().getTargetCount();
        return copyDatasets(fixture.getMarketAndProducts(), dataset -> {
            String code = dataset.getDatasetCode();
            return code.startsWith("market.")
                    && !ResearchConstants.MARKET_SALES_TREND_DATASET_CODE.equals(code)
                    && !ResearchConstants.MARKET_DEMAND_TREND_DATASET_CODE.equals(code);
        }).stream()
                .map(dataset -> ResearchConstants.MARKET_RESEARCH_DATASET_CODE.equals(dataset.getDatasetCode())
                        ? trimItems(dataset, targetCount)
                        : dataset)
                .toList();
    }

    @Override
    public List<ResearchDataset> collectReviews(ResearchInput input) {
        validateInput(input);
        List<String> selectedAsins = normalizedSeedAsins(input);
        int targetCount = input.getCollectionConfig()
                .getCollectReviews().getPagination().getTargetCountPerAsin();
        return copyDatasets(fixture.getReviews()).stream()
                .map(dataset -> filterAndTrimItemsByAsin(dataset, selectedAsins, targetCount))
                .toList();
    }

    @Override
    public List<ResearchDataset> collectAsinIntelligence(ResearchInput input) {
        validateInput(input);
        List<ResearchDataset> datasets = new ArrayList<>();
        for (String asin : normalizedSeedAsins(input)) {
            JsonNode salesTrend = fixture.getMarketAndProducts().stream()
                    .filter(dataset -> ("asins." + asin + ".sales-trend")
                            .equals(dataset.getDatasetCode()))
                    .map(ResearchDataset::getPayload)
                    .findFirst()
                    .map(JsonNode::deepCopy)
                    .orElseGet(objectMapper::createObjectNode);
            datasets.add(new ResearchDataset(
                    ResearchConstants.ASIN_SALES_TREND_DATASET_CODE_PREFIX + asin,
                    "ASIN_SALES_TREND",
                    salesTrend,
                    1));

            ObjectNode keepaTrend = objectMapper.createObjectNode();
            keepaTrend.put("asin", asin);
            keepaTrend.set("price", objectMapper.createArrayNode());
            keepaTrend.set("dealPrice", objectMapper.createArrayNode());
            keepaTrend.set("buyBox", objectMapper.createArrayNode());
            keepaTrend.set("priceList", objectMapper.createArrayNode());
            keepaTrend.set("bsr", objectMapper.createArrayNode());
            keepaTrend.set("reviews", objectMapper.createArrayNode());
            keepaTrend.set("rating", objectMapper.createArrayNode());
            keepaTrend.set("sellers", objectMapper.createArrayNode());
            datasets.add(new ResearchDataset(
                    ResearchConstants.ASIN_KEEPA_TREND_DATASET_CODE_PREFIX + asin,
                    "ASIN_KEEPA_TREND",
                    keepaTrend,
                    0));
        }
        return List.copyOf(datasets);
    }

    @Override
    public List<ResearchDataset> collectKeywordIntelligence(ResearchInput input) {
        validateInput(input);
        List<String> trafficAsins = limitedSeedAsins(
                input,
                input.getCollectionConfig().getCollectKeywordIntelligence().getTrafficAsinLimit());
        return copyDatasets(fixture.getKeywords(), dataset -> {
            String code = dataset.getDatasetCode();
            return !ResearchConstants.KEYWORD_TREND_DATASET_CODE.equals(code)
                    && (!code.startsWith("traffic-keywords.")
                    || trafficAsins.stream().anyMatch(asin -> code.equals("traffic-keywords." + asin)));
        });
    }

    private Map<String, JsonNode> trendPointsByMonth(JsonNode trend) {
        Map<String, JsonNode> points = new LinkedHashMap<>();
        for (JsonNode point : trend) {
            String month = point.path("month").asText();
            if (StringUtils.hasText(month)) {
                points.put(month, point);
            }
        }
        return points;
    }

    private ResearchDataset trimItems(ResearchDataset dataset, int targetCount) {
        JsonNode copiedPayload = dataset.getPayload().deepCopy();
        JsonNode itemsNode = copiedPayload.path("items");
        if (!(copiedPayload instanceof ObjectNode payload) || !(itemsNode instanceof ArrayNode items)) {
            return new ResearchDataset(
                    dataset.getDatasetCode(),
                    dataset.getOperation(),
                    copiedPayload,
                    dataset.getRecordCount());
        }
        while (items.size() > targetCount) {
            items.remove(items.size() - 1);
        }
        payload.put("size", items.size());
        payload.put("total", items.size());
        return new ResearchDataset(
                dataset.getDatasetCode(),
                dataset.getOperation(),
                payload,
                items.size());
    }

    private ResearchDataset filterAndTrimItemsByAsin(
            ResearchDataset dataset, List<String> selectedAsins, int targetCountPerAsin) {
        JsonNode copiedPayload = dataset.getPayload().deepCopy();
        JsonNode itemsNode = copiedPayload.path("items");
        if (!(copiedPayload instanceof ObjectNode payload) || !(itemsNode instanceof ArrayNode items)) {
            return new ResearchDataset(
                    dataset.getDatasetCode(),
                    dataset.getOperation(),
                    copiedPayload,
                    dataset.getRecordCount());
        }
        Map<String, Integer> countsByAsin = new LinkedHashMap<>();
        ArrayNode selectedItems = objectMapper.createArrayNode();
        for (JsonNode item : items) {
            String asin = item.path("asin").asText();
            int currentCount = countsByAsin.getOrDefault(asin, 0);
            if (selectedAsins.contains(asin) && currentCount < targetCountPerAsin) {
                selectedItems.add(item.deepCopy());
                countsByAsin.put(asin, currentCount + 1);
            }
        }
        payload.set("items", selectedItems);
        payload.put("size", selectedItems.size());
        payload.put("total", selectedItems.size());
        return new ResearchDataset(
                dataset.getDatasetCode(),
                dataset.getOperation(),
                payload,
                selectedItems.size());
    }

    private List<String> normalizedSeedAsins(ResearchInput input) {
        if (input.getSeedAsins() == null) {
            return List.of();
        }
        return input.getSeedAsins().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<String> limitedSeedAsins(ResearchInput input, Integer limit) {
        if (limit == null || limit <= 0) {
            return List.of();
        }
        return normalizedSeedAsins(input).stream()
                .limit(limit)
                .toList();
    }

    private MockFixture readFixture(ObjectMapper objectMapper, Resource fixtureResource) {
        if (!fixtureResource.exists()) {
            throw new IllegalStateException("市场调研 Mock fixture 不存在: " + fixtureResource.getDescription());
        }
        try (InputStream inputStream = fixtureResource.getInputStream()) {
            MockFixture loadedFixture = objectMapper.readValue(inputStream, MockFixture.class);
            validateFixture(loadedFixture, fixtureResource);
            return loadedFixture;
        } catch (IOException exception) {
            throw new IllegalStateException("读取市场调研 Mock fixture 失败: "
                    + fixtureResource.getDescription(), exception);
        }
    }

    private void validateFixture(MockFixture loadedFixture, Resource fixtureResource) {
        if (loadedFixture == null
                || !StringUtils.hasText(loadedFixture.getVersion())
                || loadedFixture.getQuota() == null
                || loadedFixture.getQuota().isEmpty()
                || loadedFixture.getMarketAndProducts() == null
                || loadedFixture.getMarketAndProducts().isEmpty()
                || loadedFixture.getKeywords() == null
                || loadedFixture.getKeywords().isEmpty()
                || loadedFixture.getReviews() == null
                || loadedFixture.getReviews().isEmpty()) {
            throw new IllegalStateException("市场调研 Mock fixture 结构不完整: "
                    + fixtureResource.getDescription());
        }
        loadedFixture.getQuota().forEach(this::validateDataset);
        loadedFixture.getMarketAndProducts().forEach(this::validateDataset);
        loadedFixture.getKeywords().forEach(this::validateDataset);
        loadedFixture.getReviews().forEach(this::validateDataset);
    }

    private void validateDataset(ResearchDataset dataset) {
        if (dataset == null
                || !StringUtils.hasText(dataset.getDatasetCode())
                || !StringUtils.hasText(dataset.getOperation())
                || dataset.getPayload() == null
                || dataset.getRecordCount() == null
                || dataset.getRecordCount() < 0) {
            throw new IllegalStateException("市场调研 Mock fixture 包含无效数据集");
        }
    }

    private List<ResearchDataset> copyDatasets(List<ResearchDataset> source) {
        return copyDatasets(source, ignored -> true);
    }

    private List<ResearchDataset> copyDatasets(
            List<ResearchDataset> source, Predicate<ResearchDataset> filter) {
        return source.stream()
                .filter(filter)
                .map(dataset -> new ResearchDataset(
                        dataset.getDatasetCode(),
                        dataset.getOperation(),
                        dataset.getPayload().deepCopy(),
                        dataset.getRecordCount()))
                .toList();
    }

    private void validateInput(ResearchInput input) {
        if (input == null
                || !StringUtils.hasText(input.getJobId())
                || !StringUtils.hasText(input.getMarketplace())
                || !StringUtils.hasText(input.getNodeIdPath())
                || !StringUtils.hasText(input.getMonth())
                || input.getCollectionConfig() == null) {
            throw new IllegalArgumentException(
                    "市场调研输入的 jobId、marketplace、nodeIdPath、month 和 collectionConfig 不能为空");
        }
    }

    @Data
    private static class MockFixture {
        private String version;
        private List<ResearchDataset> quota;
        private List<ResearchDataset> marketAndProducts;
        private List<ResearchDataset> keywords;
        private List<ResearchDataset> reviews;
    }
}
