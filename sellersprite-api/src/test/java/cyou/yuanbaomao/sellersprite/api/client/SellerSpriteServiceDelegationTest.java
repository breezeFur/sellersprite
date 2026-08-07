package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;

import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.impl.AsinServiceImpl;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import cyou.yuanbaomao.sellersprite.api.product.service.impl.ProductServiceImpl;
import cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import cyou.yuanbaomao.sellersprite.api.tool.model.vo.OcrVo;
import cyou.yuanbaomao.sellersprite.api.tool.service.impl.ToolServiceImpl;

@SuppressWarnings({"rawtypes", "unchecked"})
class SellerSpriteServiceDelegationTest {

    @Test
    void shouldDelegateJsonPostToUnifiedClient() {
        SellerSpriteClient client = mock(SellerSpriteClient.class);
        CompetitorLookupRequest request = new CompetitorLookupRequest();
        CompetitorLookupVo expected = new CompetitorLookupVo();
        doReturn(expected).when(client).post(
                eq(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP), same(request), any());

        CompetitorLookupVo result = new ProductServiceImpl(client).lookupCompetitors(request);

        assertThat(result).isSameAs(expected);
        verify(client).post(eq(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP), same(request), any());
    }

    @Test
    void shouldDelegateGetWithEncodedPathVariables() {
        SellerSpriteClient client = mock(SellerSpriteClient.class);
        AsinDetailRequest request = new AsinDetailRequest();
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setAsin("B0TESTASIN");
        AsinDetailVo expected = new AsinDetailVo();
        doReturn(expected).when(client).get(
                eq(SellerSpriteOperation.ASIN_DETAIL), any(Map.class), any(MultiValueMap.class), any());

        AsinDetailVo result = new AsinServiceImpl(client).getAsinDetail(request);

        assertThat(result).isSameAs(expected);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> pathCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).get(eq(SellerSpriteOperation.ASIN_DETAIL), pathCaptor.capture(),
                any(MultiValueMap.class), any());
        assertThat(pathCaptor.getValue()).containsExactlyInAnyOrderEntriesOf(
                Map.of("marketplace", "US", "asin", "B0TESTASIN"));
    }

    @Test
    void shouldDelegateMultipartWithFileResource() {
        SellerSpriteClient client = mock(SellerSpriteClient.class);
        OcrRequest request = new OcrRequest();
        request.setType(2);
        request.setFn("CHINESE");
        request.setImage(new MockMultipartFile("image", "sample.png", "image/png", new byte[] {1, 2}));
        OcrVo expected = new OcrVo();
        doReturn(expected).when(client).postMultipart(
                eq(SellerSpriteOperation.OCR), any(MultiValueMap.class), any());

        OcrVo result = new ToolServiceImpl(client).recognizeImageText(request);

        assertThat(result).isSameAs(expected);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MultiValueMap<String, Object>> partsCaptor = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(client).postMultipart(eq(SellerSpriteOperation.OCR), partsCaptor.capture(), any());
        MultiValueMap<String, Object> parts = partsCaptor.getValue();
        assertThat(parts.getFirst("type")).isEqualTo("2");
        assertThat(parts.getFirst("fn")).isEqualTo("CHINESE");
        assertThat(parts.getFirst("image")).isInstanceOfSatisfying(Resource.class,
                resource -> assertThat(resource.getFilename()).isEqualTo("sample.png"));
    }
}
