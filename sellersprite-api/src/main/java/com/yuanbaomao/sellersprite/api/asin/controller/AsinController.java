// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.controller;

import com.yuanbaomao.base.result.Result;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite ASIN 分析", description = "SellerSprite ASIN 分析分类接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/asins")
public class AsinController {

    private final AsinService asinService;

    @Operation(summary = "ASIN 详情", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}")
    @GetMapping("/detail")
    public Result<AsinDetailVo> getAsinDetail(@Valid @ModelAttribute AsinDetailRequest request) {
        return Result.success(asinService.getAsinDetail(request));
    }

    @Operation(summary = "ASIN优惠趋势", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}/coupon-trend")
    @GetMapping("/coupon-trend")
    public Result<List<AsinCouponTrendVo>> getCouponTrend(@Valid @ModelAttribute AsinCouponTrendRequest request) {
        return Result.success(asinService.getCouponTrend(request));
    }

    @Operation(summary = "ASIN详情及优惠趋势", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}/with-coupon-trend")
    @GetMapping("/with-coupon-trend")
    public Result<AsinWithCouponTrendVo> getAsinWithCouponTrend(@Valid @ModelAttribute AsinWithCouponTrendRequest request) {
        return Result.success(asinService.getAsinWithCouponTrend(request));
    }

    @Operation(summary = "ASIN 销量趋势", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}/sales-trend")
    @GetMapping("/sales-trend")
    public Result<AsinSalesTrendVo> getSalesTrend(@Valid @ModelAttribute AsinSalesTrendRequest request) {
        return Result.success(asinService.getSalesTrend(request));
    }

    @Operation(summary = "ASIN 销量预测", description = "通过统一 SellerSpriteClient 调用 /v1/sales/prediction/asin")
    @GetMapping("/sales-prediction")
    public Result<AsinSalesPredictionVo> predictAsinSales(@Valid @ModelAttribute AsinSalesPredictionRequest request) {
        return Result.success(asinService.predictAsinSales(request));
    }

    @Operation(summary = "BSR销量预测", description = "通过统一 SellerSpriteClient 调用 /v1/sales/prediction/bsr")
    @GetMapping("/bsr-sales-prediction")
    public Result<BsrSalesPredictionVo> predictBsrSales(@Valid @ModelAttribute BsrSalesPredictionRequest request) {
        return Result.success(asinService.predictBsrSales(request));
    }

    @Operation(summary = "商品趋势详情(keepa)", description = "通过统一 SellerSpriteClient 调用 /v1/keepa/{marketplace}/{asin}")
    @GetMapping("/keepa")
    public Result<KeepaTrendVo> getKeepaTrend(@Valid @ModelAttribute KeepaTrendRequest request) {
        return Result.success(asinService.getKeepaTrend(request));
    }

}
