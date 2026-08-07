package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 为单次市场调研模型请求保留确定性的上下文余量。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelInputCompactor {

    private static final double HEAD_TOKEN_BUDGET_RATIO = 0.75D;
    private static final String COMPRESSION_MARKER_TEMPLATE =
            "\n\n【输入限制】模型输入过长，已按 %d 个估算 tokens 的原文压缩并保留首尾数据；"
                    + "未保留的中间内容不得推断，相关结论需要补充验证。\n\n";

    private final ContextWindowEstimator contextWindowEstimator;
    private final CurationAnalysisProperties analysisProperties;

    public String compact(String modelInput) {
        if (!StringUtils.hasText(modelInput)) {
            return modelInput;
        }
        int maxTokens = requirePositiveMaxTokens();
        int estimatedTokens = contextWindowEstimator.estimateTextTokens(modelInput);
        if (estimatedTokens <= maxTokens) {
            return modelInput;
        }

        String marker = COMPRESSION_MARKER_TEMPLATE.formatted(estimatedTokens);
        int contentTokenBudget = maxTokens - contextWindowEstimator.estimateTextTokens(marker);
        if (contentTokenBudget <= 0) {
            return takePrefix(modelInput, maxTokens);
        }
        int headTokenBudget = Math.max(1, (int) Math.floor(contentTokenBudget * HEAD_TOKEN_BUDGET_RATIO));
        int tailTokenBudget = contentTokenBudget - headTokenBudget;
        String head = takePrefix(modelInput, headTokenBudget);
        String remaining = modelInput.substring(head.length());
        String tail = takeSuffix(remaining, tailTokenBudget);
        String compacted = head + marker + tail;
        log.info("市场调研模型输入已压缩，originalTokens={}, compactedTokens={}, maxTokens={}",
                estimatedTokens, contextWindowEstimator.estimateTextTokens(compacted), maxTokens);
        return compacted;
    }

    private int requirePositiveMaxTokens() {
        int maxTokens = analysisProperties.getMaxModelInputTokens();
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxModelInputTokens 必须大于 0");
        }
        int contextThresholdTokens = (int) Math.floor(
                analysisProperties.getContextCompression().getMaxContextTokens()
                        * analysisProperties.getContextCompression().getTriggerRatio());
        if (maxTokens >= contextThresholdTokens) {
            throw new IllegalArgumentException("maxModelInputTokens 必须小于上下文压缩触发阈值 " + contextThresholdTokens);
        }
        return maxTokens;
    }

    private String takePrefix(String text, int tokenBudget) {
        if (tokenBudget <= 0 || text.isEmpty()) {
            return "";
        }
        int low = 0;
        int high = text.length();
        while (low < high) {
            int middle = low + (high - low + 1) / 2;
            if (contextWindowEstimator.estimateTextTokens(text.substring(0, middle)) <= tokenBudget) {
                low = middle;
            } else {
                high = middle - 1;
            }
        }
        return text.substring(0, adjustPrefixBoundary(text, low));
    }

    private String takeSuffix(String text, int tokenBudget) {
        if (tokenBudget <= 0 || text.isEmpty()) {
            return "";
        }
        int low = 0;
        int high = text.length();
        while (low < high) {
            int middle = low + (high - low + 1) / 2;
            int startIndex = text.length() - middle;
            if (contextWindowEstimator.estimateTextTokens(text.substring(startIndex)) <= tokenBudget) {
                low = middle;
            } else {
                high = middle - 1;
            }
        }
        return text.substring(adjustSuffixBoundary(text, text.length() - low));
    }

    private int adjustPrefixBoundary(String text, int index) {
        if (splitsSurrogatePair(text, index)) {
            return index - 1;
        }
        return index;
    }

    private int adjustSuffixBoundary(String text, int index) {
        if (splitsSurrogatePair(text, index)) {
            return index + 1;
        }
        return index;
    }

    private boolean splitsSurrogatePair(String text, int index) {
        return index > 0 && index < text.length()
                && Character.isHighSurrogate(text.charAt(index - 1))
                && Character.isLowSurrogate(text.charAt(index));
    }
}
