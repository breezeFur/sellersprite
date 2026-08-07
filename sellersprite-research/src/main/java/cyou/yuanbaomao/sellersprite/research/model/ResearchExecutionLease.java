package cyou.yuanbaomao.sellersprite.research.model;

/** Graph Dispatcher 原子抢占成功后的轻量执行凭据。 */
public record ResearchExecutionLease(
        String jobId,
        String workflowVersion,
        String executionOwner,
        String executionToken,
        int attemptCount) {
}
