package cyou.yuanbaomao.sellersprite.ai.research.curation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 市场调研 Curation 分析配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = CurationAnalysisProperties.PREFIX)
public class CurationAnalysisProperties {

    public static final String PREFIX = "sellersprite.research.analysis";

    private static final int DEFAULT_MAX_SHEETS = 12;
    private static final int DEFAULT_MAX_MODEL_CALLS = 16;
    private static final int DEFAULT_MAX_MODEL_INPUT_TOKENS = 12_000;
    private static final long DEFAULT_MAX_EXECUTION_DURATION_MS = 0L;

    /** 是否启用市场调研后的模型分析。 */
    private boolean enabled = false;

    /** 是否启用数据库轮询型分析 Dispatcher。 */
    private boolean dispatcherEnabled = true;

    /** Dispatcher 数据库轮询间隔，单位毫秒。 */
    private long pollIntervalMs = 2_000L;

    /** 单次轮询最多领取的分析运行数。 */
    private int dispatchBatchSize = 10;

    /** 分析执行租约时长，单位毫秒。 */
    private long leaseDurationMs = 120_000L;

    /** 分析执行心跳间隔，单位毫秒。 */
    private long heartbeatIntervalMs = 15_000L;

    /** 单次分析执行尝试最多允许处理的 Sheet 数。 */
    private int maxSheets = DEFAULT_MAX_SHEETS;

    /** 单次分析执行尝试最多允许发起的模型调用次数，包含上下文压缩调用。 */
    private int maxModelCalls = DEFAULT_MAX_MODEL_CALLS;

    /** 单次模型输入的最大估算 token 数，超出后保留首尾数据并压缩。 */
    private int maxModelInputTokens = DEFAULT_MAX_MODEL_INPUT_TOKENS;

    /** 单次分析执行尝试的协作式最大时长，单位毫秒；0 表示不限制。 */
    private long maxExecutionDurationMs = DEFAULT_MAX_EXECUTION_DURATION_MS;

    /** Agent 历史消息上下文压缩配置。 */
    private ContextCompression contextCompression = new ContextCompression();

    @Data
    public static class ContextCompression {

        private static final int DEFAULT_MAX_CONTEXT_TOKENS = 128_000;
        private static final double DEFAULT_TRIGGER_RATIO = 0.80D;
        private static final int DEFAULT_MIN_MESSAGES_TO_COMPRESS = 6;
        private static final int DEFAULT_RECENT_MESSAGES_TO_KEEP = 6;
        private static final int DEFAULT_MAX_SUMMARY_CHARS = 4_000;

        private boolean enabled = true;

        private int maxContextTokens = DEFAULT_MAX_CONTEXT_TOKENS;

        private double triggerRatio = DEFAULT_TRIGGER_RATIO;

        private int minMessagesToCompress = DEFAULT_MIN_MESSAGES_TO_COMPRESS;

        private int recentMessagesToKeep = DEFAULT_RECENT_MESSAGES_TO_KEEP;

        private int maxSummaryChars = DEFAULT_MAX_SUMMARY_CHARS;
    }
}
