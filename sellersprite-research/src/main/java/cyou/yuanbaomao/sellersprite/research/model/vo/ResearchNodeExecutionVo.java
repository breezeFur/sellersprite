package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研节点执行详情")
public class ResearchNodeExecutionVo {

    private String executionId;
    private String graphCode;
    private String nodeCode;
    private String nodeName;
    private Integer jobAttempt;
    private Integer nodeAttempt;
    private String status;
    private Long startedAt;
    private Long finishedAt;
    private Long durationMs;
    private String errorCode;
    private String errorMessage;
}
