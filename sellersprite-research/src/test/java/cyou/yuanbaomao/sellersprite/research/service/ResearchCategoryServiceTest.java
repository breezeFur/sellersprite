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
        when(productService.listProductNodes(SellerSpriteMarketplace.US, null,
                "facial cleansing", "202607")).thenReturn(List.of(node));
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

        List<ProductNodeVo> result = service.listProductNodes(SellerSpriteMarketplace.US, null,
                "facial cleansing", "202607");

        assertThat(result).singleElement().satisfies(value -> {
            assertThat(value.getNodeIdPath()).isEqualTo("3760911:11062741");
            assertThat(value.getNodeLabelPath())
                    .isEqualTo("Beauty & Personal Care:Tools & Accessories");
        });
        verify(productService).listProductNodes(SellerSpriteMarketplace.US, null,
                "facial cleansing", "202607");
    }

    @Test
    void shouldResolveAndAggregateCategoriesByAsins() {
        cyou.yuanbaomao.sellersprite.research.model.dto.CategoryResolveByAsinsRequest request =
                new cyou.yuanbaomao.sellersprite.research.model.dto.CategoryResolveByAsinsRequest();
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setMonth("2026-07");
        request.setAsins(List.of("B08GHW4TBS", "B08GHW4TBC", "B07OTHERAS"));

        cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo lookupVo =
                new cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo();
        cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo item1 =
                new cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo();
        item1.setAsin("B08GHW4TBS");
        item1.setNodeId(1063280L);
        item1.setNodeIdPath("1055398:1063252:1063280");
        item1.setNodeLabelPath("Home & Kitchen:Bedding:Blankets & Throws");

        cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo item2 =
                new cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo();
        item2.setAsin("B08GHW4TBC");
        item2.setNodeId(1063280L);
        item2.setNodeIdPath("1055398:1063252:1063280");
        item2.setNodeLabelPath("Home & Kitchen:Bedding:Blankets & Throws");

        cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo item3 =
                new cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo();
        item3.setAsin("B07OTHERAS");
        item3.setNodeId(99999L);
        item3.setNodeIdPath("1055398:1063252:99999");
        item3.setNodeLabelPath("Home & Kitchen:Bedding:Pillows");

        lookupVo.setItems(List.of(item1, item2, item3));

        when(productService.lookupCompetitors(any())).thenReturn(lookupVo);
        CachePolicy policy = CachePolicy.ttl(604_800_000L);
        when(sourceCacheService.asinPolicy()).thenReturn(policy);
        when(sourceCacheService.getOrLoad(
                        eq(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP),
                        any(),
                        eq(policy),
                        any()))
                .thenAnswer(invocation -> {
                    Supplier<JsonNode> loader = invocation.getArgument(3);
                    JsonNode payload = loader.get();
                    return new CachedPayload(payload, payload.size());
                });

        List<cyou.yuanbaomao.sellersprite.research.model.vo.ResearchCategoryCandidateVo> candidates =
                service.resolveCategoriesByAsins(request);

        assertThat(candidates).hasSize(2);
        cyou.yuanbaomao.sellersprite.research.model.vo.ResearchCategoryCandidateVo first = candidates.get(0);
        assertThat(first.getNodeIdPath()).isEqualTo("1055398:1063252:1063280");
        assertThat(first.getNodeId()).isEqualTo("1063280");
        assertThat(first.getNodeLabel()).isEqualTo("Blankets & Throws");
        assertThat(first.getDisplayName()).isEqualTo("Blankets & Throws");
        assertThat(first.getMatchedCount()).isEqualTo(2);
        assertThat(first.getMatchedAsins()).containsExactly("B08GHW4TBS", "B08GHW4TBC");
        assertThat(first.getMatchedRatio()).isEqualTo(66.7);

        cyou.yuanbaomao.sellersprite.research.model.vo.ResearchCategoryCandidateVo second = candidates.get(1);
        assertThat(second.getNodeIdPath()).isEqualTo("1055398:1063252:99999");
        assertThat(second.getNodeId()).isEqualTo("99999");
        assertThat(second.getNodeLabel()).isEqualTo("Pillows");
        assertThat(second.getMatchedCount()).isEqualTo(1);
        assertThat(second.getMatchedRatio()).isEqualTo(33.3);
    }
}
