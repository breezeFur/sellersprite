package com.yuanbaomao.sellersprite.db.entity;

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
