// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.trademark.service.impl;

import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import com.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkDetailRequest;
import com.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkListRequest;
import com.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkStatsRequest;
import com.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo;
import com.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListVo;
import com.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkRangeVo;
import com.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo;
import com.yuanbaomao.sellersprite.api.trademark.service.TrademarkService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 全球商标接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class TrademarkServiceImpl implements TrademarkService {

    private final SellerSpriteClient client;

    @Override
    public List<TrademarkRangeVo> getBrandRange() {
        return client.get(SellerSpriteOperation.GLOBAL_BRAND_RANGE,
                Map.of(), SellerSpriteRequestEncoder.toQuery(null, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<List<TrademarkRangeVo>>>() {
                });
    }

    @Override
    public TrademarkDetailVo getBrandDetail(TrademarkDetailRequest request) {
        return client.get(SellerSpriteOperation.GLOBAL_BRAND_DETAIL,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<TrademarkDetailVo>>() {
                });
    }

    @Override
    public TrademarkListVo listBrands(TrademarkListRequest request) {
        return client.postMultipart(SellerSpriteOperation.GLOBAL_BRAND_LIST, SellerSpriteRequestEncoder.toMultipart(request),
                new ParameterizedTypeReference<SellerSpriteResponse<TrademarkListVo>>() {
                });
    }

    @Override
    public TrademarkStatsVo getBrandStats(TrademarkStatsRequest request) {
        return client.postMultipart(SellerSpriteOperation.GLOBAL_BRAND_STATS, SellerSpriteRequestEncoder.toMultipart(request),
                new ParameterizedTypeReference<SellerSpriteResponse<TrademarkStatsVo>>() {
                });
    }

}
