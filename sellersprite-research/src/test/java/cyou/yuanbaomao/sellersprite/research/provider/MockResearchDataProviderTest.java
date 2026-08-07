package cyou.yuanbaomao.sellersprite.research.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

class MockResearchDataProviderTest {

    private MockResearchDataProvider provider;
    private ResearchInput input;

    @BeforeEach
    void setUp() {
        provider = new MockResearchDataProvider(
                new ObjectMapper(),
                new ClassPathResource("research/mock/v1/market-research.json"));
        input = ResearchInput.builder()
                .jobId("job-mock-001")
                .marketplace("US")
                .nodeIdPath("172282:281407")
                .month("2026-07")
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0DB5VT4QJ"))
                .collectionConfig(new CollectionGraphConfig())
                .build();
    }

    @Test
    void shouldExposeSixDeterministicCollectionContracts() {
        List<ResearchDataset> firstProducts = provider.collectProducts(input);
        List<ResearchDataset> secondProducts = provider.collectProducts(input);

        assertThat(provider.checkQuota(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("quota.visits");
        assertThat(firstProducts)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly(
                        "products",
                        "asins.B0DB5VT4QJ.detail",
                        "asins.B0DB5VT4QJ.sales-trend");
        assertThat(provider.collectMarketSalesTrend(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly(
                        "market.sales-trend",
                        "market.statistics.history.2025-08",
                        "market.statistics.history.2025-09",
                        "market.statistics.history.2025-10",
                        "market.statistics.history.2025-11",
                        "market.statistics.history.2025-12",
                        "market.statistics.history.2026-01",
                        "market.statistics.history.2026-02",
                        "market.statistics.history.2026-03",
                        "market.statistics.history.2026-04",
                        "market.statistics.history.2026-05",
                        "market.statistics.history.2026-06");
        assertThat(provider.collectKeywordDemandTrend(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("market.demand-trend", "keywords.trend");
        assertThat(provider.collectSegmentOpportunity(input))
                .extracting(ResearchDataset::getDatasetCode)
                .contains(
                        "market.research",
                        "market.statistics",
                        "market.price",
                        "market.brand-concentration");
        assertThat(provider.collectReviews(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("reviews.fixture");
        assertThat(provider.collectKeywordIntelligence(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("keywords", "keywords.miner", "traffic-keywords.B0DB5VT4QJ");

        assertThat(firstProducts).usingRecursiveComparison().isEqualTo(secondProducts);
        assertThat(firstProducts.getFirst().getPayload())
                .isNotSameAs(secondProducts.getFirst().getPayload());
    }

    @Test
    void shouldRejectIncompleteResearchInput() {
        input.setNodeIdPath(" ");

        assertThatThrownBy(() -> provider.collectKeywordIntelligence(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nodeIdPath");
    }

    @Test
    void shouldApplyCollectionConfigToMockDatasets() {
        CollectionGraphConfig config = input.getCollectionConfig();
        config.getCollectProducts().setEnrichmentAsinLimit(0);
        config.getCollectMarketSalesTrend().setMonthCount(12);
        config.getCollectReviews().getPagination().setTargetCountPerAsin(1);
        config.getCollectKeywordIntelligence().setTrafficAsinLimit(0);

        List<ResearchDataset> products = provider.collectProducts(input);
        List<ResearchDataset> trends = provider.collectMarketSalesTrend(input);

        assertThat(products)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("products");
        assertThat(trends)
                .filteredOn(dataset -> "market.sales-trend".equals(dataset.getDatasetCode()))
                .singleElement()
                .satisfies(dataset -> assertThat(dataset.getRecordCount()).isEqualTo(12));
        assertThat(trends)
                .filteredOn(dataset -> dataset.getDatasetCode().startsWith("market.statistics.history."))
                .hasSize(11);
        assertThat(provider.collectReviews(input))
                .allSatisfy(dataset -> assertThat(dataset.getRecordCount()).isLessThanOrEqualTo(1));
        assertThat(provider.collectKeywordIntelligence(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("keywords", "keywords.miner");
    }

    @Test
    void shouldRestrictDeepDiveMockDatasetsToSelectedAsins() {
        input.setSeedAsins(List.of("B0DB5VT4QJ"));

        ResearchDataset reviews = dataset(provider.collectReviews(input), "reviews.fixture");
        List<String> reviewAsins = new ArrayList<>();
        reviews.getPayload().path("items")
                .forEach(item -> reviewAsins.add(item.path("asin").asText()));

        assertThat(reviewAsins).containsOnly("B0DB5VT4QJ");
        assertThat(reviews.getRecordCount()).isEqualTo(2);
        assertThat(provider.collectKeywordIntelligence(input))
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("keywords", "keywords.miner", "traffic-keywords.B0DB5VT4QJ");
    }

    private ResearchDataset dataset(List<ResearchDataset> datasets, String datasetCode) {
        return datasets.stream()
                .filter(dataset -> datasetCode.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow();
    }
}
