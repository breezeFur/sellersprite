// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.traffic.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.RelatedTrafficRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficSourceRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.RelatedTrafficVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceVo;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Tag(name = "SellerSprite 流量分析", description = "SellerSprite 流量分析分类接口")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/sellersprite/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    @Operation(summary = "关键词反查(流量词列表)", description = "通过统一 SellerSpriteClient 调用 /v1/traffic/keyword")
    @PostMapping("/keywords/reverse")
    public Result<TrafficKeywordVo> reverseKeywords(@Valid @RequestBody TrafficKeywordRequest request) {
        return Result.success(trafficService.reverseKeywords(request));
    }

    @Operation(summary = "关联流量列表", description = "通过统一 SellerSpriteClient 调用 /v1/traffic/listing/page")
    @PostMapping("/related")
    public Result<RelatedTrafficVo> listRelatedTraffic(@Valid @RequestBody RelatedTrafficRequest request) {
        return Result.success(trafficService.listRelatedTraffic(request));
    }

    @Operation(summary = "流量词统计", description = "通过统一 SellerSpriteClient 调用 /v1/traffic/keyword/stat/{marketplace}/{asin}")
    @GetMapping("/keywords/stats")
    public Result<TrafficKeywordStatVo> getKeywordStats(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "ASIN，例如 B07Z82895W")
            @NotBlank @RequestParam("asin") String asin,
            @Parameter(description = "查询月份，例如 202605")
            @RequestParam(value = "month", required = false) String month) {
        return Result.success(trafficService.getKeywordStats(marketplace, asin, month));
    }

    @Operation(summary = "关联流量统计", description = "通过统一 SellerSpriteClient 调用 /v1/traffic/listing/stat/{marketplace}/{asin}")
    @GetMapping("/listings/stats")
    public Result<TrafficListingStatVo> getListingStats(
            @Parameter(description = "ASIN，例如 B07Z82895W")
            @NotBlank @RequestParam("asin") String asin,
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "关联 ASIN 列表，可重复传入 asinList")
            @RequestParam(value = "asinList", required = false) List<String> asinList) {
        return Result.success(trafficService.getListingStats(asin, marketplace, asinList));
    }

    @Operation(summary = "查流量来源(关键词流向)", description = "通过统一 SellerSpriteClient 调用 /v1/traffic/source")
    @PostMapping("/sources")
    public Result<TrafficSourceVo> getTrafficSources(@Valid @RequestBody TrafficSourceRequest request) {
        return Result.success(trafficService.getTrafficSources(request));
    }

}
