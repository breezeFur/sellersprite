// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.product.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "SellerSprite 产品分析", description = "SellerSprite 产品分析分类接口")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/sellersprite/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "查竞品", description = "通过统一 SellerSpriteClient 调用 /v1/product/competitor-lookup")
    @PostMapping("/competitors")
    public Result<CompetitorLookupVo> lookupCompetitors(@Valid @RequestBody CompetitorLookupRequest request) {
        return Result.success(productService.lookupCompetitors(request));
    }

    @Operation(summary = "选产品", description = "通过统一 SellerSpriteClient 调用 /v1/product/research")
    @PostMapping("/research")
    public Result<ProductResearchVo> researchProducts(@Valid @RequestBody ProductResearchRequest request) {
        return Result.success(productService.researchProducts(request));
    }

    @Operation(summary = "查产品类目", description = "通过统一 SellerSpriteClient 调用 /v1/product/node")
    @GetMapping("/nodes")
    public Result<List<ProductNodeVo>> listProductNodes(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "类目节点 ID 路径")
            @RequestParam(value = "nodeIdPath", required = false) String nodeIdPath,
            @Parameter(description = "搜索关键字")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "历史月份，格式 yyyyMM")
            @RequestParam(value = "month", required = false) String month) {
        return Result.success(productService.listProductNodes(marketplace, nodeIdPath, keyword, month));
    }

}
