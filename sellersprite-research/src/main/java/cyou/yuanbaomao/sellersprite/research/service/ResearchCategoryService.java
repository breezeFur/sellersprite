package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** 为市场调研入口提供跨任务复用的产品类目查询。 */
@Service
@RequiredArgsConstructor
public class ResearchCategoryService {

    private static final TypeReference<List<ProductNodeVo>> PRODUCT_NODE_LIST_TYPE =
            new TypeReference<>() {
            };

    private final ProductService productService;
    private final ResearchSourceCacheService sourceCacheService;
    private final ObjectMapper objectMapper;

    public List<ProductNodeVo> listProductNodes(SellerSpriteMarketplace marketplace, String nodeIdPath,
            String keyword, String month) {
        ProductNodeRequest request = new ProductNodeRequest();
        request.setMarketplace(marketplace);
        request.setNodeIdPath(nodeIdPath);
        request.setKeyword(keyword);
        request.setMonth(month);
        CachedPayload cached = sourceCacheService.getOrLoad(
                SellerSpriteOperation.PRODUCT_NODE,
                request,
                sourceCacheService.productNodePolicy(),
                () -> objectMapper.valueToTree(productService.listProductNodes(
                        marketplace, nodeIdPath, keyword, month)));
        List<ProductNodeVo> nodes = objectMapper.convertValue(cached.payload(), PRODUCT_NODE_LIST_TYPE);
        return nodes == null ? List.of() : List.copyOf(nodes);
    }
}
