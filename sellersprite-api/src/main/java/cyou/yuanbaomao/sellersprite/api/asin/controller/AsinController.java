// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Tag(name = "SellerSprite ASIN 分析", description = "SellerSprite ASIN 分析分类接口")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/sellersprite/asins")
public class AsinController {

    private final AsinService asinService;

    @Operation(summary = "ASIN 详情", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}")
    @GetMapping("/detail")
    public Result<AsinDetailVo> getAsinDetail(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "ASIN，例如 B08GHW4TBS")
            @NotBlank @RequestParam("asin") String asin) {
        return Result.success(asinService.getAsinDetail(marketplace, asin));
    }

    @Operation(summary = "ASIN优惠趋势", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}/coupon-trend")
    @GetMapping("/coupon-trend")
    public Result<List<AsinCouponTrendVo>> getCouponTrend(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "ASIN，例如 B08GHW4TBS")
            @NotBlank @RequestParam("asin") String asin) {
        return Result.success(asinService.getCouponTrend(marketplace, asin));
    }

    @Operation(summary = "ASIN详情及优惠趋势", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}/with-coupon-trend")
    @GetMapping("/with-coupon-trend")
    public Result<AsinWithCouponTrendVo> getAsinWithCouponTrend(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "ASIN，例如 B08GHW4TBS")
            @NotBlank @RequestParam("asin") String asin) {
        return Result.success(asinService.getAsinWithCouponTrend(marketplace, asin));
    }

    @Operation(summary = "ASIN 销量趋势", description = "通过统一 SellerSpriteClient 调用 /v1/asin/{marketplace}/{asin}/sales-trend")
    @GetMapping("/sales-trend")
    public Result<AsinSalesTrendVo> getSalesTrend(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "ASIN，例如 B08GHW4TBS")
            @NotBlank @RequestParam("asin") String asin) {
        return Result.success(asinService.getSalesTrend(marketplace, asin));
    }

    @Operation(summary = "ASIN 销量预测", description = "通过统一 SellerSpriteClient 调用 /v1/sales/prediction/asin")
    @GetMapping("/sales-prediction")
    public Result<AsinSalesPredictionVo> predictAsinSales(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "ASIN，例如 B08GHW4TBS")
            @NotBlank @RequestParam("asin") String asin) {
        return Result.success(asinService.predictAsinSales(marketplace, asin));
    }

    @Operation(summary = "BSR销量预测", description = "通过统一 SellerSpriteClient 调用 /v1/sales/prediction/bsr")
    @GetMapping("/bsr-sales-prediction")
    public Result<BsrSalesPredictionVo> predictBsrSales(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "大类排名")
            @NotNull @RequestParam("bsr") Integer bsr,
            @Parameter(description = "一级类目节点")
            @NotBlank @RequestParam("categoryId") String categoryId) {
        return Result.success(asinService.predictBsrSales(marketplace, bsr, categoryId));
    }

    @Operation(summary = "商品趋势详情(keepa)", description = "通过统一 SellerSpriteClient 调用 /v1/keepa/{marketplace}/{asin}")
    @GetMapping("/keepa")
    public Result<KeepaTrendVo> getKeepaTrend(@Valid @ModelAttribute KeepaTrendRequest request) {
        return Result.success(asinService.getKeepaTrend(request));
    }

}
