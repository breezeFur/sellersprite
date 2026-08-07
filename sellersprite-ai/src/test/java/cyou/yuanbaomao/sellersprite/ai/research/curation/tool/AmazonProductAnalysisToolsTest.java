package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheetRow;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.RawSheet;
import java.util.List;
import org.junit.jupiter.api.Test;

class AmazonProductAnalysisToolsTest {

    @Test
    void shouldExposeOnlyBusinessFieldsFromCleanEvidence() {
        AmazonSelectionToolContext toolContext = new AmazonSelectionToolContext();
        AmazonProductAnalysisTools tools = new AmazonProductAnalysisTools(toolContext);
        ProductWorkbook workbook = workbookWithProvenanceFields();
        toolContext.start("analysis-run-1", workbook);

        String observation = tools.inspectAmazonSheet("analysis-run-1", "商品集中度");

        assertThat(observation)
                .contains("ASIN=B000TEST")
                .contains("占比=32%")
                .doesNotContain("证据范围", "来源数据集", "数据局限");
    }

    private ProductWorkbook workbookWithProvenanceFields() {
        ProductWorkbook workbook = new ProductWorkbook();
        ProductSheet sheet = new ProductSheet();
        sheet.setSheetName("商品集中度");
        sheet.setHeaders(List.of("ASIN", "占比"));
        ProductSheetRow row = new ProductSheetRow();
        row.setRowIndex(1);
        row.getCells().put("ASIN", "B000TEST");
        row.getCells().put("占比", "32%");
        sheet.getRows().add(row);
        RawSheet rawSheet = new RawSheet();
        rawSheet.setSheetName("商品集中度");
        rawSheet.setRawMarkdown("Sheet: 商品集中度\nRow 1: ASIN\nRow 2: B000TEST");
        sheet.setRawSheet(rawSheet);
        workbook.getSheets().add(sheet);
        workbook.getRawSheets().add(rawSheet);
        return workbook;
    }
}
