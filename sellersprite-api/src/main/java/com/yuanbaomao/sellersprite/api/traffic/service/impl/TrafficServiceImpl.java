// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.service.impl;

import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.RelatedTrafficRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordStatRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficListingStatRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficSourceRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.RelatedTrafficVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceVo;
import com.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 流量分析接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class TrafficServiceImpl implements TrafficService {

    private final SellerSpriteClient client;

    @Override
    public TrafficKeywordVo reverseKeywords(TrafficKeywordRequest request) {
        return client.post(SellerSpriteOperation.TRAFFIC_KEYWORD, request,
                new ParameterizedTypeReference<SellerSpriteResponse<TrafficKeywordVo>>() {
                });
    }

    @Override
    public RelatedTrafficVo listRelatedTraffic(RelatedTrafficRequest request) {
        return client.post(SellerSpriteOperation.TRAFFIC_LISTING_PAGE, request,
                new ParameterizedTypeReference<SellerSpriteResponse<RelatedTrafficVo>>() {
                });
    }

    @Override
    public TrafficKeywordStatVo getKeywordStats(TrafficKeywordStatRequest request) {
        return client.get(SellerSpriteOperation.TRAFFIC_KEYWORD_STAT,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<TrafficKeywordStatVo>>() {
                });
    }

    @Override
    public TrafficListingStatVo getListingStats(TrafficListingStatRequest request) {
        return client.get(SellerSpriteOperation.TRAFFIC_LISTING_STAT,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<TrafficListingStatVo>>() {
                });
    }

    @Override
    public TrafficSourceVo getTrafficSources(TrafficSourceRequest request) {
        return client.post(SellerSpriteOperation.TRAFFIC_SOURCE, request,
                new ParameterizedTypeReference<SellerSpriteResponse<TrafficSourceVo>>() {
                });
    }

}
