package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.ai.research.curation.context.ModelInputCompactor;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
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

/** 基于既有报告和会话记忆，简短回答用户的后续问题。 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FollowUpAnswerAgent extends AmazonSelectionManus {

    private static final String SYSTEM_PROMPT = """
            你是 FollowUpAnswerAgent，负责回答亚马逊市场调研报告后的继续追问。
            优先基于同一会话中的正式报告、历史问答和当前问题作答；只有关键事实缺失时，才使用字段目录和只读原始数据工具核查。
            默认直接给出结论，再列最多 3 点关键依据，回答总长度约 200-500 个中文字符。
            用户明确要求详细说明、展开分析或完整推导时，可以突破默认长度和要点限制，但仍只回答当前问题。
            证据不足时明确说明缺口以及需要补充的数据，不使用行业常识编造事实或数字。
            禁止重写、复述或重新排版整份市场调研报告。
            """;
    private static final String NEXT_STEP_PROMPT = """
            围绕当前追问直接作答。默认先用一句话给出结论，再列最多 3 点依据，总长度约 200-500 个中文字符。
            仅当用户明确要求详细回答时展开；证据不足就指出缺口，不要重写整份报告。
            """;

    public FollowUpAnswerAgent(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider,
            AmazonSelectionToolContext toolContext,
            AmazonProductAnalysisTools analysisTools,
            ResearchRawDataTools rawDataTools,
            List<Advisor> advisors,
            ModelInputCompactor modelInputCompactor) {
        super(
                "FollowUpAnswerAgent",
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
    protected String buildFinalSummaryPrompt(
            String analysisRunId,
            ProductWorkbook ignoredWorkbook,
            String ignoredEvidenceMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return """
                请直接回答当前追问，不要重新生成市场调研报告。

                【分析运行 ID】
                %s

                【当前追问】
                %s

                【可查询原始字段目录】
                %s

                【回答要求】
                - 默认先给一句话结论，再列最多 3 点关键依据，总长度约 200-500 个中文字符。
                - 用户明确要求详细说明、展开分析或完整推导时，可以突破默认长度和要点限制。
                - 优先使用同一会话中的正式报告和历史问答；仅在关键事实缺失时调用只读原始数据工具。
                - 证据不足时明确指出缺口和需要补充的数据，不编造数字或事实。
                - 只回答当前问题，禁止重写、复述或重新排版整份报告。
                """.formatted(
                        defaultText(analysisRunId),
                        defaultText(analysisGoal),
                        defaultText(rawFieldCatalog));
    }

    private static String defaultText(String value) {
        return value == null || value.isBlank() ? "未提供" : value;
    }
}
