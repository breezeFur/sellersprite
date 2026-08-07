package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.prompt.AmazonSelectionPromptTemplates;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallback;

public abstract class SheetDispatchAmazonSelectionAgent extends AbstractAmazonSelectionAgent {

    private static final String OBSERVATION_TITLE_USER_REQUEST = "用户任务";
    private static final String OBSERVATION_TITLE_WORKBOOK = "工作簿结构";
    private static final String EVENT_TYPE_WORKBOOK = "workbook";
    private static final String EVENT_TYPE_SHEET_PREPARE = "sheet_prepare";
    private static final String EVENT_TYPE_SHEET = "sheet";
    private static final String EVENT_TYPE_SHEET_FOCUS = "sheet_focus";
    private static final String EVENT_TYPE_SHEET_THINK = "sheet_think";
    private static final String PHASE_OBSERVE = "observe";
    private static final String PHASE_THINK = "think";

    protected SheetDispatchAmazonSelectionAgent(ToolCallback[] allTools, String chatId,
            AmazonSelectionToolContext toolContext, AmazonProductAnalysisTools analysisTools,
            ChatClient compactChatClient, Object... conversationTools) {
        super(allTools, chatId, toolContext, analysisTools, compactChatClient, conversationTools);
    }

    protected SheetDispatchAmazonSelectionAgent(ToolCallback[] allTools, String chatId,
            AmazonSelectionToolContext toolContext, AmazonProductAnalysisTools analysisTools,
            ChatClient compactChatClient) {
        this(allTools, chatId, toolContext, analysisTools, compactChatClient, new Object[0]);
    }

    @Override
    protected final boolean dispatchSelection(
            String analysisRunId, String conversationId, ProductWorkbook workbook, String analysisGoal) {
        boolean modelInvoked = false;
        appendObservation(OBSERVATION_TITLE_USER_REQUEST, buildUserPrompt(conversationId, workbook, analysisGoal));
        String workbookObservation = getAnalysisTools().inspectAmazonWorkbook(analysisRunId);
        appendObservation(OBSERVATION_TITLE_WORKBOOK, workbookObservation);
        emit(event(conversationId, nextEventIndex(), EVENT_TYPE_WORKBOOK, PHASE_OBSERVE,
                "已观察工作簿结构，共 " + workbook.getSheets().size() + " 个 sheet。", workbookObservation));

        for (var sheet : workbook.getSheets()) {
            String sheetName = sheet.getSheetName();
            emit(sheetEvent(conversationId, nextEventIndex(), EVENT_TYPE_SHEET_PREPARE, PHASE_THINK,
                    "准备读取 Sheet「" + sheetName + "」的原始文本和图片资产。", sheetName,
                    buildSheetPrepareMessage(sheetName)));
            String sheetObservation = getAnalysisTools().inspectAmazonSheet(analysisRunId, sheetName);
            appendObservation("Sheet：" + sheetName, sheetObservation);
            emit(sheetEvent(conversationId, nextEventIndex(), EVENT_TYPE_SHEET, PHASE_OBSERVE,
                    "已分析 Sheet「" + sheetName + "」。", sheetName, sheetObservation));
            emit(sheetEvent(conversationId, nextEventIndex(), EVENT_TYPE_SHEET_FOCUS, PHASE_THINK,
                    "已整理 Sheet「" + sheetName + "」的判断重点。", sheetName,
                    buildSheetFocus(sheetName, sheetObservation, analysisGoal)));
            String reflection = reflectSheet(conversationId, sheetName, sheetObservation, analysisGoal);
            if (reflection != null && !reflection.isBlank()) {
                modelInvoked = true;
                updateSheetSummary(analysisRunId, sheetName, reflection);
                appendObservation("模型对 Sheet「" + sheetName + "」的判断", reflection);
                emit(sheetEvent(conversationId, nextEventIndex(), EVENT_TYPE_SHEET_THINK, PHASE_THINK,
                        "模型完成 Sheet「" + sheetName + "」价值判断。", sheetName, reflection));
            }
        }

        emit(event(conversationId, nextEventIndex(), EVENT_TYPE_SUMMARY_PREPARE, PHASE_SUMMARY,
                "准备让模型基于全部 Sheet 观察和反思生成选品评分报告。", "最终总结会优先使用已观察到的事实，不补写 evidence 中不存在的数据。"));
        return modelInvoked;
    }

