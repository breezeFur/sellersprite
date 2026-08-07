package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 市场调研父 Graph 的业务步骤。 */
@Data
@Builder
@Schema(description = "市场调研工作流步骤")
public class ResearchWorkflowStepVo {

    @Schema(description = "稳定业务步骤编码")
    private String code;

    @Schema(description = "父 Graph 节点编码")
    private String nodeCode;

    @Schema(description = "前端展示中文名")
    private String label;
}
