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

/** 阶段三：综合十二张证据表和前两阶段结论形成最终进入决策。 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FinalDecisionAgent extends AmazonSelectionManus {

    private static final String SYSTEM_PROMPT = """
            你是 FinalDecisionAgent，负责亚马逊市场调研的最终决策。
            你综合阶段一七张证据表、阶段二五张证据表以及同一会话中的两阶段结论，形成一次性最终报告。
            不重复逐 Sheet 分析；必要时才使用字段目录和只读查询工具核查会改变最终结论的原始信号。
            Keywords 竞价只能作为宣传获客成本与竞争难度信号，所选 ASIN 趋势不能外推为全市场表现。
            评论设置星级或类型筛选时必须按任务采集上下文解释，不能把定向样本外推为评论总体。
            只陈述证据或工具结果能够支持的事实，不编造数字。
            """;
    private static final String NEXT_STEP_PROMPT = """
            直接交叉验证市场规模、趋势、竞争、用户需求、宣传成本和所选 ASIN 经营表现。
            最终给出进入/谨慎进入/放弃、核心依据、执行策略、主要门槛和关键验证项。
            """;

    public FinalDecisionAgent(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider,
            AmazonSelectionToolContext toolContext,
            AmazonProductAnalysisTools analysisTools,
            ResearchRawDataTools rawDataTools,
            ResearchReportChartTools reportChartTools,
            List<Advisor> advisors,
            ModelInputCompactor modelInputCompactor) {
        super(
                "FinalDecisionAgent",
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
        return AmazonSelectionPromptTemplates.buildDeepDiveSheetPrompt(
                sheetName, sheetObservation, analysisGoal);
    }

    @Override
    protected String buildStageSummaryPrompt(
            String analysisRunId,
            ProductWorkbook workbook,
            String sheetSummaryMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return buildFinalSummaryPrompt(
                analysisRunId, workbook, sheetSummaryMarkdown, analysisGoal, rawFieldCatalog);
    }

    @Override
    protected String buildFinalSummaryPrompt(
            String analysisRunId,
            ProductWorkbook workbook,
            String evidenceMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return AmazonSelectionPromptTemplates.buildFinalDecisionPrompt(
                analysisRunId,
                workbook.getFileName(),
                workbook.getSheets().size(),
                evidenceMarkdown,
                rawFieldCatalog,
                analysisGoal);
    }
}
