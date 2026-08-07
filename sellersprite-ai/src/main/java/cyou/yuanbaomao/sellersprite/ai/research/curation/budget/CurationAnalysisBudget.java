package cyou.yuanbaomao.sellersprite.ai.research.curation.budget;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/** 单次 Curation 分析执行尝试的 Sheet、模型调用和协作式执行时长预算。 */
public final class CurationAnalysisBudget {

    private static final long UNLIMITED_EXECUTION_DURATION_MS = 0L;
    private static final Runnable NO_OP_RECORDER = () -> {
    };
    private static final LongSupplier SYSTEM_NANO_TIME = System::nanoTime;
    private final int maxSheets;
    private final int maxModelCalls;
    private final long maxExecutionDurationMs;
    private final long maxExecutionDurationNanos;
    private final Runnable modelCallRecorder;
    private final LongSupplier nanoTime;
    private final long startedAtNanos;
    private final AtomicInteger modelCallCount = new AtomicInteger();

    public CurationAnalysisBudget(
            int maxSheets,
            int maxModelCalls,
            long maxExecutionDurationMs,
            Runnable modelCallRecorder) {
        this(maxSheets, maxModelCalls, maxExecutionDurationMs, modelCallRecorder, SYSTEM_NANO_TIME);
    }

    public CurationAnalysisBudget(
            int maxSheets,
            int maxModelCalls,
            long maxExecutionDurationMs,
            Runnable modelCallRecorder,
            LongSupplier nanoTime) {
        this.maxSheets = requirePositive(maxSheets, "maxSheets");
        this.maxModelCalls = requirePositive(maxModelCalls, "maxModelCalls");
        this.maxExecutionDurationMs = requireNonNegative(maxExecutionDurationMs, "maxExecutionDurationMs");
        this.maxExecutionDurationNanos = TimeUnit.MILLISECONDS.toNanos(maxExecutionDurationMs);
        this.modelCallRecorder = Objects.requireNonNull(modelCallRecorder, "modelCallRecorder");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        this.startedAtNanos = nanoTime.getAsLong();
    }

    public static CurationAnalysisBudget unlimited() {
        return new CurationAnalysisBudget(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                UNLIMITED_EXECUTION_DURATION_MS,
                NO_OP_RECORDER,
                SYSTEM_NANO_TIME);
    }

    public void assertSheetCount(int sheetCount) {
        assertExecutionDuration();
        if (sheetCount > maxSheets) {
            throw failure(
                    AmazonSelectionAnalysisException.ErrorCode.SHEET_LIMIT_EXCEEDED,
                    "工作簿 Sheet 数 " + sheetCount + " 超过分析上限 " + maxSheets);
        }
    }

    /** 在模型请求发出前预留并持久化一次真实调用尝试。 */
    public void beforeModelCall() {
        assertExecutionDuration();
        int currentCount;
        do {
            currentCount = modelCallCount.get();
            if (currentCount >= maxModelCalls) {
                throw failure(
                        AmazonSelectionAnalysisException.ErrorCode.MODEL_CALL_LIMIT_EXCEEDED,
                        "模型调用次数已达到分析上限 " + maxModelCalls);
            }
        } while (!modelCallCount.compareAndSet(currentCount, currentCount + 1));

        try {
            modelCallRecorder.run();
        } catch (RuntimeException exception) {
            modelCallCount.decrementAndGet();
            throw exception;
        }
    }

    /** 模型请求返回后再次协作检查；不负责强制中断底层 HTTP 调用。 */
    public void afterModelCall() {
        assertExecutionDuration();
    }

    /** 每个业务过程事件持久化前必须检查总执行时长。 */
    public void beforeEventPersistence() {
        assertExecutionDuration();
    }

    public void assertExecutionDuration() {
        if (isExecutionDurationUnlimited()) {
            return;
        }
        long elapsedNanos = nanoTime.getAsLong() - startedAtNanos;
        if (elapsedNanos >= maxExecutionDurationNanos) {
            throw failure(
                    AmazonSelectionAnalysisException.ErrorCode.EXECUTION_DURATION_EXCEEDED,
                    "分析执行时长已达到上限 " + maxExecutionDurationMs + " ms");
        }
    }

    int modelCallCount() {
        return modelCallCount.get();
    }

    private static int requirePositive(int value, String propertyName) {
        if (value <= 0) {
            throw new IllegalArgumentException(propertyName + " 必须大于 0");
        }
        return value;
    }

    private static long requireNonNegative(long value, String propertyName) {
        if (value < 0L) {
            throw new IllegalArgumentException(propertyName + " 不得小于 0");
        }
        return value;
    }

    private boolean isExecutionDurationUnlimited() {
        return maxExecutionDurationMs == UNLIMITED_EXECUTION_DURATION_MS;
    }

    private AmazonSelectionAnalysisException failure(
            AmazonSelectionAnalysisException.ErrorCode errorCode, String message) {
        return new AmazonSelectionAnalysisException(errorCode, message);
    }
}
