package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModelInputCompactorTest {

    private ContextWindowEstimator contextWindowEstimator;
    private CurationAnalysisProperties analysisProperties;
    private ModelInputCompactor compactor;

    @BeforeEach
    void setUp() {
        contextWindowEstimator = new ContextWindowEstimator();
        analysisProperties = new CurationAnalysisProperties();
        compactor = new ModelInputCompactor(contextWindowEstimator, analysisProperties);
    }

    @Test
    void shouldKeepModelInputWithinBudgetAndPreserveBothEnds() {
        analysisProperties.setMaxModelInputTokens(160);
        String modelInput = "HEAD-" + "中".repeat(300) + "-TAIL";

        String compacted = compactor.compact(modelInput);

        assertThat(compacted)
                .startsWith("HEAD-")
                .contains("【输入限制】模型输入过长")
                .endsWith("-TAIL");
        assertThat(contextWindowEstimator.estimateTextTokens(compacted)).isLessThanOrEqualTo(160);
    }

    @Test
    void shouldNotSplitSurrogatePairsOrExceedBudget() {
        analysisProperties.setMaxModelInputTokens(160);
        String modelInput = "头".repeat(260) + "🙂".repeat(80);

        String compacted = compactor.compact(modelInput);

        assertThat(contextWindowEstimator.estimateTextTokens(compacted)).isLessThanOrEqualTo(160);
        assertThat(hasUnpairedSurrogate(compacted)).isFalse();
    }

    @Test
    void shouldReturnOriginalModelInputWhenItFitsBudget() {
        String modelInput = "small model input";

        assertThat(compactor.compact(modelInput)).isSameAs(modelInput);
    }

    @Test
    void shouldUseSafeDefaultModelInputBudget() {
        assertThat(analysisProperties.getMaxModelInputTokens()).isEqualTo(12_000);
    }

    @Test
    void shouldRejectInvalidTokenBudget() {
        analysisProperties.setMaxModelInputTokens(0);

        assertThatThrownBy(() -> compactor.compact("model input"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxModelInputTokens 必须大于 0");
    }

    @Test
    void shouldRejectTokenBudgetWithoutContextHeadroom() {
        analysisProperties.setMaxModelInputTokens(102_400);

        assertThatThrownBy(() -> compactor.compact("model input"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxModelInputTokens 必须小于上下文压缩触发阈值 102400");
    }

    private boolean hasUnpairedSurrogate(String value) {
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.isHighSurrogate(current)) {
                if (index + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(index + 1))) {
                    return true;
                }
                index++;
            } else if (Character.isLowSurrogate(current)) {
                return true;
            }
        }
        return false;
    }
}
