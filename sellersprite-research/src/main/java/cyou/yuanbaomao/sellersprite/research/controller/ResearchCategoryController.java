package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "市场调研类目")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/categories")
public class ResearchCategoryController {

    private final ResearchCategoryService researchCategoryService;

    @Operation(summary = "查询市场调研产品类目", description = "结果按完整查询参数持久化缓存")
    @GetMapping
    public Result<List<ProductNodeVo>> list(@Valid @ModelAttribute ProductNodeRequest request) {
        return Result.success(researchCategoryService.listProductNodes(request));
    }
}
