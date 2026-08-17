package cyou.yuanbaomao.sellersprite.api.market.model.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class MarketDemandTrendVoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeActualDecimalRatioFields() throws Exception {
        SellerSpriteResponse<MarketDemandTrendVo> response = objectMapper.readValue("""
                {
                  "code": "OK",
                  "message": "success",
                  "data": {
                    "asinCount": "22187",
                    "returnRatio": "1.38",
                    "searchToPurchaseRatio": 3.17875,
                    "avgReturnRatio": 2.72,
                    "avgSearchToPurchaseRatio": 2.6,
                    "items": [{"date": "2022-09-10", "glanceViews": 2}]
                  }
                }
                """, new TypeReference<>() {
        });

        MarketDemandTrendVo data = response.getData();
        assertThat(data.getSearchToPurchaseRatio()).isEqualByComparingTo(new BigDecimal("3.17875"));
        assertThat(data.getAvgReturnRatio()).isEqualByComparingTo(new BigDecimal("2.72"));
        assertThat(data.getAvgSearchToPurchaseRatio()).isEqualByComparingTo(new BigDecimal("2.6"));
        assertThat(data.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getDate()).isEqualTo("2022-09-10");
            assertThat(item.getGlanceViews()).isEqualTo(2);
        });
        assertThat(data.getAdditionalProperties()).isEmpty();
    }
}
