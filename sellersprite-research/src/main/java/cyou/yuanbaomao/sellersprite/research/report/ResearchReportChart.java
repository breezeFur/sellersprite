package cyou.yuanbaomao.sellersprite.research.report;

import java.math.BigDecimal;
import java.util.List;

/** 可通过 SSE 回放并复用于 PDF 的确定性图表规格。 */
public record ResearchReportChart(
        String chartCode,
        String sectionCode,
        String sectionTitle,
        String type,
        String title,
        List<String> categories,
        List<Series> series,
        String unit,
        String methodology) {

    static final String TYPE_BAR = "BAR";
    static final String TYPE_HORIZONTAL_BAR = "HORIZONTAL_BAR";

    public ResearchReportChart {
        categories = List.copyOf(categories);
        series = List.copyOf(series);
    }

    /** 将确定性图表规格转换为可直接通过 SSE 展示的 Mermaid Markdown。 */
    public String mermaidMarkdown() {
        if (series.isEmpty() || series.getFirst().values().isEmpty()) {
            throw new IllegalStateException("图表缺少可渲染序列: " + chartCode);
        }
        boolean horizontalBar = TYPE_HORIZONTAL_BAR.equalsIgnoreCase(type);
        String command = TYPE_BAR.equalsIgnoreCase(type) || horizontalBar ? "bar" : "line";
        String categoryValues = categories.stream()
                .map(this::quotedMermaidText)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        String seriesValues = series.getFirst().values().stream()
                .map(BigDecimal::stripTrailingZeros)
                .map(BigDecimal::toPlainString)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        BigDecimal maximum = series.getFirst().values().stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE)
                .max(BigDecimal.ONE)
                .stripTrailingZeros();
        return """
                ```mermaid
                xychart-beta%s
                    title %s
                    x-axis [%s]
                    y-axis %s 0 --> %s
                    %s [%s]
                ```
                """.formatted(
                horizontalBar ? " horizontal" : "",
                quotedMermaidText(title),
                categoryValues,
                quotedMermaidText(unit == null ? "" : unit),
                maximum.toPlainString(),
                command,
                seriesValues);
    }

    private String quotedMermaidText(String value) {
        String safeValue = value == null ? "" : value
                .replace('"', '’')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
        return "\"" + safeValue + "\"";
    }

    public record Series(String name, List<BigDecimal> values) {

        public Series {
            values = List.copyOf(values);
        }
    }
}
