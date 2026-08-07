package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchProductSelectionRequest;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEvidencePageVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEvidenceTableSummaryVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchProductSelectionVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageDataQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "市场调研阶段数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/jobs")
public class ResearchStageController {

    private final ResearchStageInputService stageInputService;
    private final ResearchStageDataQueryService stageDataQueryService;

    @Operation(summary = "查询指定阶段的证据表目录")
    @GetMapping("/{jobId}/evidence")
    public Result<java.util.List<ResearchEvidenceTableSummaryVo>> evidence(
            @PathVariable String jobId,
            @RequestParam EvidenceStage stageCode) {
        return Result.success(stageDataQueryService.listEvidence(jobId, stageCode));
    }

    @Operation(summary = "分页查询一张已持久化证据表")
    @GetMapping("/{jobId}/evidence/{datasetCode}")
    public Result<ResearchEvidencePageVo> evidencePage(
            @PathVariable String jobId,
            @PathVariable String datasetCode,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "50") long size) {
        return Result.success(stageDataQueryService.pageEvidence(
                jobId, datasetCode, current, size));
    }

    @Operation(summary = "查询阶段一Top20商品和已提交选择")
    @GetMapping("/{jobId}/product-selection")
    public Result<ResearchProductSelectionVo> productSelection(@PathVariable String jobId) {
        return Result.success(stageInputService.getForCurrentUser(jobId));
    }

    @Operation(summary = "提交阶段一商品选择或放弃市场")
    @PostMapping("/{jobId}/product-selection")
    public Result<ResearchProductSelectionVo> submitProductSelection(
            @PathVariable String jobId,
            @Valid @RequestBody ResearchProductSelectionRequest request) {
        return Result.success(stageInputService.submitForCurrentUser(jobId, request));
    }
}
