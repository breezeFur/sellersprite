package cyou.yuanbaomao.sellersprite.ai.conversation.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 会话设置")
public class AiConversationSettingsVo {

    @Schema(description = "模型服务提供方，只读")
    private String provider;

    @Schema(description = "模型名称，只读")
    private String model;

    @Schema(description = "系统提示词，空值表示使用默认设定")
    private String systemPrompt;
}
