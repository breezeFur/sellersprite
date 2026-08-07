package cyou.yuanbaomao.sellersprite.research.support;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 调研业务月份与 SellerSprite 月份参数转换工具。
 */
public final class ResearchMonthUtils {

    private static final DateTimeFormatter BUSINESS_MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM");
    private static final DateTimeFormatter SELLERSPRITE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuuMM");

    private ResearchMonthUtils() {
    }

    public static String normalize(String value) {
        return parse(value).format(BUSINESS_MONTH_FORMATTER);
    }

    public static String toSellerSpriteMonth(String value) {
        return parse(value).format(SELLERSPRITE_MONTH_FORMATTER);
    }

    private static YearMonth parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("调研月份不能为空");
        }
        try {
            return YearMonth.parse(value.trim(), BUSINESS_MONTH_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("调研月份必须为yyyy-MM格式: " + value, exception);
        }
    }
}
