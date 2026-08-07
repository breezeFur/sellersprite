package cyou.yuanbaomao.sellersprite.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 聊天响应")
public class AiChatVo {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "助手消息ID")
    private String messageId;

    @Schema(description = "Prompt记录ID")
    private String promptRecordId;

    @Schema(description = "模型回复内容")
    private String content;

    @Schema(description = "模型服务提供方")
    private String provider;

    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "生成时间，Unix 毫秒时间戳")
    private Long createdAt;

    @Schema(description = "输入Token数")
    private Integer promptTokens;

    @Schema(description = "输出Token数")
    private Integer completionTokens;

    @Schema(description = "总Token数")
    private Integer totalTokens;

    @Schema(description = "模型结束原因")
    private String finishReason;
}
