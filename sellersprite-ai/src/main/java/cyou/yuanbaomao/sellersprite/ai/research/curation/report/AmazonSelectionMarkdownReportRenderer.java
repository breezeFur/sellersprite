package cyou.yuanbaomao.sellersprite.ai.research.curation.report;

import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.research.report.ResearchFinalReportSection;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 只渲染模型实际产出的通用 Markdown，不在 Java 中注入任何品类事实或业务结论。
 */
@Component
public class AmazonSelectionMarkdownReportRenderer {

    private static final Pattern DATA_TABLE_SEPARATOR = Pattern.compile(
            "(?m)^\\s*\\|?\\s*:?-{3,}.*\\|.*$");
    private static final Pattern MARKDOWN_HEADING = Pattern.compile("^\\s*#{2,6}\\s+.+$");
    private static final Pattern SHEET_TERM = Pattern.compile("(?i)sheet");
    private static final List<String> SCREENING_SECTION_TITLES = List.of(
            "US",
            "行业销售趋势",
            "行业需求及趋势",
            "细分市场现状",
            "细分市场退货率",
            "竞品品牌",
            "商品集中度");

    public String render(AmazonSelectionReactResult result) {
        if (result == null || !result.isModelInvoked() || !StringUtils.hasText(result.getFinalSummary())) {
            throw new IllegalStateException("模型未成功生成最终摘要，不能创建分析报告");
        }
        return "# 亚马逊市场调研分析报告\n\n" + result.getFinalSummary().trim() + "\n";
    }

    public String renderFinal(AmazonSelectionReactResult result) {
        String report = render(result);
        int previousIndex = -1;
        for (ResearchFinalReportSection section : ResearchFinalReportSection.SECTIONS) {
            String heading = "## " + section.order() + ". " + section.title();
            int headingIndex = report.indexOf(heading);
            if (headingIndex < 0 || headingIndex <= previousIndex) {
                throw new IllegalStateException("最终报告缺少或乱序章节: " + heading);
            }
            int nextHeadingIndex = section.order() == ResearchFinalReportSection.SECTIONS.size()
                    ? report.length()
                    : report.indexOf("## " + (section.order() + 1) + ". ", headingIndex + heading.length());
            String chapter = report.substring(headingIndex, nextHeadingIndex < 0 ? report.length() : nextHeadingIndex);
            if (!chapter.contains("**章节评分：")) {
                throw new IllegalStateException("最终报告章节缺少评分: " + heading);
            }
            previousIndex = headingIndex;
        }
        String chapterBody = report.substring(report.indexOf("## 1. US"));
        if (DATA_TABLE_SEPARATOR.matcher(chapterBody).find()) {
            throw new IllegalStateException("最终报告十二章不得重复输出 Markdown 数据表");
        }
        return report;
    }

    /** 阶段一终态报告只暴露真实表名，屏蔽工作簿内部 Sheet 术语。 */
    public String renderScreeningSummary(AmazonSelectionReactResult result) {
        if (result == null || !result.isModelInvoked() || !StringUtils.hasText(result.getFinalSummary())) {
            throw new IllegalStateException("模型未成功生成阶段一摘要，不能创建阶段一报告");
        }
        String normalized = normalizeScreeningHeadings(result.getFinalSummary().trim());
        int previousIndex = -1;
        for (String title : SCREENING_SECTION_TITLES) {
            String heading = "## " + title;
            int headingIndex = normalized.indexOf(heading);
            if (headingIndex < 0 || headingIndex <= previousIndex) {
                throw new IllegalStateException("阶段一报告缺少或乱序表名章节: " + heading);
            }
            previousIndex = headingIndex;
        }
        String chapterBody = normalized.substring(normalized.indexOf("## US"));
        if (DATA_TABLE_SEPARATOR.matcher(chapterBody).find()) {
            throw new IllegalStateException("阶段一报告表名章节不得重复输出 Markdown 数据表");
        }
        if (chapterBody.contains("### 当前事实") || chapterBody.contains("### 数据事实")) {
            throw new IllegalStateException("阶段一报告不得重复输出事实数据小节");
        }
        return normalized + "\n";
    }

    private String normalizeScreeningHeadings(String markdown) {
        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        int nextSectionIndex = 0;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (MARKDOWN_HEADING.matcher(line).matches()) {
                String matchedTitle = SCREENING_SECTION_TITLES.stream()
                        .filter(line::contains)
                        .findFirst()
                        .orElse(null);
                if (matchedTitle != null) {
                    lines[index] = "## " + matchedTitle;
                    nextSectionIndex = Math.max(
                            nextSectionIndex, SCREENING_SECTION_TITLES.indexOf(matchedTitle) + 1);
                    continue;
                }
                if (SHEET_TERM.matcher(line).find() && nextSectionIndex < SCREENING_SECTION_TITLES.size()) {
                    lines[index] = "## " + SCREENING_SECTION_TITLES.get(nextSectionIndex++);
                    continue;
                }
            }
            lines[index] = SHEET_TERM.matcher(line).replaceAll("证据表");
        }
        return String.join("\n", lines);
    }
}
