package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_analysis_run")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研AI分析运行实体")
public class MarketResearchAnalysisRun extends BaseAudit {

    @TableId("analysis_run_id")
    @Schema(description = "分析运行ID")
    private String analysisRunId;

    @TableField("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("user_id")
    @Schema(description = "分析运行归属用户ID")
    private String userId;

    @TableField("conversation_id")
    @Schema(description = "关联AI会话ID")
    private String conversationId;

    @TableField("parent_run_id")
    @Schema(description = "重试或追问来源分析运行ID")
    private String parentRunId;

    @TableField("run_type")
    @Schema(description = "分析类型")
    private String runType;

    @TableField("analysis_goal")
    @Schema(description = "本次分析目标或后续问题")
    private String analysisGoal;

    @TableField("run_status")
    @Schema(description = "分析运行状态")
    private String runStatus;

    @TableField("current_phase")
    @Schema(description = "当前分析阶段编码")
    private String currentPhase;

    @TableField("progress")
    @Schema(description = "分析进度，范围0到100")
    private Integer progress;

    @TableField("attempt_count")
    @Schema(description = "已抢占执行次数")
    private Integer attemptCount;

    @TableField("max_attempts")
    @Schema(description = "最大自动执行次数")
    private Integer maxAttempts;

    @TableField("next_run_at")
    @Schema(description = "下次允许调度时间，Unix毫秒")
    private Long nextRunAt;

    @TableField("execution_owner")
    @Schema(description = "当前执行实例标识")
    private String executionOwner;

    @TableField("execution_token")
    @Schema(description = "当前领取令牌")
    private String executionToken;

    @TableField("lease_until")
    @Schema(description = "执行租约截止时间，Unix毫秒")
    private Long leaseUntil;

    @TableField("heartbeat_at")
    @Schema(description = "最近心跳时间，Unix毫秒")
    private Long heartbeatAt;

    @TableField("cancel_requested_at")
    @Schema(description = "取消请求时间，Unix毫秒")
    private Long cancelRequestedAt;

    @TableField("model_call_count")
    @Schema(description = "累计模型调用次数")
    private Integer modelCallCount;

    @TableField("event_count")
    @Schema(description = "累计持久化事件数")
    private Integer eventCount;

    @TableField("final_summary")
    @Schema(description = "最终分析摘要Markdown")
    private String finalSummary;

    @TableField("error_code")
    @Schema(description = "稳定业务错误码")
    private String errorCode;

    @TableField("error_message")
    @Schema(description = "可安全展示的错误摘要")
    private String errorMessage;

    @TableField("started_at")
    @Schema(description = "首次开始时间，Unix毫秒")
    private Long startedAt;

    @TableField("finished_at")
    @Schema(description = "终态完成时间，Unix毫秒")
    private Long finishedAt;
}
