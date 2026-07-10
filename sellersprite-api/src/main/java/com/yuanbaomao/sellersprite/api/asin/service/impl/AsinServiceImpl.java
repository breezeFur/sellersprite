// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.service.impl;

import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinCouponTrendRequest;
import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesPredictionRequest;
import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesTrendRequest;
import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinWithCouponTrendRequest;
import com.yuanbaomao.sellersprite.api.asin.model.dto.BsrSalesPredictionRequest;
import com.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import com.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo;
import com.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import com.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo;
import com.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo;
import com.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo;
import com.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo;
import com.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo;
import com.yuanbaomao.sellersprite.api.asin.service.AsinService;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite ASIN 分析接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class AsinServiceImpl implements AsinService {

    private final SellerSpriteClient client;

    @Override
    public AsinDetailVo getAsinDetail(AsinDetailRequest request) {
        return client.get(SellerSpriteOperation.ASIN_DETAIL,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinDetailVo>>() {
                });
    }

    @Override
    public List<AsinCouponTrendVo> getCouponTrend(AsinCouponTrendRequest request) {
        return client.get(SellerSpriteOperation.ASIN_COUPON_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<List<AsinCouponTrendVo>>>() {
                });
    }

    @Override
    public AsinWithCouponTrendVo getAsinWithCouponTrend(AsinWithCouponTrendRequest request) {
        return client.get(SellerSpriteOperation.ASIN_WITH_COUPON_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinWithCouponTrendVo>>() {
                });
    }

    @Override
    public AsinSalesTrendVo getSalesTrend(AsinSalesTrendRequest request) {
        return client.get(SellerSpriteOperation.ASIN_SALES_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinSalesTrendVo>>() {
                });
    }

    @Override
    public AsinSalesPredictionVo predictAsinSales(AsinSalesPredictionRequest request) {
        return client.get(SellerSpriteOperation.ASIN_SALES_PREDICTION,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinSalesPredictionVo>>() {
                });
    }

    @Override
    public BsrSalesPredictionVo predictBsrSales(BsrSalesPredictionRequest request) {
        return client.get(SellerSpriteOperation.BSR_SALES_PREDICTION,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<BsrSalesPredictionVo>>() {
                });
    }

    @Override
    public KeepaTrendVo getKeepaTrend(KeepaTrendRequest request) {
        return client.get(SellerSpriteOperation.ASIN_KEEPA_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<KeepaTrendVo>>() {
                });
    }

}
