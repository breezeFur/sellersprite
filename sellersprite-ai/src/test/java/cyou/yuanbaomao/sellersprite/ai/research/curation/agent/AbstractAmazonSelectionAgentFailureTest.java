package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheetRow;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.RawSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

class AbstractAmazonSelectionAgentFailureTest {

    private AmazonSelectionToolContext toolContext;
    private AmazonProductAnalysisTools analysisTools;

    @BeforeEach
    void setUp() {
        toolContext = new AmazonSelectionToolContext();
        analysisTools = new AmazonProductAnalysisTools(toolContext);
    }

    @Test
    void shouldFailExplicitlyWhenModelIsUnavailable() {
        List<AmazonSelectionReactEvent> events = new ArrayList<>();
        SheetDispatchAmazonSelectionAgent agent = new UnavailableModelAgent(toolContext, analysisTools);

        assertThatThrownBy(() -> agent.run(
                "analysis-run-1", "conversation-1", workbook(), "判断市场机会", events::add))
                .isInstanceOfSatisfying(AmazonSelectionAnalysisException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AmazonSelectionAnalysisException.ErrorCode.MODEL_UNAVAILABLE));

        assertThat(events).extracting(AmazonSelectionReactEvent::getEventType)
                .containsExactly("plan", "workbook", "sheet_prepare", "sheet", "sheet_focus", "error")
                .doesNotContain("summary");
        assertThat(events.getLast().getConversationId()).isEqualTo("conversation-1");
        assertThat(toolContext.get("analysis-run-1")).isEmpty();
    }

    @Test
    void shouldNotConvertRuntimeFailureIntoSuccessfulFallbackSummary() {
        List<AmazonSelectionReactEvent> events = new ArrayList<>();
        SheetDispatchAmazonSelectionAgent agent = new BrokenAgent(toolContext, analysisTools);

        assertThatThrownBy(() -> agent.run(
                "analysis-run-2", "conversation-1", workbook(), "判断市场机会", events::add))
                .isInstanceOfSatisfying(AmazonSelectionAnalysisException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AmazonSelectionAnalysisException.ErrorCode.AGENT_EXECUTION_FAILED));

        assertThat(events).extracting(AmazonSelectionReactEvent::getEventType)
                .contains("error")
                .doesNotContain("summary");
        assertThat(toolContext.get("analysis-run-2")).isEmpty();
    }

    private ProductWorkbook workbook() {
        ProductWorkbook workbook = new ProductWorkbook();
        workbook.setFileName("market-research-job-1.xlsx");
        ProductSheet sheet = new ProductSheet();
        sheet.setSheetName("US");
        sheet.setSheetIndex(0);
        sheet.setHeaders(List.of("ASIN"));
        ProductSheetRow row = new ProductSheetRow();
        row.setRowIndex(1);
        row.getCells().put("ASIN", "B000TEST");
        sheet.getRows().add(row);
        RawSheet rawSheet = new RawSheet();
        rawSheet.setSheetName("US");
        rawSheet.setSheetIndex(0);
        rawSheet.setRowCount(2);
        rawSheet.setColumnCount(1);
        rawSheet.setRawMarkdown("Sheet: US\nRow 1: ASIN\nRow 2: B000TEST");
        sheet.setRawSheet(rawSheet);
        workbook.getSheets().add(sheet);
        workbook.getRawSheets().add(rawSheet);
        return workbook;
    }

    private static final class UnavailableModelAgent extends SheetDispatchAmazonSelectionAgent {

        private UnavailableModelAgent(
                AmazonSelectionToolContext toolContext, AmazonProductAnalysisTools analysisTools) {
            super(new ToolCallback[0], "", toolContext, analysisTools, null);
            setName("UnavailableModelAgent");
            setSystemPrompt("system");
        }

        @Override
        protected String reflectSheet(
                String conversationId, String sheetName, String sheetObservation, String analysisGoal) {
            return callCompactModelStream(conversationId, "prompt", "Sheet 模型分析失败");
        }
    }

    private static final class BrokenAgent extends SheetDispatchAmazonSelectionAgent {

        private BrokenAgent(AmazonSelectionToolContext toolContext, AmazonProductAnalysisTools analysisTools) {
            super(new ToolCallback[0], "", toolContext, analysisTools, null);
            setName("BrokenAgent");
            setSystemPrompt("system");
        }

        @Override
        protected String reflectSheet(
                String conversationId, String sheetName, String sheetObservation, String analysisGoal) {
            throw new IllegalStateException("unexpected failure");
        }
    }
}
