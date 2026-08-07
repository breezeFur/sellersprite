package cyou.yuanbaomao.sellersprite.ai.conversation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI 会话重命名请求")
public class AiConversationRenameRequest {

    @NotBlank(message = "会话标题不能为空")
    @Size(max = 128, message = "会话标题不能超过128个字符")
    @Schema(description = "会话标题", example = "Spring AI 会话管理设计")
    private String title;
}
