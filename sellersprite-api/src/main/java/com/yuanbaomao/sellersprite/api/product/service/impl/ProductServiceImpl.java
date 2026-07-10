// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.product.service.impl;

import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import com.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import com.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
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
    public List<ProductNodeVo> listProductNodes(ProductNodeRequest request) {
        return client.get(SellerSpriteOperation.PRODUCT_NODE,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<List<ProductNodeVo>>>() {
                });
    }

}
