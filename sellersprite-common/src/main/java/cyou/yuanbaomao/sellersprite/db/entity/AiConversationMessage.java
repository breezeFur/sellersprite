package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("ai_conversation_message")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 会话消息实体")
public class AiConversationMessage extends BaseCreateTime {

    @TableId("message_id")
    @Schema(description = "消息ID")
    private String messageId;

    @TableField("conversation_id")
    @Schema(description = "会话ID")
    private String conversationId;

    @TableField("user_id")
    @Schema(description = "会话归属用户ID")
    private String userId;

    @TableField("prompt_record_id")
    @Schema(description = "关联Prompt记录ID")
    private String promptRecordId;

    @TableField("sequence_no")
    @Schema(description = "会话内消息序号")
    private Integer sequenceNo;

    @TableField("role")
    @Schema(description = "消息角色：SYSTEM USER ASSISTANT TOOL")
    private String role;

    @TableField("content")
    @Schema(description = "消息内容")
    private String content;

    @TableField("content_type")
    @Schema(description = "内容类型")
    private String contentType;

    @TableField("metadata")
    @Schema(description = "消息扩展元数据JSON")
    private String metadata;

    @TableField("message_status")
    @Schema(description = "消息状态：STREAMING COMPLETED CANCELLED FAILED")
    private String messageStatus;

    @TableField("error_code")
    @Schema(description = "稳定错误码")
    private String errorCode;

    @TableField("error_message")
    @Schema(description = "安全错误摘要")
    private String errorMessage;

    @TableLogic
    @TableField("deleted")
    @Schema(description = "逻辑删除：0正常 1删除")
    private Integer deleted;
}
