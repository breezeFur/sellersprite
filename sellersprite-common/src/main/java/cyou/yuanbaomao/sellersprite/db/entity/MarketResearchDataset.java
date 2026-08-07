package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_dataset")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研不可变数据集实体")
public class MarketResearchDataset extends BaseAudit {

    @TableId("dataset_id")
    @Schema(description = "数据集ID")
    private String datasetId;

    @TableField("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("node_code")
    @Schema(description = "产生数据集的固定Graph节点编码")
    private String nodeCode;

    @TableField("operation")
    @Schema(description = "外部采集操作编码")
    private String operation;

    @TableField("dataset_code")
    @Schema(description = "数据集业务编码")
    private String datasetCode;

    @TableField("request_hash")
    @Schema(description = "幂等请求SHA-256摘要")
    private String requestHash;

    @TableField("data_source_mode")
    @Schema(description = "数据源模式：MOCK或REMOTE")
    private String dataSourceMode;

    @TableField("request_payload")
    @Schema(description = "脱敏后的请求参数JSON")
    private String requestPayload;

    @TableField("source_payload")
    @Schema(description = "外部接口原始响应JSON")
    private String sourcePayload;

    @TableField("normalized_payload")
    @Schema(description = "可选标准化响应JSON")
    private String normalizedPayload;

    @TableField("record_count")
    @Schema(description = "数据集顶层业务记录数")
    private Integer recordCount;

    @TableField("schema_version")
    @Schema(description = "数据集结构版本")
    private String schemaVersion;

    @TableField("validation_status")
    @Schema(description = "数据集校验状态")
    private String validationStatus;

    @TableField("validation_summary")
    @Schema(description = "数据集校验摘要")
    private String validationSummary;

    @TableField("sha256")
    @Schema(description = "源响应SHA-256十六进制摘要")
    private String sha256;

    @TableField("fetched_at")
    @Schema(description = "数据采集时间，Unix毫秒")
    private Long fetchedAt;
}
