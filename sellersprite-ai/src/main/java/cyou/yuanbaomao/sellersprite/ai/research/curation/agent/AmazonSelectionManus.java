package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.advisor.AmazonSelectionAdvisorSupport;
import cyou.yuanbaomao.sellersprite.ai.research.curation.context.ModelInputCompactor;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.ResearchRawDataTools;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;

/** 三个市场调研专用 Agent 共用的执行基础设施。 */
public abstract class AmazonSelectionManus extends SheetDispatchAmazonSelectionAgent {

    private static final int MANUS_MAX_STEPS = 20;
    private static final String EVENT_TYPE_SHEET_THINK_DELTA = "sheet_think_delta";
    private static final String PHASE_THINK = "think";

    private final ModelInputCompactor modelInputCompactor;
    private final ResearchRawDataTools rawDataTools;

    protected AmazonSelectionManus(
            String agentName,
            String systemPrompt,
            String nextStepPrompt,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider,
            AmazonSelectionToolContext toolContext,
            AmazonProductAnalysisTools analysisTools,
            ResearchRawDataTools rawDataTools,
            List<Advisor> advisors,
            ModelInputCompactor modelInputCompactor) {
        this(
                agentName,
                systemPrompt,
                nextStepPrompt,
                chatClientBuilderProvider,
                toolCallbackProviderProvider,
                toolContext,
                analysisTools,
                rawDataTools,
                advisors,
                modelInputCompactor,
                new Object[0]);
    }

