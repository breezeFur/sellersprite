package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("ai_conversation")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 会话实体")
public class AiConversation extends BaseAudit {

    @TableId("conversation_id")
    @Schema(description = "会话ID")
    private String conversationId;

    @TableField("user_id")
    @Schema(description = "会话归属用户ID")
    private String userId;

    @TableField("title")
    @Schema(description = "会话标题")
    private String title;

    @TableField("provider")
    @Schema(description = "模型服务提供方")
    private String provider;

    @TableField("model")
    @Schema(description = "模型名称")
    private String model;

    @TableField("system_prompt")
    @Schema(description = "会话系统提示词")
    private String systemPrompt;

    @TableField("message_count")
    @Schema(description = "前端可见消息数量")
    private Integer messageCount;

    @TableField("last_message_at")
    @Schema(description = "最后消息时间，Unix毫秒")
    private Long lastMessageAt;

    @TableField("status")
    @Schema(description = "会话状态")
    private String status;
}
