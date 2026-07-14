package com.yuanbaomao.sellersprite.research.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;

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
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0MOCK0001"))
                .build();
    }

    @Test
    void shouldReturnCompleteStableFixtureWithoutSharingMutablePayloads() {
        List<ResearchDataset> quotaDatasets = provider.checkQuota(input);
        List<ResearchDataset> firstMarketDatasets = provider.collectMarketAndProducts(input);
        List<ResearchDataset> secondMarketDatasets = provider.collectMarketAndProducts(input);
        List<ResearchDataset> keywordDatasets = provider.collectKeywords(input);
        List<ResearchDataset> reviewDatasets = provider.collectReviews(input);

        assertThat(quotaDatasets).singleElement()
                .satisfies(dataset -> {
                    assertThat(dataset.getDatasetCode()).isEqualTo("quota.visits");
                    assertThat(dataset.getOperation()).isEqualTo("ACCOUNT_VISITS");
                    assertThat(dataset.getRecordCount()).isEqualTo(1);
                    assertThat(dataset.getPayload().at("/details/modules/productResearch/remaining").asInt())
                            .isEqualTo(9999);
                });
        assertThat(firstMarketDatasets)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("market.overview", "products");
        assertThat(firstMarketDatasets)
                .extracting(ResearchDataset::getRecordCount)
                .containsExactly(1, 1);
        assertThat(firstMarketDatasets.get(1).getPayload().at("/items/0/asin").asText())
                .isEqualTo("B0DB5VT4QJ");
        assertThat(firstMarketDatasets.get(1).getPayload().at("/items/0/units").asInt())
                .isEqualTo(158113);
        assertThat(firstMarketDatasets.get(1).getPayload().at("/items/0/badge/bestSeller").asText())
                .isEqualTo("#1 Best Seller in Bath Rugs");
        assertThat(firstMarketDatasets.get(1).getPayload().at("/items/0/subcategories/0/code").asText())
                .isEqualTo("1063242");
        assertThat(firstMarketDatasets.get(1).getPayload().at("/guestVisited").asBoolean())
                .isFalse();
        assertThat(keywordDatasets).singleElement()
                .satisfies(dataset -> {
                    assertThat(dataset.getDatasetCode()).isEqualTo("keywords");
                    assertThat(dataset.getRecordCount()).isEqualTo(4);
                });
        assertThat(reviewDatasets).singleElement()
                .satisfies(dataset -> {
                    assertThat(dataset.getDatasetCode()).isEqualTo("reviews.fixture");
                    assertThat(dataset.getRecordCount()).isEqualTo(4);
                });
        assertThat(firstMarketDatasets).usingRecursiveComparison().isEqualTo(secondMarketDatasets);
        assertThat(firstMarketDatasets.get(0).getPayload())
                .isNotSameAs(secondMarketDatasets.get(0).getPayload());
    }

    @Test
    void shouldRejectIncompleteResearchInput() {
        input.setKeyword(" ");

        assertThatThrownBy(() -> provider.collectKeywords(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keyword");
    }
}