    protected abstract String reflectSheet(String conversationId, String sheetName, String sheetObservation,
            String analysisGoal);

    @Override
    protected String summarizeSelection(
            String analysisRunId, String conversationId, ProductWorkbook workbook, String analysisGoal) {
        return callConversationModelStream(conversationId,
                buildSummaryContextPrompt(analysisRunId, workbook, analysisGoal),
                "最终模型汇总失败");
    }

    String buildSummaryContextPrompt(String analysisRunId, ProductWorkbook workbook) {
        return buildSummaryContextPrompt(analysisRunId, workbook, "");
    }

    String buildSummaryContextPrompt(String analysisRunId, ProductWorkbook workbook, String analysisGoal) {
        String sheetSummaryMarkdown = buildSheetSummaryMarkdown(analysisRunId);
        return AmazonSelectionPromptTemplates.buildFinalSummaryPrompt(workbook.getFileName(), workbook.getSheets().size(),
                sheetSummaryMarkdown, analysisGoal);
    }

    protected String buildSheetPrepareMessage(String sheetName) {
        return "思考入口：先保留 Sheet「" + sheetName + "」的原始行文本，再查看图片 URL、DISPIMG 公式和嵌入图片元数据。";
    }

    protected String buildSheetFocus(String sheetName, String sheetObservation, String analysisGoal) {
        return "思考重点：Sheet「" + sheetName + "」需要判断它属于趋势、竞品、商品集中度、评价/VOC、关键词或结论说明中的哪一类；"
                + "同时优先判断它是否能回答用户分析目标：「" + analysisGoal + "」；"
                + "再从原始文本中提取会影响选品决策的机会、风险和需要进入最终汇总的证据。\n\n"
                + defaultText(sheetObservation, "");
    }

    protected String buildSheetSummaryMarkdown(String analysisRunId) {
        var summaries = getMessageHistory().stream()
                .map(Message::getText)
                .filter(this::isModelSheetSummary)
                .map(this::normalizeSheetSummary)
                .toList();
        if (!summaries.isEmpty()) {
            return String.join("\n\n", summaries);
        }
        return getToolContext().get(analysisRunId)
                .map(result -> result.getSheetAnalyses().stream()
                        .filter(sheet -> sheet.getSummary() != null && !sheet.getSummary().isBlank())
                        .map(sheet -> "### " + sheet.getSheetName() + "\n" + sheet.getSummary())
                        .toList())
                .filter(resultSummaries -> !resultSummaries.isEmpty())
                .map(resultSummaries -> String.join("\n\n", resultSummaries))
                .orElse("当前没有可用 Sheet 富摘要。");
    }

    private boolean isModelSheetSummary(String text) {
        return text != null && text.startsWith("【模型对 Sheet「") && text.contains("」的判断】");
    }

    private String normalizeSheetSummary(String text) {
        int titleEndIndex = text.indexOf('】');
        if (titleEndIndex < 0) {
            return text;
        }
        String title = text.substring(1, titleEndIndex);
        String content = text.substring(titleEndIndex + 1).trim();
        return "### " + extractSheetName(title) + "\n" + content;
    }

    private String extractSheetName(String title) {
        int startIndex = title.indexOf("Sheet「");
        int endIndex = title.indexOf("」", startIndex);
        if (startIndex < 0 || endIndex <= startIndex) {
            return title;
        }
        return title.substring(startIndex + "Sheet「".length(), endIndex);
    }

    private void appendObservation(String title, String content) {
        getMessageHistory().add(new UserMessage("【" + title + "】\n" + defaultText(content, "")));
    }

    private void updateSheetSummary(String analysisRunId, String sheetName, String summary) {
        getToolContext().get(analysisRunId).ifPresent(result -> result.getSheetAnalyses().stream()
                .filter(sheet -> sheetName.equals(sheet.getSheetName()))
                .findFirst()
                .ifPresent(sheet -> sheet.setSummary(summary)));
    }

    private String buildUserPrompt(String conversationId, ProductWorkbook workbook, String analysisGoal) {
        return "会话 ID：" + conversationId + "\n"
                + "文件名：" + workbook.getFileName() + "\n"
                + "sheet 数：" + workbook.getSheets().size() + "\n"
                + "用户分析目标：" + analysisGoal + "\n"
                + "请开始分析这个多 sheet 亚马逊选品调研表。";
    }
}
