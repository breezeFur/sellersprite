package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_artifact")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研Excel产物实体")
public class MarketResearchArtifact extends BaseAudit {

    @TableId("artifact_id")
    @Schema(description = "报告产物ID")
    private String artifactId;

    @TableField("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("analysis_run_id")
    @Schema(description = "可选关联分析运行ID")
    private String analysisRunId;

    @TableField("artifact_scope_id")
    @Schema(description = "产物唯一作用域，数据报告使用任务ID，AI报告使用分析运行ID")
    private String artifactScopeId;

    @TableField("workflow_version")
    @Schema(description = "生成产物的工作流版本")
    private String workflowVersion;

    @TableField("artifact_type")
    @Schema(description = "产物类型")
    private String artifactType;

    @TableField("file_name")
    @Schema(description = "下载文件名")
    private String fileName;

    @TableField("storage_key")
    @Schema(description = "受控存储键")
    private String storageKey;

    @TableField("media_type")
    @Schema(description = "文件媒体类型")
    private String mediaType;

    @TableField("file_size")
    @Schema(description = "文件大小，单位字节")
    private Long fileSize;

    @TableField("sha256")
    @Schema(description = "文件SHA-256十六进制摘要")
    private String sha256;

    @TableField("artifact_status")
    @Schema(description = "产物状态")
    private String artifactStatus;

    @TableField("published_at")
    @Schema(description = "发布时间，Unix毫秒")
    private Long publishedAt;
}
