package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.ai.research.curation.context.ModelInputCompactor;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.prompt.AmazonSelectionPromptTemplates;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.ResearchRawDataTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.ResearchReportChartTools;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** 阶段一：判断市场进入价值并支持 Top20 候选商品选择。 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ScreeningAnalysisAgent extends AmazonSelectionManus {

    private static final String SYSTEM_PROMPT = """
            你是 ScreeningAnalysisAgent，只负责亚马逊市场初筛。
            你根据阶段一七张证据表判断市场是否值得继续研究，并指出 Top20 候选商品的选择依据。
            评论、VOC、关键词宣传成本和选中 ASIN 深挖属于下一阶段；它们尚未采集不是阶段一的数据缺陷。
            证据表是稳定主干，原始字段目录和只读查询工具用于核查证据没有覆盖但可能影响初筛的信号。
            只陈述证据或工具结果能够支持的事实，不编造数字。
            """;
    private static final String NEXT_STEP_PROMPT = """
            先逐表提炼市场规模、趋势、细分机会、退货、品牌和集中度信号，再结合字段目录决定是否查询原始字段。
            最终明确给出继续进入阶段二、谨慎观察或放弃，并提供候选商品选择标准。
            """;

    public ScreeningAnalysisAgent(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider,
            AmazonSelectionToolContext toolContext,
            AmazonProductAnalysisTools analysisTools,
            ResearchRawDataTools rawDataTools,
            ResearchReportChartTools reportChartTools,
            List<Advisor> advisors,
            ModelInputCompactor modelInputCompactor) {
        super(
                "ScreeningAnalysisAgent",
                SYSTEM_PROMPT,
                NEXT_STEP_PROMPT,
                chatClientBuilderProvider,
                toolCallbackProviderProvider,
                toolContext,
                analysisTools,
                rawDataTools,
                advisors,
                modelInputCompactor,
                reportChartTools);
    }

    @Override
    protected String buildSheetPrompt(String sheetName, String sheetObservation, String analysisGoal) {
        return AmazonSelectionPromptTemplates.buildScreeningSheetPrompt(
                sheetName, sheetObservation, analysisGoal);
    }

    @Override
    protected String buildStageSummaryPrompt(
            String analysisRunId,
            ProductWorkbook workbook,
            String sheetSummaryMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return AmazonSelectionPromptTemplates.buildScreeningSummaryPrompt(
                analysisRunId,
                workbook.getFileName(),
                workbook.getSheets().size(),
                sheetSummaryMarkdown,
                rawFieldCatalog,
                analysisGoal);
    }

    @Override
    protected String buildFinalSummaryPrompt(
            String analysisRunId,
            ProductWorkbook workbook,
            String evidenceMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return buildStageSummaryPrompt(
                analysisRunId, workbook, evidenceMarkdown, analysisGoal, rawFieldCatalog);
    }
}
