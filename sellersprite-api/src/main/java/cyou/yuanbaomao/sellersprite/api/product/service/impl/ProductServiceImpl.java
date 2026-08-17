// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.product.service.impl;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 产品分析接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final SellerSpriteClient client;

    @Override
    public CompetitorLookupVo lookupCompetitors(CompetitorLookupRequest request) {
        return client.post(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP, request,
                new ParameterizedTypeReference<SellerSpriteResponse<CompetitorLookupVo>>() {
                });
    }

    @Override
    public ProductResearchVo researchProducts(ProductResearchRequest request) {
        return client.post(SellerSpriteOperation.PRODUCT_RESEARCH, request,
                new ParameterizedTypeReference<SellerSpriteResponse<ProductResearchVo>>() {
                });
    }

    @Override
    public List<ProductNodeVo> listProductNodes(SellerSpriteMarketplace marketplace, String nodeIdPath,
            String keyword, String month) {
        ProductNodeRequest request = new ProductNodeRequest();
        request.setMarketplace(marketplace);
        request.setNodeIdPath(nodeIdPath);
        request.setKeyword(keyword);
        request.setMonth(month);
        return client.get(SellerSpriteOperation.PRODUCT_NODE,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<List<ProductNodeVo>>>() {
                });
    }

}
