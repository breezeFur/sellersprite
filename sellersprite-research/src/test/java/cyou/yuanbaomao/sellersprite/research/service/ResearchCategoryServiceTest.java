package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ResearchCategoryServiceTest {

    private final ProductService productService = mock(ProductService.class);
    private final ResearchSourceCacheService sourceCacheService = mock(ResearchSourceCacheService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResearchCategoryService service =
            new ResearchCategoryService(productService, sourceCacheService, objectMapper);

    @Test
    void shouldLoadProductNodesThroughPersistentCache() {
        ProductNodeRequest request = new ProductNodeRequest();
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setMonth("202607");
        request.setKeyword("facial cleansing");
        ProductNodeVo node = new ProductNodeVo();
        node.setNodeIdPath("3760911:11062741");
        node.setNodeLabelPath("Beauty & Personal Care:Tools & Accessories");
        when(productService.listProductNodes(request)).thenReturn(List.of(node));
        CachePolicy policy = CachePolicy.ttl(604_800_000L);
        when(sourceCacheService.productNodePolicy()).thenReturn(policy);
        when(sourceCacheService.getOrLoad(
                        eq(SellerSpriteOperation.PRODUCT_NODE),
                        eq(request),
                        eq(policy),
                        any()))
                .thenAnswer(invocation -> {
                    Supplier<JsonNode> loader = invocation.getArgument(3);
                    JsonNode payload = loader.get();
                    return new CachedPayload(payload, payload.size());
                });

        List<ProductNodeVo> result = service.listProductNodes(request);

        assertThat(result).singleElement().satisfies(value -> {
            assertThat(value.getNodeIdPath()).isEqualTo("3760911:11062741");
            assertThat(value.getNodeLabelPath())
                    .isEqualTo("Beauty & Personal Care:Tools & Accessories");
        });
        verify(productService).listProductNodes(request);
    }
}
