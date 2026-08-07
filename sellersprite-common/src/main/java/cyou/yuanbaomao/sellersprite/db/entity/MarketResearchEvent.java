package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_event")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研可重放SSE事件实体")
public class MarketResearchEvent extends BaseAudit {

    @TableId("event_id")
    @Schema(description = "事件ID")
    private String eventId;

    @TableField(value = "sequence_no", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    @Schema(description = "全局单调递增SSE序号")
    private Long sequenceNo;

    @TableField("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("conversation_id")
    @Schema(description = "关联AI会话ID")
    private String conversationId;

    @TableField("analysis_run_id")
    @Schema(description = "关联分析运行ID")
    private String analysisRunId;

    @TableField("scope")
    @Schema(description = "事件作用域")
    private String scope;

    @TableField("event_type")
    @Schema(description = "SSE事件类型")
    private String eventType;

    @TableField("phase")
    @Schema(description = "业务阶段编码")
    private String phase;

    @TableField("sheet_name")
    @Schema(description = "证据Sheet名称")
    private String sheetName;

    @TableField("node_code")
    @Schema(description = "Graph节点编码")
    private String nodeCode;

    @TableField("message")
    @Schema(description = "用户可读消息或模型增量")
    private String message;

    @TableField("payload")
    @Schema(description = "事件结构化JSON载荷")
    private String payload;

    @TableField("terminal")
    @Schema(description = "是否统一工作流终态：1是 0否")
    private Integer terminal;
}
