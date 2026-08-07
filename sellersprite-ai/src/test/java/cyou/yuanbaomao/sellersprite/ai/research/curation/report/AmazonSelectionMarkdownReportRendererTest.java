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
    void shouldRenderModelSummaryBeforeNeutralRunMetadata() {
        AmazonSelectionReactResult result = successfulResult();

        String markdown = renderer.render(result);

        assertThat(markdown)
                .contains("conversation-1")
                .contains("market-research-job-1.xlsx")
                .contains("这是模型根据本次证据生成的结论")
                .contains("US：结构化行 1")
                .doesNotContain("睫毛增长液")
                .doesNotContain("18.17%")
                .doesNotContain("## 模型分析报告");
        assertThat(markdown.indexOf("## 最终决策评分速览"))
                .isLessThan(markdown.indexOf("## 分析信息"));
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
