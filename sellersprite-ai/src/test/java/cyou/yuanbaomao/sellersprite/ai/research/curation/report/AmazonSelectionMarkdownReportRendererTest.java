package cyou.yuanbaomao.sellersprite.ai.research.curation.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.SheetAnalysisResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class AmazonSelectionMarkdownReportRendererTest {

    private final AmazonSelectionMarkdownReportRenderer renderer = new AmazonSelectionMarkdownReportRenderer();

    @Test
    void shouldRenderOnlyModelSummaryWithoutRepeatedRunMetadata() {
        AmazonSelectionReactResult result = successfulResult();

        String markdown = renderer.render(result);

        assertThat(markdown)
                .contains("这是模型根据本次证据生成的结论")
                .doesNotContain("conversation-1")
                .doesNotContain("market-research-job-1.xlsx")
                .doesNotContain("Sheet 分析状态")
                .doesNotContain("睫毛增长液")
                .doesNotContain("18.17%")
                .doesNotContain("## 模型分析报告");
        assertThat(markdown).startsWith("# 亚马逊市场调研分析报告");
    }

    @Test
    void shouldRequireAllTwelveOrderedSectionsForFinalReport() {
        AmazonSelectionReactResult result = successfulResult();
        result.setFinalSummary(finalSummary());

        assertThat(renderer.renderFinal(result))
                .contains("## 1. US", "## 10. Keywords", "## 12. ASIN运营趋势")
                .doesNotContain("## Sheet 分析状态");

        result.setFinalSummary(finalSummary().replace(
                "- 保留结论。", "- 样本月销量为 1200。"));
        assertThat(renderer.renderFinal(result))
                .contains("样本月销量为 1200。")
                .contains("**章节评分：70/100");

        result.setFinalSummary(finalSummary().replace("## 10. Keywords", "## 10. 关键词"));
        assertThatThrownBy(() -> renderer.renderFinal(result))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("最终报告缺少或乱序章节");

        result.setFinalSummary(finalSummary().replaceFirst("\\*\\*章节评分：", "**判断评分："));
        assertThatThrownBy(() -> renderer.renderFinal(result))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("最终报告章节缺少评分");
    }

    @Test
    void shouldNormalizeScreeningHeadingsToTableNamesAndRejectRepeatedData() {
        AmazonSelectionReactResult result = successfulResult();
        result.setFinalSummary("""
                ## 阶段一初筛评分速览

                **综合评分：75/100｜推进建议：有条件推进｜置信度：中**

                ## Sheet 一
                - 市场存在进入机会。
                ## Sheet 二
                - 销量趋势保持稳定。
                ## Sheet 三
                - 需求变化需要持续观察。
                ## Sheet 四
                - 细分方向存在差异化空间。
                ## Sheet 五
                - 退货风险需要通过产品设计控制。
                ## Sheet 六
                - 品牌竞争需要差异化定位。
                ## Sheet 七
                - 商品集中度仍需谨慎评估。
                """);

        assertThat(renderer.renderScreeningSummary(result))
                .contains("## US", "## 行业销售趋势", "## 行业需求及趋势", "## 商品集中度")
                .doesNotContainIgnoringCase("sheet");

        result.setFinalSummary(result.getFinalSummary().replace(
                "- 市场存在进入机会。", "- 样本月销量为 1200。"));
        assertThat(renderer.renderScreeningSummary(result))
                .contains("样本月销量为 1200。");
    }

    private String finalSummary() {
        String[] titles = {
            "US", "行业销售趋势", "行业需求及趋势", "细分市场现状", "细分市场退货率", "竞品品牌",
            "商品集中度", "评价", "VOC", "Keywords", "ASIN销售趋势", "ASIN运营趋势"
        };
        StringBuilder markdown = new StringBuilder("## 最终决策评分速览\n\n综合评分：70/100\n\n");
        for (int index = 0; index < titles.length; index++) {
            markdown.append("## ").append(index + 1).append(". ").append(titles[index])
                    .append("\n\n**章节评分：70/100｜判断：可验证｜置信度：中**\n\n")
                    .append("### 核心结论\n\n- 保留结论。\n\n")
                    .append("### 主要风险\n\n- 仍有风险。\n\n")
                    .append("### 决策建议\n\n- 继续验证。\n\n");
        }
        return markdown.toString();
    }

    @Test
    void shouldRejectReportWhenModelDidNotProduceFinalSummary() {
        AmazonSelectionReactResult result = successfulResult();
        result.setModelInvoked(false);

        assertThatThrownBy(() -> renderer.render(result))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("模型未成功生成最终摘要");
    }

    private AmazonSelectionReactResult successfulResult() {
        ProductWorkbook workbook = new ProductWorkbook();
        workbook.setFileName("market-research-job-1.xlsx");
        ProductSheet sheet = new ProductSheet();
        sheet.setSheetName("US");
        workbook.getSheets().add(sheet);

        AmazonSelectionReactResult result = new AmazonSelectionReactResult();
        result.setConversationId("conversation-1");
        result.setWorkbook(workbook);
        result.setModelInvoked(true);
        result.setFinalSummary("""
                ## 最终决策评分速览

                **综合评分：75/100｜推进建议：有条件推进｜置信度：中**

                这是模型根据本次证据生成的结论。
                """);
        result.setSheetAnalyses(List.of(SheetAnalysisResult.builder()
                .sheetName("US")
                .rowCount(1)
                .rawCellCount(2)
                .imageAssetCount(0)
                .build()));
        return result;
    }
}
