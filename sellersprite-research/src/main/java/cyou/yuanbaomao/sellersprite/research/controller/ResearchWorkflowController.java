package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchWorkflowTopologyVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "市场调研工作流")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/workflow")
public class ResearchWorkflowController {

    private final ResearchWorkflowService researchWorkflowService;

    @Operation(summary = "查询市场调研固定工作流拓扑")
    @GetMapping
    public Result<ResearchWorkflowTopologyVo> topology() {
        return Result.success(researchWorkflowService.topology());
    }
}
