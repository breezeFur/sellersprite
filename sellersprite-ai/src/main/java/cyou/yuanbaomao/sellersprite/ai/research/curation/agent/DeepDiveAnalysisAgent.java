package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.ai.research.curation.context.ModelInputCompactor;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.prompt.AmazonSelectionPromptTemplates;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.ResearchRawDataTools;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** 阶段二：深挖人工选中 ASIN 的用户反馈、获客信号与经营趋势。 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeepDiveAnalysisAgent extends AmazonSelectionManus {

    private static final String SYSTEM_PROMPT = """
            你是 DeepDiveAnalysisAgent，只负责人工选中 ASIN 的深挖分析。
            你分析评价、VOC、Keywords、ASIN 销售趋势和 Keepa 经营趋势五张证据表。
            Keywords 用于判断宣传获客成本信号、竞争强度和投放难度，不得在缺少实际花费与转化时推算 ACOS、ROI 或预算。
            必须读取任务采集上下文中的评论星级和类型筛选；定向星级样本不得外推总体差评率、平均星级或满意度。
            证据表是稳定主干，原始字段目录和只读查询工具用于核查评论细节、关键词和 ASIN 趋势的补充字段。
            只陈述证据或工具结果能够支持的事实，不编造数字。
            """;
    private static final String NEXT_STEP_PROMPT = """
            先逐表提炼用户需求、产品缺陷、宣传成本信号和所选 ASIN 经营稳定性，再按需查询原始字段。
            最终输出产品改进方向、获客难度、竞品经营风险和进入阶段三所需的深挖结论。
            """;

    public DeepDiveAnalysisAgent(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider,
            AmazonSelectionToolContext toolContext,
            AmazonProductAnalysisTools analysisTools,
            ResearchRawDataTools rawDataTools,
            List<Advisor> advisors,
            ModelInputCompactor modelInputCompactor) {
        super(
                "DeepDiveAnalysisAgent",
                SYSTEM_PROMPT,
                NEXT_STEP_PROMPT,
                chatClientBuilderProvider,
                toolCallbackProviderProvider,
                toolContext,
                analysisTools,
                rawDataTools,
                advisors,
                modelInputCompactor);
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
        return AmazonSelectionPromptTemplates.buildDeepDiveSummaryPrompt(
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
