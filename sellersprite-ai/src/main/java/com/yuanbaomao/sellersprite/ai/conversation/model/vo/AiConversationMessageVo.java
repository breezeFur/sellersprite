package com.yuanbaomao.sellersprite.ai.conversation.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 会话消息")
public class AiConversationMessageVo {

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "关联Prompt记录ID")
    private String promptRecordId;

    @Schema(description = "会话内消息序号")
    private Integer sequenceNo;

    @Schema(description = "消息角色：SYSTEM USER ASSISTANT TOOL")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "消息扩展元数据JSON")
    private String metadata;

    @Schema(description = "消息状态：STREAMING COMPLETED CANCELLED FAILED")
    private String messageStatus;

    @Schema(description = "稳定错误码")
    private String errorCode;

    @Schema(description = "安全错误摘要")
    private String errorMessage;

    @Schema(description = "创建时间，Unix毫秒")
    private Long createdAt;
}
