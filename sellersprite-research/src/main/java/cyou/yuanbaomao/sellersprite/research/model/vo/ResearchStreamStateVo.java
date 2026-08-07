package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研事件流中的权威任务状态快照")
public class ResearchStreamStateVo {

    @Schema(description = "市场调研任务详情")
    private ResearchJobDetailVo job;

    @Builder.Default
    @Schema(description = "Graph节点执行轨迹")
    private List<ResearchNodeExecutionVo> nodes = List.of();
}