    protected AmazonSelectionManus(
            String agentName,
            String systemPrompt,
            String nextStepPrompt,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider,
            AmazonSelectionToolContext toolContext,
            AmazonProductAnalysisTools analysisTools,
            ResearchRawDataTools rawDataTools,
            List<Advisor> advisors,
            ModelInputCompactor modelInputCompactor,
            Object... additionalConversationTools) {
        super(
                toolCallbacks(toolCallbackProviderProvider),
                null,
                toolContext,
                analysisTools,
                buildCompactChatClient(chatClientBuilderProvider, advisors),
                conversationTools(rawDataTools, additionalConversationTools));
        this.modelInputCompactor = modelInputCompactor;
        this.rawDataTools = rawDataTools;
        setName(agentName);
        setSystemPrompt(systemPrompt);
        setNextStepPrompt(nextStepPrompt);
        setMaxSteps(MANUS_MAX_STEPS);
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder != null) {
            setChatClient(chatClientBuilder.clone()
                    .defaultAdvisors(advisors == null ? List.of() : new ArrayList<>(advisors))
                    .build());
        }
    }

    static List<Advisor> compactAdvisors(List<Advisor> advisors) {
        return AmazonSelectionAdvisorSupport.compactAdvisors(advisors);
    }

    @Override
    protected void onThink(AssistantMessage output) {
        String toolNames = output.getToolCalls().stream()
                .map(AssistantMessage.ToolCall::name)
                .collect(Collectors.joining("、"));
        String message = output.hasToolCalls()
                ? "模型决定调用工具：" + toolNames
                : defaultText(output.getText(), "模型已输出分析结论。");
        emit(event(getChatId(), nextEventIndex(), "think", PHASE_THINK, message, output.getText()));
    }

    @Override
    protected void onAct(String result) {
        emit(event(getChatId(), nextEventIndex(), "tool", "act", result, result));
    }

    @Override
    protected String reflectSheet(
            String conversationId,
            String sheetName,
            String sheetObservation,
            String analysisGoal) {
        String prompt = buildSheetPrompt(sheetName, sheetObservation, analysisGoal);
        String compactedPrompt = modelInputCompactor.compact(prompt);
        return callCompactModelStream(
                conversationId,
                sheetName,
                compactedPrompt,
                EVENT_TYPE_SHEET_THINK_DELTA,
                PHASE_THINK,
                "Sheet「" + sheetName + "」模型富摘要失败");
    }

    @Override
    protected String summarizeSelection(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String analysisGoal) {
        String prompt = buildStageSummaryPrompt(
                analysisRunId,
                workbook,
                buildSheetSummaryMarkdown(analysisRunId),
                analysisGoal,
                rawDataTools.inspectResearchRawDatasetCatalog(analysisRunId));
        return callConversationModelStream(
                conversationId,
                modelInputCompactor.compact(prompt),
                getName() + " 阶段汇总失败");
    }

    @Override
    protected String summarizeFinalAnalysis(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String analysisGoal) {
        String evidenceMarkdown = workbook.getRawSheets().stream()
                .map(rawSheet -> rawSheet.getRawMarkdown() == null ? "" : rawSheet.getRawMarkdown())
                .filter(markdown -> !markdown.isBlank())
                .collect(Collectors.joining("\n\n"));
        String prompt = buildFinalSummaryPrompt(
                analysisRunId,
                workbook,
                evidenceMarkdown,
                analysisGoal,
                rawDataTools.inspectResearchRawDatasetCatalog(analysisRunId));
        return callConversationModelStream(
                conversationId,
                modelInputCompactor.compact(prompt),
                getName() + " 最终综合失败");
    }

    protected String buildSheetPrompt(
            String sheetName, String sheetObservation, String analysisGoal) {
        return cyou.yuanbaomao.sellersprite.ai.research.curation.prompt.AmazonSelectionPromptTemplates
                .buildSheetSummaryPrompt(sheetName, sheetObservation, analysisGoal);
    }

    protected String buildStageSummaryPrompt(
            String analysisRunId,
            ProductWorkbook workbook,
            String sheetSummaryMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return cyou.yuanbaomao.sellersprite.ai.research.curation.prompt.AmazonSelectionPromptTemplates
                .buildFinalSummaryPrompt(
                        workbook.getFileName(),
                        workbook.getSheets().size(),
                        sheetSummaryMarkdown,
                        analysisGoal);
    }

    protected String buildFinalSummaryPrompt(
            String analysisRunId,
            ProductWorkbook workbook,
            String evidenceMarkdown,
            String analysisGoal,
            String rawFieldCatalog) {
        return cyou.yuanbaomao.sellersprite.ai.research.curation.prompt.AmazonSelectionPromptTemplates
                .buildFinalEvidenceSummaryPrompt(
                        workbook.getFileName(),
                        workbook.getSheets().size(),
                        evidenceMarkdown,
                        analysisGoal);
    }

    private String callCompactModelStream(
            String conversationId,
            String sheetName,
            String prompt,
            String deltaEventType,
            String phase,
            String failureMessage) {
        return callCompactModelStream(
                conversationId,
                prompt,
                failureMessage,
                delta -> emit(streamDeltaEvent(conversationId, sheetName, deltaEventType, phase, delta)));
    }

    private AmazonSelectionReactEvent streamDeltaEvent(
            String conversationId, String sheetName, String eventType, String phase, String delta) {
        if (sheetName == null || sheetName.isBlank()) {
            return event(conversationId, nextEventIndex(), eventType, phase, delta, delta);
        }
        return sheetEvent(conversationId, nextEventIndex(), eventType, phase, delta, sheetName, delta);
    }

    private static ToolCallback[] toolCallbacks(ObjectProvider<ToolCallbackProvider> provider) {
        ToolCallbackProvider toolCallbackProvider = provider.getIfAvailable();
        return toolCallbackProvider == null ? new ToolCallback[0] : toolCallbackProvider.getToolCallbacks();
    }

    private static Object[] conversationTools(
            ResearchRawDataTools rawDataTools, Object[] additionalConversationTools) {
        Object[] tools = new Object[additionalConversationTools.length + 1];
        tools[0] = rawDataTools;
        System.arraycopy(additionalConversationTools, 0, tools, 1, additionalConversationTools.length);
        return tools;
    }

    private static ChatClient buildCompactChatClient(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider, List<Advisor> advisors) {
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder == null) {
            return null;
        }
        return chatClientBuilder.clone()
                .defaultAdvisors(compactAdvisors(advisors))
                .build();
    }
}
