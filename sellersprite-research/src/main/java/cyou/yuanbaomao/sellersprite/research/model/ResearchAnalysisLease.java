package cyou.yuanbaomao.sellersprite.research.model;

/** 分析 Dispatcher 原子领取成功后的 fencing 凭据。 */
public record ResearchAnalysisLease(
        String analysisRunId,
        String jobId,
        String userId,
        String conversationId,
        String runType,
        String analysisGoal,
        String executionOwner,
        String executionToken,
        int attemptCount,
        int maxAttempts) {
}
