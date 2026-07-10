// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.service.impl;

import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketBrandConcentrationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketDemandTrendRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketEbcDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketGoodsConcentrationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketPriceDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingsDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerConcentrationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerLocationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerTypeRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTimeRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTrendRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketBrandConcentrationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketDemandTrendVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketEbcDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketGoodsConcentrationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketPriceDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingsDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerConcentrationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerLocationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerTypeVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTimeVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTrendVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo;
import com.yuanbaomao.sellersprite.api.market.service.MarketService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 市场分析接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final SellerSpriteClient client;

    @Override
    public MarketResearchVo researchMarkets(MarketResearchRequest request) {
        return client.post(SellerSpriteOperation.MARKET_RESEARCH, request,
                new ParameterizedTypeReference<SellerSpriteResponse<MarketResearchVo>>() {
                });
    }

    @Override
    public MarketStatisticsVo getMarketStatistics(MarketStatisticsRequest request) {
        return client.post(SellerSpriteOperation.MARKET_STATISTICS, request,
                new ParameterizedTypeReference<SellerSpriteResponse<MarketStatisticsVo>>() {
                });
    }

    @Override
    public List<MarketGoodsConcentrationVo> getGoodsConcentration(MarketGoodsConcentrationRequest request) {
        return client.post(SellerSpriteOperation.MARKET_GOODS, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketGoodsConcentrationVo>>>() {
                });
    }

    @Override
    public List<MarketBrandConcentrationVo> getBrandConcentration(MarketBrandConcentrationRequest request) {
        return client.post(SellerSpriteOperation.MARKET_BRAND, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketBrandConcentrationVo>>>() {
                });
    }

    @Override
    public List<MarketSellerLocationVo> getSellerLocationDistribution(MarketSellerLocationRequest request) {
        return client.post(SellerSpriteOperation.MARKET_SELLER_LOCATION, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketSellerLocationVo>>>() {
                });
    }

    @Override
    public List<MarketSellerConcentrationVo> getSellerConcentration(MarketSellerConcentrationRequest request) {
        return client.post(SellerSpriteOperation.MARKET_SELLER, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketSellerConcentrationVo>>>() {
                });
    }

    @Override
    public List<MarketSellerTypeVo> getSellerTypeDistribution(MarketSellerTypeRequest request) {
        return client.post(SellerSpriteOperation.MARKET_SELLER_TYPE, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketSellerTypeVo>>>() {
                });
    }

    @Override
    public MarketDemandTrendVo getDemandTrend(MarketDemandTrendRequest request) {
        return client.post(SellerSpriteOperation.MARKET_PERFORMANCE, request,
                new ParameterizedTypeReference<SellerSpriteResponse<MarketDemandTrendVo>>() {
                });
    }

    @Override
    public List<MarketShelfTimeVo> getShelfTimeDistribution(MarketShelfTimeRequest request) {
        return client.post(SellerSpriteOperation.MARKET_SHELF_TIME, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketShelfTimeVo>>>() {
                });
    }

    @Override
    public List<MarketShelfTrendVo> getShelfTrendDistribution(MarketShelfTrendRequest request) {
        return client.post(SellerSpriteOperation.MARKET_SHELF_TREND, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketShelfTrendVo>>>() {
                });
    }

    @Override
    public List<MarketRatingsDistributionVo> getRatingsDistribution(MarketRatingsDistributionRequest request) {
        return client.post(SellerSpriteOperation.MARKET_RATINGS, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketRatingsDistributionVo>>>() {
                });
    }

    @Override
    public List<MarketRatingDistributionVo> getRatingDistribution(MarketRatingDistributionRequest request) {
        return client.post(SellerSpriteOperation.MARKET_RATING, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketRatingDistributionVo>>>() {
                });
    }

    @Override
    public List<MarketPriceDistributionVo> getPriceDistribution(MarketPriceDistributionRequest request) {
        return client.post(SellerSpriteOperation.MARKET_PRICE, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketPriceDistributionVo>>>() {
                });
    }

    @Override
    public List<MarketEbcDistributionVo> getEbcDistribution(MarketEbcDistributionRequest request) {
        return client.post(SellerSpriteOperation.MARKET_EBC, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<MarketEbcDistributionVo>>>() {
                });
    }

}
