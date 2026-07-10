// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.controller;

import com.yuanbaomao.base.result.Result;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite 市场分析", description = "SellerSprite 市场分析分类接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/markets")
public class MarketController {

    private final MarketService marketService;

    @Operation(summary = "选市场列表", description = "通过统一 SellerSpriteClient 调用 /v1/market/research")
    @PostMapping("/research")
    public Result<MarketResearchVo> researchMarkets(@Valid @RequestBody MarketResearchRequest request) {
        return Result.success(marketService.researchMarkets(request));
    }

    @Operation(summary = "选市场-统计", description = "通过统一 SellerSpriteClient 调用 /v1/market/statistics")
    @PostMapping("/statistics")
    public Result<MarketStatisticsVo> getMarketStatistics(@Valid @RequestBody MarketStatisticsRequest request) {
        return Result.success(marketService.getMarketStatistics(request));
    }

    @Operation(summary = "选市场-商品集中度", description = "通过统一 SellerSpriteClient 调用 /v1/market/goods")
    @PostMapping("/goods")
    public Result<List<MarketGoodsConcentrationVo>> getGoodsConcentration(@Valid @RequestBody MarketGoodsConcentrationRequest request) {
        return Result.success(marketService.getGoodsConcentration(request));
    }

    @Operation(summary = "选市场-品牌集中度", description = "通过统一 SellerSpriteClient 调用 /v1/market/brand")
    @PostMapping("/brands")
    public Result<List<MarketBrandConcentrationVo>> getBrandConcentration(@Valid @RequestBody MarketBrandConcentrationRequest request) {
        return Result.success(marketService.getBrandConcentration(request));
    }

    @Operation(summary = "选市场-卖家所属地分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/seller/location")
    @PostMapping("/sellers/locations")
    public Result<List<MarketSellerLocationVo>> getSellerLocationDistribution(@Valid @RequestBody MarketSellerLocationRequest request) {
        return Result.success(marketService.getSellerLocationDistribution(request));
    }

    @Operation(summary = "选市场-卖家集中度", description = "通过统一 SellerSpriteClient 调用 /v1/market/seller")
    @PostMapping("/sellers")
    public Result<List<MarketSellerConcentrationVo>> getSellerConcentration(@Valid @RequestBody MarketSellerConcentrationRequest request) {
        return Result.success(marketService.getSellerConcentration(request));
    }

    @Operation(summary = "选市场-卖家类型分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/seller/type")
    @PostMapping("/sellers/types")
    public Result<List<MarketSellerTypeVo>> getSellerTypeDistribution(@Valid @RequestBody MarketSellerTypeRequest request) {
        return Result.success(marketService.getSellerTypeDistribution(request));
    }

    @Operation(summary = "选市场-商品需求趋势", description = "通过统一 SellerSpriteClient 调用 /v1/market/performance")
    @PostMapping("/demand-trend")
    public Result<MarketDemandTrendVo> getDemandTrend(@Valid @RequestBody MarketDemandTrendRequest request) {
        return Result.success(marketService.getDemandTrend(request));
    }

    @Operation(summary = "选市场-上架时间分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/shelf/time")
    @PostMapping("/shelf-times")
    public Result<List<MarketShelfTimeVo>> getShelfTimeDistribution(@Valid @RequestBody MarketShelfTimeRequest request) {
        return Result.success(marketService.getShelfTimeDistribution(request));
    }

    @Operation(summary = "选市场-上架趋势分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/shelf/trend")
    @PostMapping("/shelf-trends")
    public Result<List<MarketShelfTrendVo>> getShelfTrendDistribution(@Valid @RequestBody MarketShelfTrendRequest request) {
        return Result.success(marketService.getShelfTrendDistribution(request));
    }

    @Operation(summary = "选市场-评分数分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/ratings")
    @PostMapping("/ratings")
    public Result<List<MarketRatingsDistributionVo>> getRatingsDistribution(@Valid @RequestBody MarketRatingsDistributionRequest request) {
        return Result.success(marketService.getRatingsDistribution(request));
    }

    @Operation(summary = "选市场-评分值分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/rating")
    @PostMapping("/rating")
    public Result<List<MarketRatingDistributionVo>> getRatingDistribution(@Valid @RequestBody MarketRatingDistributionRequest request) {
        return Result.success(marketService.getRatingDistribution(request));
    }

    @Operation(summary = "选市场-价格分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/price")
    @PostMapping("/prices")
    public Result<List<MarketPriceDistributionVo>> getPriceDistribution(@Valid @RequestBody MarketPriceDistributionRequest request) {
        return Result.success(marketService.getPriceDistribution(request));
    }

    @Operation(summary = "选市场-A+视频分布", description = "通过统一 SellerSpriteClient 调用 /v1/market/ebc")
    @PostMapping("/ebc")
    public Result<List<MarketEbcDistributionVo>> getEbcDistribution(@Valid @RequestBody MarketEbcDistributionRequest request) {
        return Result.success(marketService.getEbcDistribution(request));
    }

}
