package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchAnalysisRunVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 前端按分析运行ID执行重试和取消的稳定命令入口。 */
@Tag(name = "市场调研AI分析运行")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/analysis-runs")
public class ResearchAnalysisRunController {

    private final ResearchAnalysisService analysisService;

    @Operation(summary = "按运行ID重试AI分析")
    @PostMapping("/{analysisRunId}/retry")
    public Result<ResearchAnalysisRunVo> retry(@PathVariable String analysisRunId) {
        return Result.success(analysisService.retryRun(analysisRunId));
    }

    @Operation(summary = "按运行ID继续AI分析")
    @PostMapping("/{analysisRunId}/continue")
    public Result<ResearchAnalysisRunVo> continueAnalysis(@PathVariable String analysisRunId) {
        return Result.success(analysisService.continueRun(analysisRunId));
    }

    @Operation(summary = "按运行ID取消AI分析")
    @PostMapping("/{analysisRunId}/cancel")
    public Result<Void> cancel(@PathVariable String analysisRunId) {
        analysisService.cancelRun(analysisRunId);
        return Result.success();
    }
}
