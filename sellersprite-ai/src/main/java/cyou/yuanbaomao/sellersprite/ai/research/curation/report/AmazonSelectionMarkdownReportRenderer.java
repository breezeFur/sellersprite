package cyou.yuanbaomao.sellersprite.ai.research.curation.report;

import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.SheetAnalysisResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 只渲染模型实际产出的通用 Markdown，不在 Java 中注入任何品类事实或业务结论。
 */
@Component
public class AmazonSelectionMarkdownReportRenderer {

    public String render(AmazonSelectionReactResult result) {
        if (result == null || !result.isModelInvoked() || !StringUtils.hasText(result.getFinalSummary())) {
            throw new IllegalStateException("模型未成功生成最终摘要，不能创建分析报告");
        }
        StringBuilder report = new StringBuilder("# 亚马逊市场调研分析报告\n\n");
        report.append(result.getFinalSummary().trim())
                .append("\n\n---\n\n")
                .append("## 分析信息\n\n")
                .append("- 会话 ID：").append(defaultText(result.getConversationId())).append('\n');
        if (result.getWorkbook() != null) {
            report.append("- 工作簿：").append(defaultText(result.getWorkbook().getFileName())).append('\n')
                    .append("- Sheet 数：").append(result.getWorkbook().getSheets().size()).append('\n');
        }
        report.append("\n## Sheet 分析状态\n\n");
        for (SheetAnalysisResult sheet : result.getSheetAnalyses()) {
            report.append("- ").append(defaultText(sheet.getSheetName()))
                    .append("：结构化行 ").append(defaultNumber(sheet.getRowCount()))
                    .append("，原始非空单元格 ").append(defaultNumber(sheet.getRawCellCount()))
                    .append("，图片资产 ").append(defaultNumber(sheet.getImageAssetCount()))
                    .append('\n');
        }
        return report.toString();
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
