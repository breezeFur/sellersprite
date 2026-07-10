package com.yuanbaomao.sellersprite.ai.controller;

import com.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import com.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import com.yuanbaomao.sellersprite.ai.service.AiChatService;
import com.yuanbaomao.base.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 接口", description = "大模型聊天接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "发送聊天消息", description = "调用 OpenAI 兼容聊天模型生成文本回复")
    @PostMapping("/chat")
    public Result<AiChatVo> chat(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiChatService.chat(request));
    }
}
