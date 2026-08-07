package cyou.yuanbaomao.sellersprite.research.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResearchAnalysisRunVo {

    private String analysisRunId;
    private String jobId;
    private String conversationId;
    private String parentRunId;
    private String runType;
    private String stageCode;
    private String analysisGoal;
    private String status;
    private String currentPhase;
    private Integer progress;
    private Integer attemptCount;
    private Integer maxAttempts;
    private Long nextRunAt;
    private Long leaseUntil;
    private Long heartbeatAt;
    private Long cancelRequestedAt;
    private Integer modelCallCount;
    private Integer eventCount;
    private String finalSummary;
    private String errorCode;
    private String errorMessage;
    private Long startedAt;
    private Long finishedAt;
    private Long createdAt;
    private Boolean cancellable;
    private Boolean retryable;
}
