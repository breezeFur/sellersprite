package cyou.yuanbaomao.sellersprite.ai.conversation.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "AI 会话详情")
public class AiConversationDetailVo {

    @Schema(description = "会话摘要")
    private AiConversationVo conversation;

    @Schema(description = "按顺序排列的完整前端可见消息")
    private List<AiConversationMessageVo> messages;

    @Schema(description = "会话设置")
    private AiConversationSettingsVo settings;
}
