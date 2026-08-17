package cyou.yuanbaomao.sellersprite.api.market.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import org.junit.jupiter.api.Test;

class MarketDemandTrendRequestTest {

    @Test
    void shouldNotSerializeNewProductParameter() throws Exception {
        MarketDemandTrendRequest request = new MarketDemandTrendRequest();
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setMonth("202607");
        request.setTopN(100);
        request.setNodeIdPath("2619525011:17921061011:2399939011");

        String payload = new ObjectMapper().writeValueAsString(request);

        assertThat(payload)
                .contains("\"marketplace\":\"US\"")
                .contains("\"topN\":100")
                .doesNotContain("newProduct");
    }
}
