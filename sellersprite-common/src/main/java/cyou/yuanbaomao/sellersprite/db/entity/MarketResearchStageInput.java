package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_stage_input")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研人工阶段输入")
public class MarketResearchStageInput extends BaseAudit {

    @TableId("input_id")
    private String inputId;

    @TableField("job_id")
    private String jobId;

    @TableField("stage_code")
    private String stageCode;

    @TableField("input_type")
    private String inputType;

    @TableField("decision")
    private String decision;

    @TableField("input_payload")
    private String inputPayload;

    @TableField("submitted_by")
    private String submittedBy;

    @TableField("submitted_at")
    private Long submittedAt;
}
