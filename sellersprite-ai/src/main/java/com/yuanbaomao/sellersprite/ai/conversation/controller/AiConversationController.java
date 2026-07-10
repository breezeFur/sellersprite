package com.yuanbaomao.sellersprite.ai.conversation.controller;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationPageRequest;
import com.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationRenameRequest;
import com.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import com.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;
import com.yuanbaomao.sellersprite.ai.conversation.service.AiConversationService;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 会话管理", description = "管理当前用户的 AI 会话和完整聊天历史")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/conversations")
public class AiConversationController {

    private final AiConversationService conversationService;

    @Operation(summary = "分页查询会话")
    @GetMapping
    public Result<PageResult<AiConversationVo>> page(@Valid AiConversationPageRequest request) {
        return Result.success(conversationService.page(request));
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

    @Operation(summary = "删除会话")
    @DeleteMapping("/{conversationId}")
    public Result<Void> delete(@PathVariable String conversationId) {
        conversationService.delete(conversationId);
        return Result.success();
    }
}
