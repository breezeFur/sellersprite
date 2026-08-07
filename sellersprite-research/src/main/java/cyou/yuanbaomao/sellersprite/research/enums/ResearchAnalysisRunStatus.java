package cyou.yuanbaomao.sellersprite.research.enums;

/**
 * 市场调研AI分析运行状态。
 */
public enum ResearchAnalysisRunStatus {

    WAITING_RESEARCH,
    QUEUED,
    RUNNING,
    RETRY_WAIT,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
