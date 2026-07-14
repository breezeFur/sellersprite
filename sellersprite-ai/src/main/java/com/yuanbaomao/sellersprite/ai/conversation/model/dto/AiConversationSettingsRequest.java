package com.yuanbaomao.sellersprite.ai.conversation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI 会话设置更新请求")
public class AiConversationSettingsRequest {

    @Size(max = 2000, message = "系统提示词不能超过2000个字符")
    @Schema(description = "系统提示词，空值表示使用默认设定")
    private String systemPrompt;
}
