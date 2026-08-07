package cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI Prompt日志")
public class AiPromptLogVo {

    private String promptRecordId;
    private String conversationId;
    private String userId;
    private String provider;
    private String model;
    private String requestMessages;
    private String promptSummary;
    private Integer promptTruncated;
    private String responseContent;
    private String responseMetadata;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private String finishReason;
    private String status;
    private String errorType;
    private String errorMessage;
    private Long costMs;
    private String trackId;
    private Long createdAt;
}
