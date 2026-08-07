package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研SSE聚合帧")
public class ResearchStreamFrameVo {

    @Schema(description = "帧类型：snapshot或events")
    private String frameType;

    @Schema(description = "市场调研任务ID")
    private String jobId;

    @Schema(description = "本帧起始游标，不包含该序号")
    private long afterSequence;

    @Schema(description = "本帧确认的最新事件序号")
    private long lastSequence;

    @Schema(description = "是否已完成持久化事件追赶并进入实时推送阶段")
    private boolean replayComplete;

    @Schema(description = "可选权威任务状态；首帧必填，状态变化时随增量帧刷新")
    private ResearchJobDetailVo job;

    @Schema(description = "可选权威Graph节点状态；与job同时出现")
    private List<ResearchNodeExecutionVo> nodes;

    @Builder.Default
    @Schema(description = "按sequenceNo递增排列的业务事件")
    private List<ResearchEventEnvelope> events = List.of();
}
