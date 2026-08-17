// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.trademark.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkListRequest;
import cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkStatsRequest;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkRangeVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo;
import cyou.yuanbaomao.sellersprite.api.trademark.service.TrademarkService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Tag(name = "SellerSprite 全球商标", description = "SellerSprite 全球商标分类接口")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/sellersprite/trademarks")
public class TrademarkController {

    private final TrademarkService trademarkService;

    @Operation(summary = "全球商标库-数据范围", description = "通过统一 SellerSpriteClient 调用 /v1/global/brand/range")
    @GetMapping("/range")
    public Result<List<TrademarkRangeVo>> getBrandRange() {
        return Result.success(trademarkService.getBrandRange());
    }

    @Operation(summary = "全球商标库-详情", description = "通过统一 SellerSpriteClient 调用 /v1/global/brand/detail")
    @GetMapping("/detail")
    public Result<TrademarkDetailVo> getBrandDetail(
            @Parameter(description = "数据范围，例如 US")
            @NotBlank @RequestParam("office") String office,
            @Parameter(description = "商标 ID，例如 US502022097612203")
            @NotBlank @RequestParam("brandId") String brandId) {
        return Result.success(trademarkService.getBrandDetail(office, brandId));
    }

    @Operation(summary = "全球商标库-列表", description = "通过统一 SellerSpriteClient 调用 /v1/global/brand/list")
    @PostMapping(value = "/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<TrademarkListVo> listBrands(@Valid @ModelAttribute TrademarkListRequest request) {
        return Result.success(trademarkService.listBrands(request));
    }

    @Operation(summary = "全球商标库-统计", description = "通过统一 SellerSpriteClient 调用 /v1/global/brand/stats")
    @PostMapping(value = "/stats", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<TrademarkStatsVo> getBrandStats(@Valid @ModelAttribute TrademarkStatsRequest request) {
        return Result.success(trademarkService.getBrandStats(request));
    }

}
