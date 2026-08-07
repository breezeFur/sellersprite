package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("ai_prompt_record")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI Prompt调用记录实体")
public class AiPromptRecord extends BaseAudit {

    @TableId("prompt_record_id")
    @Schema(description = "Prompt记录ID")
    private String promptRecordId;

    @TableField("conversation_id")
    @Schema(description = "会话ID")
    private String conversationId;

    @TableField("user_id")
    @Schema(description = "调用用户ID")
    private String userId;

    @TableField("provider")
    @Schema(description = "模型服务提供方")
    private String provider;

    @TableField("model")
    @Schema(description = "模型名称")
    private String model;

    @TableField("request_messages")
    @Schema(description = "实际送模消息JSON")
    private String requestMessages;

    @TableField("prompt_summary")
    @Schema(description = "截断脱敏后的Prompt摘要")
    private String promptSummary;

    @TableField("prompt_truncated")
    @Schema(description = "摘要是否被截断：1是 0否")
    private Integer promptTruncated;

    @TableField("response_content")
    @Schema(description = "模型响应文本")
    private String responseContent;

    @TableField("response_metadata")
    @Schema(description = "模型响应元数据JSON")
    private String responseMetadata;

    @TableField("prompt_tokens")
    @Schema(description = "输入Token数")
    private Integer promptTokens;

    @TableField("completion_tokens")
    @Schema(description = "输出Token数")
    private Integer completionTokens;

    @TableField("total_tokens")
    @Schema(description = "总Token数")
    private Integer totalTokens;

    @TableField("finish_reason")
    @Schema(description = "模型结束原因")
    private String finishReason;

    @TableField("status")
    @Schema(description = "调用状态：PROCESSING SUCCESS CANCELLED FAILED")
    private String status;

    @TableField("error_type")
    @Schema(description = "错误类型")
    private String errorType;

    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    @TableField("cost_ms")
    @Schema(description = "调用耗时毫秒")
    private Long costMs;

    @TableField("track_id")
    @Schema(description = "链路追踪ID")
    private String trackId;
}
