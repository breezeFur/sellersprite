package cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto;

import cyou.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI Prompt日志分页查询")
public class AiPromptLogPageRequest extends PageQuery {

    @Size(max = 36)
    private String userId;

    @Size(max = 36)
    private String conversationId;

    @Size(max = 64)
    private String provider;

    @Size(max = 128)
    private String model;

    @Size(max = 32)
    private String status;

    private Long startTime;

    private Long endTime;
}
