package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 市场调研固定工作流拓扑。 */
@Data
@Builder
@Schema(description = "市场调研固定工作流拓扑")
public class ResearchWorkflowTopologyVo {

    @Schema(description = "拓扑描述格式，当前固定为MERMAID")
    private String type;

    @Schema(description = "工作流标题")
    private String title;

    @Schema(description = "由当前已编译Graph生成的Mermaid定义")
    private String content;

    @Builder.Default
    @Schema(description = "按父Graph实际执行顺序排列的业务步骤")
    private List<ResearchWorkflowStepVo> steps = List.of();
}
