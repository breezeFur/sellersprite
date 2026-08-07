package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研任务创建结果")
public class ResearchJobCreatedVo {

    @Schema(description = "任务ID")
    private String jobId;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "本次任务固定的数据源模式")
    private String dataSourceMode;

    @Schema(description = "固定工作流版本")
    private String workflowVersion;

    @Schema(description = "关联AI会话ID")
    private String conversationId;

    @Schema(description = "初次分析运行ID")
    private String analysisRunId;

    @Schema(description = "初次分析状态")
    private String analysisStatus;
}
