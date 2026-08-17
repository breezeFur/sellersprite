package cyou.yuanbaomao.sellersprite.system.ops.ai.controller;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.ai.service.AiPromptLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Prompt日志", description = "查询同步与流式AI调用审计日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs/ai-prompts")
public class AiPromptLogController {

    private final AiPromptLogService promptLogService;

    @Operation(summary = "分页查询AI Prompt日志")
    @GetMapping
    public Result<YPage<AiPromptLogVo>> page(@Valid YPage<AiPromptLogVo> page,
            @Valid AiPromptLogPageRequest request) {
        return Result.success(promptLogService.page(page, request));
    }

    @Operation(summary = "查询AI Prompt日志详情")
    @GetMapping("/{promptRecordId}")
    public Result<AiPromptLogVo> detail(@PathVariable String promptRecordId) {
        return Result.success(promptLogService.detail(promptRecordId));
    }
}
