package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchAnalysisMessageRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchAnalysisRunVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "市场调研AI分析")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/jobs")
public class ResearchAnalysisController {

    private final ResearchAnalysisService analysisService;

    @Operation(summary = "查询任务全部AI分析运行")
    @GetMapping("/{jobId}/analyses")
    public Result<List<ResearchAnalysisRunVo>> analyses(@PathVariable String jobId) {
        return Result.success(analysisService.list(jobId));
    }

    @Operation(summary = "查询任务最新AI分析运行")
    @GetMapping("/{jobId}/analysis")
    public Result<ResearchAnalysisRunVo> latest(@PathVariable String jobId) {
        return Result.success(analysisService.latest(jobId));
    }

    @Operation(summary = "基于现有证据重试失败的AI分析")
    @PostMapping("/{jobId}/analysis/retry")
    public Result<ResearchAnalysisRunVo> retry(@PathVariable String jobId) {
        return Result.success(analysisService.retry(jobId));
    }

    @Operation(summary = "取消当前AI分析")
    @PostMapping("/{jobId}/analysis/cancel")
    public Result<Void> cancel(@PathVariable String jobId) {
        analysisService.cancel(jobId);
        return Result.success();
    }

    @Operation(summary = "基于同一任务证据继续追问")
    @PostMapping("/{jobId}/messages")
    public Result<ResearchAnalysisRunVo> followUp(
            @PathVariable String jobId,
            @Valid @RequestBody ResearchAnalysisMessageRequest request) {
        return Result.success(analysisService.followUp(jobId, request));
    }
}
