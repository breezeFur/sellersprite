package cyou.yuanbaomao.sellersprite.framework.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    public static final String REDACTED_VALUE = "[REDACTED]";

    private static final String SENSITIVE_KEY_PATTERN = "password|passwd|pwd|authorization|access[_-]?token|"
            + "refresh[_-]?token|token|cookie|api[_-]?key|apikey|secret|client[_-]?secret";
    private static final Pattern JSON_VALUE_PATTERN = Pattern.compile(
            "(?i)([\\\"'](?:" + SENSITIVE_KEY_PATTERN + ")[\\\"']\\s*:\\s*[\\\"'])([^\\\"']*)([\\\"'])");
    private static final Pattern QUERY_VALUE_PATTERN = Pattern.compile(
            "(?i)((?:^|[?&;,\\s])(?:" + SENSITIVE_KEY_PATTERN + ")\\s*=\\s*)([^&;,\\s]+)");
    private static final Pattern HEADER_VALUE_PATTERN = Pattern.compile(
            "(?im)^(\\s*(?:" + SENSITIVE_KEY_PATTERN + ")\\s*:\\s*)(.+)$");

    private SensitiveDataMasker() {
    }

    public static String mask(String source) {
        if (source == null || source.isBlank()) {
            return "";
        }
        String masked = replaceValue(JSON_VALUE_PATTERN, source, 1, 3);
        masked = replaceValue(QUERY_VALUE_PATTERN, masked, 1, -1);
        return replaceValue(HEADER_VALUE_PATTERN, masked, 1, -1);
    }

    public static MaskedText maskAndTruncate(String source, int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength 必须大于 0");
        }
        String masked = mask(source);
        if (masked.length() <= maxLength) {
            return new MaskedText(masked, false);
        }
        return new MaskedText(masked.substring(0, maxLength), true);
    }

    private static String replaceValue(Pattern pattern, String source, int prefixGroup, int suffixGroup) {
        Matcher matcher = pattern.matcher(source);
        StringBuilder result = new StringBuilder(source.length());
        while (matcher.find()) {
            String suffix = suffixGroup > 0 ? matcher.group(suffixGroup) : "";
            matcher.appendReplacement(result,
                    Matcher.quoteReplacement(matcher.group(prefixGroup) + REDACTED_VALUE + suffix));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public record MaskedText(String content, boolean truncated) {
    }
}
