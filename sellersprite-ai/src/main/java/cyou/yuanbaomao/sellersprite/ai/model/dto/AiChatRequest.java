package cyou.yuanbaomao.sellersprite.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI 聊天请求")
public class AiChatRequest {

    @Size(max = 36, message = "会话ID不能超过36个字符")
    @Schema(description = "会话ID，不传则自动创建新会话")
    private String conversationId;

    @NotBlank(message = "用户消息不能为空")
    @Size(max = 10000, message = "用户消息不能超过10000个字符")
    @Schema(description = "用户消息", example = "请用三句话介绍这个系统")
    private String prompt;

    @Size(max = 2000, message = "系统提示词不能超过2000个字符")
    @Schema(description = "系统提示词，仅创建新会话时生效，不传则使用默认助手设定")
    private String systemPrompt;
}
