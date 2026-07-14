package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("market_research_snapshot")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市场调研采集快照实体")
public class MarketResearchSnapshot extends BaseAudit {

    @TableId("snapshot_id")
    @Schema(description = "采集快照ID")
    private String snapshotId;

    @TableField("job_id")
    @Schema(description = "市场调研任务ID")
    private String jobId;

    @TableField("phase")
    @Schema(description = "产生快照的Spring Batch业务阶段")
    private String phase;

    @TableField("operation")
    @Schema(description = "采集操作编码")
    private String operation;

    @TableField("business_key")
    @Schema(description = "分页或ASIN等业务去重键")
    private String businessKey;

    @TableField("data_source_mode")
    @Schema(description = "数据源模式：MOCK或REMOTE")
    private String dataSourceMode;

    @TableField("request_payload")
    @Schema(description = "脱敏后的请求参数JSON")
    private String requestPayload;

    @TableField("response_payload")
    @Schema(description = "采集响应JSON")
    private String responsePayload;

    @TableField("record_count")
    @Schema(description = "快照包含的顶层业务记录数")
    private Integer recordCount;

    @TableField("sha256")
    @Schema(description = "响应内容SHA-256十六进制摘要")
    private String sha256;

    @TableField("fetched_at")
    @Schema(description = "数据采集时间，Unix毫秒")
    private Long fetchedAt;
}
