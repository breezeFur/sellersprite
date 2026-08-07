package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.dict.core.DictTemplate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SellerSpriteDictionaryResolverTest {

    @Test
    void shouldResolveStableLabelsInNestedJsonPayload() {
        DictTemplate dictTemplate = mock(DictTemplate.class);
        when(dictTemplate.getValueByLabel("MARKET_US")).thenReturn("US");
        when(dictTemplate.getValueByLabel("MARKET_PERIOD_N")).thenReturn("N");
        when(dictTemplate.getValueByLabel("RELATED_PRODUCT_ASSOCIATION_TYPE_VAV")).thenReturn("vav");
        when(dictTemplate.getValueByLabel("LISTING_DATE_NULL")).thenReturn(null);
        when(dictTemplate.getValueByLabel("PRODUCT_SIZE_US_ST_SS")).thenReturn("ST/SS");
        when(dictTemplate.getValueByLabel("PRODUCT_SIZE_US_LS")).thenReturn("LS");
        when(dictTemplate.getValueByLabel("PRODUCT_SORT_FIELD_TOTAL_UNITS")).thenReturn("total_units");
        SellerSpriteDictionaryResolver resolver = new SellerSpriteDictionaryResolver(dictTemplate);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("marketplace", "MARKET_US");
        request.put("period", "MARKET_PERIOD_N");
        request.put("relations", List.of("RELATED_PRODUCT_ASSOCIATION_TYPE_VAV"));
        request.put("listingDate", "LISTING_DATE_NULL");
        request.put("dimensionType", "PRODUCT_SIZE_US_ST_SS,PRODUCT_SIZE_US_LS");
        request.put("order", Map.of("field", "PRODUCT_SORT_FIELD_TOTAL_UNITS", "desc", true));
        request.put("keyword", "wireless earbuds");
        Object resolved = resolver.resolveRequest(request);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("marketplace", "US");
        expected.put("period", "N");
        expected.put("relations", List.of("vav"));
        expected.put("listingDate", null);
        expected.put("dimensionType", "ST/SS,LS");
        expected.put("order", Map.of("field", "total_units", "desc", true));
        expected.put("keyword", "wireless earbuds");
        assertThat(resolved).isEqualTo(expected);
    }
}
