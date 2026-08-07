package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import java.util.Collection;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ContextWindowEstimator {

    private static final int MESSAGE_OVERHEAD_TOKENS = 4;
    private static final int ASCII_CHARS_PER_TOKEN = 4;

    public ContextWindowSnapshot snapshot(Collection<Message> memoryMessages, Collection<Message> promptMessages,
            int maxContextTokens, double triggerRatio) {
        int estimatedTokens = estimateTokens(memoryMessages) + estimateTokens(promptMessages);
        int thresholdTokens = Math.max(1, (int) Math.floor(maxContextTokens * triggerRatio));
        return new ContextWindowSnapshot(estimatedTokens, maxContextTokens, thresholdTokens, triggerRatio,
                estimatedTokens >= thresholdTokens);
    }

    public int estimateTokens(Collection<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        return messages.stream()
                .map(ContextMessageText::extract)
                .mapToInt(this::estimateTextTokensWithMessageOverhead)
                .sum();
    }

    int estimateTextTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        int asciiChars = 0;
        int nonAsciiChars = 0;
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            if (Character.isWhitespace(value)) {
                continue;
            }
            if (value <= 0x7F) {
                asciiChars++;
            } else {
                nonAsciiChars++;
            }
        }
        return nonAsciiChars + (int) Math.ceil((double) asciiChars / ASCII_CHARS_PER_TOKEN);
    }

    private int estimateTextTokensWithMessageOverhead(String text) {
        int contentTokens = estimateTextTokens(text);
        return contentTokens == 0 ? 0 : contentTokens + MESSAGE_OVERHEAD_TOKENS;
    }
}
