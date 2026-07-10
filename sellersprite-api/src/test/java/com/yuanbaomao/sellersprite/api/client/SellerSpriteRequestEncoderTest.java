package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkListRequest;

import lombok.Data;

class SellerSpriteRequestEncoderTest {

    @Test
    void shouldEncodeQueryAndExcludePathVariablesAndFiles() {
        TestRequest request = new TestRequest();
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setAsin("B0TEST");
        request.setPage(2);
        request.setTypes(List.of("price", "bsr"));
        request.setImage(new MockMultipartFile("image", "image.png", "image/png", new byte[] {1, 2}));

        MultiValueMap<String, String> query = SellerSpriteRequestEncoder.toQuery(
                request, Set.of("marketplace", "asin"));

        assertThat(query.getFirst("page")).isEqualTo("2");
        assertThat(query.get("types")).containsExactly("price", "bsr");
        assertThat(query.getFirst("marketplace")).isNull();
        assertThat(query.getFirst("asin")).isNull();
        assertThat(query.getFirst("image")).isNull();
        assertThat(query.getFirst("keyword")).isNull();
        assertThat(SellerSpriteRequestEncoder.pathValue(request.getMarketplace())).isEqualTo("US");
    }

    @Test
    void shouldEncodeMultipartValuesAndFileResource() {
        TestRequest request = new TestRequest();
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setTypes(List.of("word", "logo"));
        request.setImage(new MockMultipartFile("image", "image.png", "image/png", new byte[] {1, 2}));

        MultiValueMap<String, Object> parts = SellerSpriteRequestEncoder.toMultipart(request);

        assertThat(parts.getFirst("marketplace")).isEqualTo("US");
        assertThat(parts.get("types")).hasSize(2);
        assertThat(parts.getFirst("image")).isNotNull();
        assertThat(parts.getFirst("keyword")).isNull();
    }

    @Test
    void shouldUseJsonPropertyNamesForMultipartFields() {
        TrademarkListRequest request = new TrademarkListRequest();
        request.setOrderField("applicationDate");
        request.setOrderDesc(false);

        MultiValueMap<String, Object> parts = SellerSpriteRequestEncoder.toMultipart(request);

        assertThat(parts).containsKeys("order.field", "order.desc")
                .doesNotContainKeys("orderField", "orderDesc");
        assertThat(parts.getFirst("order.field")).isEqualTo("applicationDate");
        assertThat(parts.getFirst("order.desc")).isEqualTo("false");
    }

    @Data
    static class TestRequest {
        private SellerSpriteMarketplace marketplace;
        private String asin;
        private Integer page;
        private List<String> types;
        private String keyword;
        private MockMultipartFile image;
    }
}
