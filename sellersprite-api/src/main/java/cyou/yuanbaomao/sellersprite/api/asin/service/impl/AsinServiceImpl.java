// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.service.impl;

import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinCouponTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesPredictionRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinWithCouponTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.BsrSalesPredictionRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
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
    public AsinDetailVo getAsinDetail(SellerSpriteMarketplace marketplace, String asin) {
        AsinDetailRequest request = new AsinDetailRequest();
        request.setMarketplace(marketplace);
        request.setAsin(asin);
        return client.get(SellerSpriteOperation.ASIN_DETAIL,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinDetailVo>>() {
                });
    }

    @Override
    public List<AsinCouponTrendVo> getCouponTrend(SellerSpriteMarketplace marketplace, String asin) {
        AsinCouponTrendRequest request = new AsinCouponTrendRequest();
        request.setMarketplace(marketplace);
        request.setAsin(asin);
        return client.get(SellerSpriteOperation.ASIN_COUPON_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<List<AsinCouponTrendVo>>>() {
                });
    }

    @Override
    public AsinWithCouponTrendVo getAsinWithCouponTrend(SellerSpriteMarketplace marketplace, String asin) {
        AsinWithCouponTrendRequest request = new AsinWithCouponTrendRequest();
        request.setMarketplace(marketplace);
        request.setAsin(asin);
        return client.get(SellerSpriteOperation.ASIN_WITH_COUPON_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinWithCouponTrendVo>>() {
                });
    }

    @Override
    public AsinSalesTrendVo getSalesTrend(SellerSpriteMarketplace marketplace, String asin) {
        AsinSalesTrendRequest request = new AsinSalesTrendRequest();
        request.setMarketplace(marketplace);
        request.setAsin(asin);
        return client.get(SellerSpriteOperation.ASIN_SALES_TREND,
                Map.of("marketplace", SellerSpriteRequestEncoder.pathValue(request.getMarketplace()), "asin", SellerSpriteRequestEncoder.pathValue(request.getAsin())), SellerSpriteRequestEncoder.toQuery(request, Set.of("marketplace", "asin")),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinSalesTrendVo>>() {
                });
    }

    @Override
    public AsinSalesPredictionVo predictAsinSales(SellerSpriteMarketplace marketplace, String asin) {
        AsinSalesPredictionRequest request = new AsinSalesPredictionRequest();
        request.setMarketplace(marketplace);
        request.setAsin(asin);
        return client.get(SellerSpriteOperation.ASIN_SALES_PREDICTION,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<AsinSalesPredictionVo>>() {
                });
    }

    @Override
    public BsrSalesPredictionVo predictBsrSales(SellerSpriteMarketplace marketplace, Integer bsr, String categoryId) {
        BsrSalesPredictionRequest request = new BsrSalesPredictionRequest();
        request.setMarketplace(marketplace);
        request.setBsr(bsr);
        request.setCategoryId(categoryId);
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
