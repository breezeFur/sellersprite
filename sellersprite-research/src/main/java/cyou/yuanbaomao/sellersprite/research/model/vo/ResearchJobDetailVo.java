package cyou.yuanbaomao.sellersprite.research.model.vo;

import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研任务详情")
public class ResearchJobDetailVo {

    private String jobId;
    private String reportName;
    private String marketplace;
    private String nodeIdPath;
    private String month;
    private String keyword;
    private List<String> seedAsins;
    private CollectionGraphConfig collectionConfig;
    private String dataSourceMode;
    private String workflowVersion;
    private String status;
    private String currentNode;
    private String currentNodeName;
    private String currentStage;
    private String waitingInputType;
    private Integer progress;
    private Integer attemptCount;
    private Integer maxAttempts;
    private Integer remainingAttempts;
    private Long nextRunAt;
    private Long leaseUntil;
    private Long heartbeatAt;
    private Long cancelRequestedAt;
    private Boolean cancellable;
    private Boolean retryable;
    private String errorCode;
    private String errorMessage;
    private Long startedAt;
    private Long finishedAt;
    private Long createdAt;
    private String conversationId;
    private String analysisRunId;
    private String analysisStatus;
    private String analysisPhase;
    private Integer analysisProgress;
    private String analysisGoal;
    private List<ResearchArtifactVo> artifacts;
}
