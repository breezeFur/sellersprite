package cyou.yuanbaomao.sellersprite.api.market.model.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class MarketSellerConcentrationVoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeActualSellerNameAndAsinsFields() throws IOException {
        MarketSellerConcentrationVo response = objectMapper.readValue("""
                {
                  "sellerName": "PRITECH BEAUTY CARE",
                  "asins": ["B07RB2KRCS"]
                }
                """, MarketSellerConcentrationVo.class);

        assertThat(response.getSellerName()).isEqualTo("PRITECH BEAUTY CARE");
        assertThat(response.getAsins()).isEqualTo(List.of("B07RB2KRCS"));
        assertThat(response.getAdditionalProperties()).isEmpty();
    }
}
