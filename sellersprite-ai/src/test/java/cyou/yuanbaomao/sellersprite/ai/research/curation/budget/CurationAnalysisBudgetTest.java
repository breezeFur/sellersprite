package cyou.yuanbaomao.sellersprite.ai.research.curation.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CurationAnalysisBudgetTest {

    @Test
    void shouldProvideDefaultsWithUnlimitedExecutionDuration() {
        CurationAnalysisProperties properties = new CurationAnalysisProperties();

        assertThat(properties.getMaxSheets()).isEqualTo(12);
        assertThat(properties.getMaxModelCalls()).isGreaterThanOrEqualTo(12);
        assertThat(properties.getMaxExecutionDurationMs()).isZero();
    }

    @Test
    void shouldRejectSheetCountAboveConfiguredLimit() {
        CurationAnalysisBudget budget = new CurationAnalysisBudget(10, 16, 60_000L, () -> {
        });

        budget.assertSheetCount(10);

        assertThatThrownBy(() -> budget.assertSheetCount(11))
                .isInstanceOfSatisfying(AmazonSelectionAnalysisException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AmazonSelectionAnalysisException.ErrorCode.SHEET_LIMIT_EXCEEDED));
    }

    @Test
    void shouldPersistEachAllowedModelCallBeforeRejectingNextCall() {
        AtomicInteger persistedCalls = new AtomicInteger();
        CurationAnalysisBudget budget = new CurationAnalysisBudget(10, 2, 60_000L,
                persistedCalls::incrementAndGet);

        budget.beforeModelCall();
        budget.beforeModelCall();

        assertThatThrownBy(budget::beforeModelCall)
                .isInstanceOfSatisfying(AmazonSelectionAnalysisException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AmazonSelectionAnalysisException.ErrorCode.MODEL_CALL_LIMIT_EXCEEDED));
        assertThat(persistedCalls).hasValue(2);
        assertThat(budget.modelCallCount()).isEqualTo(2);
    }

    @Test
    void shouldCheckDurationAtModelAndEventBoundaries() {
        AtomicLong nanoTime = new AtomicLong();
        CurationAnalysisBudget budget = new CurationAnalysisBudget(
                10, 16, 1_000L, () -> {
                }, nanoTime::get);

        nanoTime.set(TimeUnit.MILLISECONDS.toNanos(999L));
        budget.beforeEventPersistence();
        nanoTime.set(TimeUnit.MILLISECONDS.toNanos(1_000L));

        assertThatThrownBy(budget::beforeModelCall)
                .isInstanceOfSatisfying(AmazonSelectionAnalysisException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AmazonSelectionAnalysisException.ErrorCode.EXECUTION_DURATION_EXCEEDED));
    }

    @Test
    void shouldDisableDurationChecksWhenConfiguredAsZero() {
        AtomicLong nanoTime = new AtomicLong();
        AtomicInteger persistedCalls = new AtomicInteger();
        CurationAnalysisBudget budget = new CurationAnalysisBudget(
                10, 16, 0L, persistedCalls::incrementAndGet, nanoTime::get);

        nanoTime.set(Long.MAX_VALUE);

        budget.beforeEventPersistence();
        budget.beforeModelCall();
        budget.afterModelCall();

        assertThat(persistedCalls).hasValue(1);
    }

    @Test
    void shouldRejectNegativeExecutionDuration() {
        assertThatThrownBy(() -> new CurationAnalysisBudget(10, 16, -1L, () -> {
        }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxExecutionDurationMs")
                .hasMessageContaining("不得小于 0");
    }

    @Test
    void shouldReturnIndependentUnlimitedBudgets() {
        CurationAnalysisBudget first = CurationAnalysisBudget.unlimited();
        CurationAnalysisBudget second = CurationAnalysisBudget.unlimited();

        first.beforeModelCall();

        assertThat(first).isNotSameAs(second);
        assertThat(first.modelCallCount()).isEqualTo(1);
        assertThat(second.modelCallCount()).isZero();
    }
}
