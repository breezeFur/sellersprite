package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_node_execution")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研节点执行记录实体")
public class MarketResearchNodeExecution extends BaseAudit {

    @TableId("execution_id")
    @Schema(description = "节点执行记录ID")
    private String executionId;

    @TableField("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("graph_code")
    @Schema(description = "所属子图编码")
    private String graphCode;

    @TableField("node_code")
    @Schema(description = "固定Graph节点编码")
    private String nodeCode;

    @TableField("node_name")
    @Schema(description = "节点中文名称快照")
    private String nodeName;

    @TableField("job_attempt")
    @Schema(description = "节点所属任务执行次数")
    private Integer jobAttempt;

    @TableField("node_attempt")
    @Schema(description = "当前任务执行内的节点尝试序号")
    private Integer nodeAttempt;

    @TableField("execution_status")
    @Schema(description = "节点执行状态")
    private String executionStatus;

    @TableField("started_at")
    @Schema(description = "节点开始时间，Unix毫秒")
    private Long startedAt;

    @TableField("finished_at")
    @Schema(description = "节点结束时间，Unix毫秒")
    private Long finishedAt;

    @TableField("duration_ms")
    @Schema(description = "节点耗时，毫秒")
    private Long durationMs;

    @TableField("error_code")
    @Schema(description = "稳定错误码")
    private String errorCode;

    @TableField("error_message")
    @Schema(description = "可安全展示的错误摘要")
    private String errorMessage;
}
