package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

class AbstractAmazonSelectionAgentStageTest {

    @Test
    void shouldRunFinalAnalysisOnceWithoutDispatchingIndividualSheets() {
        FinalOnlyAgent agent = new FinalOnlyAgent(new AmazonSelectionToolContext());
        List<AmazonSelectionReactEvent> events = new ArrayList<>();

        AmazonSelectionReactResult result = agent.runFinalAnalysis(
                "final-run-1",
                "conversation-1",
                workbook(10),
                "判断市场是否值得进入",
                events::add,
                CurationAnalysisBudget.unlimited());

        assertThat(agent.sheetDispatchCount()).isZero();
        assertThat(agent.finalSummaryCount()).isEqualTo(1);
        assertThat(result.getFinalSummary()).isEqualTo("十二表最终综合");
        assertThat(events)
                .extracting(AmazonSelectionReactEvent::getEventType)
                .containsExactly("plan", "summary_prepare", "summary");
        assertThat(events).allMatch(event -> event.getSheetName() == null);
    }

    private ProductWorkbook workbook(int sheetCount) {
        ProductWorkbook workbook = new ProductWorkbook();
        workbook.setFileName("market-research-job-1.xlsx");
        IntStream.range(0, sheetCount).forEach(index -> {
            ProductSheet sheet = new ProductSheet();
            sheet.setSheetName("Sheet-" + index);
            workbook.getSheets().add(sheet);
        });
        return workbook;
    }

    private static final class FinalOnlyAgent extends AbstractAmazonSelectionAgent {

        private final AtomicInteger sheetDispatchCount = new AtomicInteger();
        private final AtomicInteger finalSummaryCount = new AtomicInteger();

        private FinalOnlyAgent(AmazonSelectionToolContext toolContext) {
            super(
                    new ToolCallback[0],
                    null,
                    toolContext,
                    mock(AmazonProductAnalysisTools.class),
                    null);
            setName("FinalOnlyAgent");
        }

        @Override
        protected boolean dispatchSelection(
                String analysisRunId,
                String conversationId,
                ProductWorkbook workbook,
                String analysisGoal) {
            sheetDispatchCount.incrementAndGet();
            return true;
        }

        @Override
        protected String summarizeSelection(
                String analysisRunId,
                String conversationId,
                ProductWorkbook workbook,
                String analysisGoal) {
            throw new AssertionError("FINAL_ANALYSIS 不应执行逐 Sheet 总结路径");
        }

        @Override
        protected String summarizeFinalAnalysis(
                String analysisRunId,
                String conversationId,
                ProductWorkbook workbook,
                String analysisGoal) {
            finalSummaryCount.incrementAndGet();
            return "十二表最终综合";
        }

        private int sheetDispatchCount() {
            return sheetDispatchCount.get();
        }

        private int finalSummaryCount() {
            return finalSummaryCount.get();
        }
    }
}
