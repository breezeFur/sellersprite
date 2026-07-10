// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.product.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import com.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite 产品分析", description = "SellerSprite 产品分析分类接口")
@RestController
@RequiredArgsConstructor
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
    public Result<List<ProductNodeVo>> listProductNodes(@Valid @ModelAttribute ProductNodeRequest request) {
        return Result.success(productService.listProductNodes(request));
    }

}
