package cyou.yuanbaomao.sellersprite.api.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.SellerSpritePageVo;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class SellerSpriteCommonModelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeOfficialProductPageShape() throws Exception {
        String json = """
                {
                  "pages": 2,
                  "page": 1,
                  "size": 1,
                  "total": 2,
                  "took": 754,
                  "order": {"field": "total_units", "desc": true},
                  "items": [{
                    "asin": "B0DGVP84B5",
                    "brand": "Apple",
                    "price": 728.99,
                    "revenue": 123276872.0,
                    "availableDate": 1726012800000,
                    "badge": {
                      "bestSeller": "#1 Best Seller",
                      "amazonChoice": "N",
                      "ebc": "Y",
                      "video": "Y",
                      "futureBadge": "badge-value"
                    },
                    "subcategories": [{
                      "code": "14130292011",
                      "rank": 1,
                      "label": "Smartwatches",
                      "futureSubcategory": true
                    }],
                    "futureProductMetric": {"score": 42}
                  }],
                  "guestVisited": false
                }
                """;

        SellerSpritePageVo<ProductSummaryVo> page = objectMapper.readValue(json, new TypeReference<>() {
        });

        assertThat(page.getPage()).isEqualTo(1);
        assertThat(page.getOrder().getField()).isEqualTo("total_units");
        assertThat(page.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getAsin()).isEqualTo("B0DGVP84B5");
            assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal("728.99"));
            assertThat(item.getBadge().getEbc()).isEqualTo("Y");
            assertThat(item.getBadge().getAdditionalProperties().get("futureBadge").asText())
                    .isEqualTo("badge-value");
            assertThat(item.getSubcategories()).singleElement()
                    .satisfies(subcategory -> {
                        assertThat(subcategory.getCode()).isEqualTo("14130292011");
                        assertThat(subcategory.getAdditionalProperties()
                                        .get("futureSubcategory")
                                        .asBoolean())
                                .isTrue();
                    });
            assertThat(item.getAdditionalProperties().get("futureProductMetric").path("score").asInt())
                    .isEqualTo(42);
        });
    }

    @Test
    void shouldDeclareAllOfficialMarketplaces() {
        assertThat(SellerSpriteMarketplace.values())
                .extracting(SellerSpriteMarketplace::getCode)
                .containsExactly("US", "JP", "UK", "DE", "FR", "IT", "ES", "CA", "IN");
    }
}
