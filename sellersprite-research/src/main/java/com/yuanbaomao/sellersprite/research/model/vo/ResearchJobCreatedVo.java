package com.yuanbaomao.sellersprite.research.model.vo;

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
}
