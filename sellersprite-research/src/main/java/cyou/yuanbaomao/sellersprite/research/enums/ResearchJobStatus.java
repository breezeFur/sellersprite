package cyou.yuanbaomao.sellersprite.research.enums;

/**
 * 市场调研任务对外状态。
 */
public enum ResearchJobStatus {

    QUEUED,
    RUNNING,
    WAITING_INPUT,
    RETRY_WAIT,
    SUCCEEDED,
    ABANDONED,
    FAILED,
    CANCELLED
}
