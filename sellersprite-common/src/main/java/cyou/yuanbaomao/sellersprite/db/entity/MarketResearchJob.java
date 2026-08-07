package cyou.yuanbaomao.sellersprite.db.entity;

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
    @Schema(description = "Amazon站点")
    private String marketplace;

    @TableField("node_id_path")
    @Schema(description = "SellerSprite类目节点路径")
    private String nodeIdPath;

    @TableField("research_month")
    @Schema(description = "调研月份，yyyy-MM格式")
    private String researchMonth;

    @TableField("keyword")
    @Schema(description = "可选核心调研关键词")
    private String keyword;

    @TableField("seed_asins")
    @Schema(description = "可选种子ASIN数组JSON")
    private String seedAsins;

    @TableField("collection_config")
    @Schema(description = "采集子图强类型参数JSON快照")
    private String collectionConfig;

    @TableField("template_code")
    @Schema(description = "Excel模板代码")
    private String templateCode;

    @TableField("data_source_mode")
    @Schema(description = "数据源模式：MOCK或REMOTE")
    private String dataSourceMode;

    @TableField("workflow_version")
    @Schema(description = "固定市场调研工作流版本")
    private String workflowVersion;

    @TableField("job_status")
    @Schema(description = "任务状态")
    private String jobStatus;

    @TableField("current_node")
    @Schema(description = "当前市场调研Graph节点编码")
    private String currentNode;

    @TableField("current_stage")
    @Schema(description = "当前业务阶段：SCREENING DEEP_DIVE FINAL_ANALYSIS")
    private String currentStage;

    @TableField("waiting_input_type")
    @Schema(description = "当前等待的人工输入类型")
    private String waitingInputType;

    @TableField("progress")
    @Schema(description = "任务进度，范围0到100")
    private Integer progress;

    @TableField("error_code")
    @Schema(description = "稳定业务错误码")
    private String errorCode;

    @TableField("error_message")
    @Schema(description = "可安全展示的错误摘要")
    private String errorMessage;

    @TableField("attempt_count")
    @Schema(description = "任务已抢占执行次数")
    private Integer attemptCount;

    @TableField("max_attempts")
    @Schema(description = "任务最大自动执行次数")
    private Integer maxAttempts;

    @TableField("next_run_at")
    @Schema(description = "下次允许调度时间，Unix毫秒")
    private Long nextRunAt;

    @TableField("execution_owner")
    @Schema(description = "当前执行实例标识")
    private String executionOwner;

    @TableField("execution_token")
    @Schema(description = "当前任务领取令牌，租约转移时更新")
    private String executionToken;

    @TableField("lease_until")
    @Schema(description = "当前执行租约截止时间，Unix毫秒")
    private Long leaseUntil;

    @TableField("heartbeat_at")
    @Schema(description = "最近一次执行心跳时间，Unix毫秒")
    private Long heartbeatAt;

    @TableField("cancel_requested_at")
    @Schema(description = "取消请求时间，Unix毫秒")
    private Long cancelRequestedAt;

    @TableField("started_at")
    @Schema(description = "任务开始时间，Unix毫秒")
    private Long startedAt;

    @TableField("finished_at")
    @Schema(description = "任务结束时间，Unix毫秒")
    private Long finishedAt;
}
