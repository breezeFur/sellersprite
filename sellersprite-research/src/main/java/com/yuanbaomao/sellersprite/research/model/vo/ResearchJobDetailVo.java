package com.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研任务详情")
public class ResearchJobDetailVo {

    private String jobId;
    private String reportName;
    private String marketplace;
    private String keyword;
    private String dataSourceMode;
    private String status;
    private String currentPhase;
    private String currentPhaseName;
    private Integer progress;
    private Long batchJobExecutionId;
    private String errorCode;
    private String errorMessage;
    private Long startedAt;
    private Long finishedAt;
    private Long createdAt;
    private Boolean downloadable;
    private String fileName;
}
