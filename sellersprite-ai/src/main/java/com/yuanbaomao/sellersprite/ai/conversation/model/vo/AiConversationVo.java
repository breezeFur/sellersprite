package com.yuanbaomao.sellersprite.ai.conversation.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 会话摘要")
public class AiConversationVo {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "模型服务提供方")
    private String provider;

    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "可见消息数量")
    private Integer messageCount;

    @Schema(description = "最后消息时间，Unix毫秒")
    private Long lastMessageAt;

    @Schema(description = "会话状态")
    private String status;

    @Schema(description = "创建时间，Unix毫秒")
    private Long createdAt;

    @Schema(description = "更新时间，Unix毫秒")
    private Long updatedAt;
}
