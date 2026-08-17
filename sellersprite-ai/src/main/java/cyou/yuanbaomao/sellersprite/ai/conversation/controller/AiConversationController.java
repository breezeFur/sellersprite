package cyou.yuanbaomao.sellersprite.ai.conversation.controller;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationRenameRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationSettingsRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationSettingsVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.service.AiConversationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 会话管理", description = "管理当前用户的 AI 会话和完整聊天历史")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/conversations")
public class AiConversationController {

    private final AiConversationService conversationService;

    @Operation(summary = "分页查询会话")
    @GetMapping
    public Result<YPage<AiConversationVo>> page(@Valid YPage<AiConversationVo> page,
            @Parameter(description = "会话标题，支持模糊查询")
            @RequestParam(value = "title", required = false) String title) {
        return Result.success(conversationService.page(page, title));
    }

    @Operation(summary = "查询会话详情", description = "返回不受模型Memory窗口影响的完整前端可见消息")
    @GetMapping("/{conversationId}")
    public Result<AiConversationDetailVo> detail(@PathVariable String conversationId) {
        return Result.success(conversationService.detail(conversationId));
    }

    @Operation(summary = "重命名会话")
    @PutMapping("/{conversationId}")
    public Result<AiConversationVo> rename(@PathVariable String conversationId,
                                           @Valid @RequestBody AiConversationRenameRequest request) {
        return Result.success(conversationService.rename(conversationId, request));
    }

    @Operation(summary = "更新会话设置", description = "当前只支持更新系统提示词，提供方与模型只读")
    @PutMapping("/{conversationId}/settings")
    public Result<AiConversationSettingsVo> updateSettings(
            @PathVariable String conversationId,
            @Valid @RequestBody AiConversationSettingsRequest request) {
        return Result.success(conversationService.updateSettings(conversationId, request));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{conversationId}")
    public Result<Void> delete(@PathVariable String conversationId) {
        conversationService.delete(conversationId);
        return Result.success();
    }
}
