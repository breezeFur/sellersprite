package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import cyou.yuanbaomao.sellersprite.research.model.dto.CategoryResolveByAsinsRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchCategoryCandidateVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "市场调研类目")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/market-research/categories")
public class ResearchCategoryController {

    private final ResearchCategoryService researchCategoryService;

    @Operation(summary = "查询市场调研产品类目", description = "结果按完整查询参数持久化缓存")
    @GetMapping
    public Result<List<ProductNodeVo>> list(
            @Parameter(description = "市场，例如 US")
            @NotNull @RequestParam("marketplace") SellerSpriteMarketplace marketplace,
            @Parameter(description = "类目节点 ID 路径")
            @RequestParam(value = "nodeIdPath", required = false) String nodeIdPath,
            @Parameter(description = "搜索关键字")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "历史月份，格式 yyyyMM")
            @RequestParam(value = "month", required = false) String month) {
        return Result.success(researchCategoryService.listProductNodes(marketplace, nodeIdPath, keyword, month));
    }

    @Operation(summary = "通过 ASIN 反查所属类目候选", description = "批量反查 ASIN 所属类目并聚合推荐候选列表")
    @PostMapping("/resolve-by-asins")
    public Result<List<ResearchCategoryCandidateVo>> resolveByAsins(
            @Valid @RequestBody CategoryResolveByAsinsRequest request) {
        return Result.success(researchCategoryService.resolveCategoriesByAsins(request));
    }
}
