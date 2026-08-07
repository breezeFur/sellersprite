package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import lombok.Getter;

/**
 * Curation Agent 的显式失败，调用方应据此把分析运行标记为失败，而不是生成降级成功报告。
 */
@Getter
public class AmazonSelectionAnalysisException extends RuntimeException {

    private final ErrorCode errorCode;

    public AmazonSelectionAnalysisException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AmazonSelectionAnalysisException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        MODEL_UNAVAILABLE,
        MODEL_INVOCATION_FAILED,
        MODEL_EMPTY_RESPONSE,
        AGENT_EXECUTION_FAILED,
        SHEET_LIMIT_EXCEEDED,
        MODEL_CALL_LIMIT_EXCEEDED,
        EXECUTION_DURATION_EXCEEDED
    }
}
