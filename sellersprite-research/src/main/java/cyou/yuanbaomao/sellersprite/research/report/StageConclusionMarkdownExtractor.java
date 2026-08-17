package cyou.yuanbaomao.sellersprite.research.report;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** 从阶段分析 Markdown 中提取评分速览结论表。 */
@Component
public class StageConclusionMarkdownExtractor {

    private static final Pattern HEADING_PATTERN =
            Pattern.compile("^ {0,3}(#{1,2})\\s+(.+?)\\s*#*\\s*$");
    private static final Pattern FENCE_PATTERN = Pattern.compile("^ {0,3}(`{3,}|~{3,}).*$");
    private static final String SCORECARD_DISCLAIMER_PREFIX = "> 评分仅代表";

    public String extract(String markdown, String expectedHeading) {
        if (markdown == null || markdown.isBlank()) {
            throw new IllegalArgumentException("阶段结论 Markdown 不能为空");
        }
        if (expectedHeading == null || expectedHeading.isBlank()) {
            throw new IllegalArgumentException("阶段结论标题不能为空");
        }
        String normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        int start = findHeading(lines, expectedHeading.trim());
        if (start < 0) {
            return normalized.strip();
        }
        int end = findSectionEnd(lines, start + 1);
        return String.join("\n", Arrays.copyOfRange(lines, start, end)).strip();
    }

    private int findHeading(String[] lines, String expectedHeading) {
        boolean fenced = false;
        for (int index = 0; index < lines.length; index++) {
            if (FENCE_PATTERN.matcher(lines[index]).matches()) {
                fenced = !fenced;
                continue;
            }
            if (fenced) {
                continue;
            }
            Matcher heading = HEADING_PATTERN.matcher(lines[index]);
            if (heading.matches()
                    && "##".equals(heading.group(1))
                    && expectedHeading.equals(heading.group(2).trim())) {
                return index;
            }
        }
        return -1;
    }

    private int findSectionEnd(String[] lines, int fromIndex) {
        boolean fenced = false;
        for (int index = fromIndex; index < lines.length; index++) {
            if (FENCE_PATTERN.matcher(lines[index]).matches()) {
                fenced = !fenced;
                continue;
            }
            if (fenced) {
                continue;
            }
            if (lines[index].stripLeading().startsWith(SCORECARD_DISCLAIMER_PREFIX)) {
                return index + 1;
            }
            if (HEADING_PATTERN.matcher(lines[index]).matches()) {
                return index;
            }
        }
        return lines.length;
    }
}
