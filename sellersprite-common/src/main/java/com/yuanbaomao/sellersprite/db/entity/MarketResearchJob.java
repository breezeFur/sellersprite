package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_job")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研任务实体")
public class MarketResearchJob extends BaseAudit {

    @TableId("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("user_id")
    @Schema(description = "任务归属用户ID")
    private String userId;

    @TableField("report_name")
    @Schema(description = "报告名称")
    private String reportName;

    @TableField("marketplace")
    @Schema(description = "Amazon站点，第一版固定为US")
    private String marketplace;

    @TableField("keyword")
    @Schema(description = "核心调研关键词")
    private String keyword;

    @TableField("seed_asins")
    @Schema(description = "可选种子ASIN数组JSON")
    private String seedAsins;

    @TableField("template_code")
    @Schema(description = "Excel模板代码")
    private String templateCode;

    @TableField("data_source_mode")
    @Schema(description = "数据源模式：MOCK或REMOTE")
    private String dataSourceMode;

    @TableField("job_status")
    @Schema(description = "任务状态")
    private String jobStatus;

    @TableField("current_phase")
    @Schema(description = "当前Spring Batch业务阶段")
    private String currentPhase;

    @TableField("progress")
    @Schema(description = "任务进度，范围0到100")
    private Integer progress;

    @TableField("batch_job_instance_id")
    @Schema(description = "Spring Batch JobInstance ID")
    private Long batchJobInstanceId;

    @TableField("batch_job_execution_id")
    @Schema(description = "Spring Batch JobExecution ID")
    private Long batchJobExecutionId;

    @TableField("error_code")
    @Schema(description = "稳定业务错误码")
    private String errorCode;

    @TableField("error_message")
    @Schema(description = "可安全展示的错误摘要")
    private String errorMessage;

    @TableField("started_at")
    @Schema(description = "任务开始时间，Unix毫秒")
    private Long startedAt;

    @TableField("finished_at")
    @Schema(description = "任务结束时间，Unix毫秒")
    private Long finishedAt;
}
