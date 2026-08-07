package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.context.ContextCompressionAdvisor;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;

@Slf4j
public abstract class AbstractAmazonSelectionAgent extends ToolCallAgent {

    protected static final String EVENT_TYPE_PLAN = "plan";
    protected static final String EVENT_TYPE_SUMMARY_PREPARE = "summary_prepare";
    protected static final String EVENT_TYPE_SUMMARY = "summary";
    protected static final String EVENT_TYPE_SUMMARY_DELTA = "summary_delta";
    protected static final String EVENT_TYPE_ERROR = "error";
    protected static final String PHASE_SUMMARY = "summary";
    protected static final String PHASE_ERROR = "error";

    private final AmazonSelectionToolContext toolContext;
    private final AmazonProductAnalysisTools analysisTools;
    private final ChatClient compactChatClient;
    private final Object[] conversationTools;

    private Consumer<AmazonSelectionReactEvent> eventConsumer;
    private AtomicInteger stepIndex;
    private CurationAnalysisBudget analysisBudget = CurationAnalysisBudget.unlimited();

    protected AbstractAmazonSelectionAgent(ToolCallback[] allTools, String chatId,
            AmazonSelectionToolContext toolContext, AmazonProductAnalysisTools analysisTools,
            ChatClient compactChatClient, Object... conversationTools) {
        super(allTools, chatId);
        this.toolContext = toolContext;
        this.analysisTools = analysisTools;
        this.compactChatClient = compactChatClient;
        this.conversationTools = conversationTools == null ? new Object[0] : conversationTools;
    }

    protected AbstractAmazonSelectionAgent(ToolCallback[] allTools, String chatId,
            AmazonSelectionToolContext toolContext, AmazonProductAnalysisTools analysisTools,
            ChatClient compactChatClient) {
        this(allTools, chatId, toolContext, analysisTools, compactChatClient, new Object[0]);
    }

    public final AmazonSelectionReactResult run(String conversationId, ProductWorkbook workbook,
            Consumer<AmazonSelectionReactEvent> eventConsumer) {
        return run(conversationId, conversationId, workbook, "", eventConsumer);
    }

    public final AmazonSelectionReactResult run(String conversationId, ProductWorkbook workbook, String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer) {
        return run(conversationId, conversationId, workbook, userAnalysisGoal, eventConsumer);
    }

    /**
     * @param analysisRunId 唯一分析运行 ID，用作工具上下文隔离键
     * @param conversationId 可跨运行复用的对话 ID，用作 ChatMemory 与前端事件关联键
     */
    public final AmazonSelectionReactResult run(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer) {
        return run(analysisRunId, conversationId, workbook, userAnalysisGoal, eventConsumer,
                CurationAnalysisBudget.unlimited());
    }

    public final AmazonSelectionReactResult run(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer,
            CurationAnalysisBudget analysisBudget) {
        return run(
                analysisRunId,
                conversationId,
                workbook,
                userAnalysisGoal,
                eventConsumer,
                analysisBudget,
                null);
    }

    public final AmazonSelectionReactResult run(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer,
            CurationAnalysisBudget analysisBudget,
            ResearchRawDataAccessScope rawDataScope) {
        return runInternal(
                analysisRunId,
                conversationId,
                workbook,
                userAnalysisGoal,
                eventConsumer,
                analysisBudget,
                rawDataScope,
                true);
    }

    /** FINAL_ANALYSIS 读取完整证据，但不重复执行前两阶段的逐 Sheet 模型调用。 */
    public final AmazonSelectionReactResult runFinalAnalysis(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer,
            CurationAnalysisBudget analysisBudget) {
        return runFinalAnalysis(
                analysisRunId,
                conversationId,
                workbook,
                userAnalysisGoal,
                eventConsumer,
                analysisBudget,
                null);
    }

    public final AmazonSelectionReactResult runFinalAnalysis(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer,
            CurationAnalysisBudget analysisBudget,
            ResearchRawDataAccessScope rawDataScope) {
        return runInternal(
                analysisRunId,
                conversationId,
                workbook,
                userAnalysisGoal,
                eventConsumer,
                analysisBudget,
                rawDataScope,
                false);
    }

    private AmazonSelectionReactResult runInternal(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            String userAnalysisGoal,
            Consumer<AmazonSelectionReactEvent> eventConsumer,
            CurationAnalysisBudget analysisBudget,
            ResearchRawDataAccessScope rawDataScope,
            boolean analyzeEachSheet) {
        requireRunInput(analysisRunId, conversationId, workbook);
        this.analysisBudget = analysisBudget == null ? CurationAnalysisBudget.unlimited() : analysisBudget;
        this.analysisBudget.assertSheetCount(workbook.getSheets().size());
        setChatId(conversationId);
        String analysisGoal = normalizeAnalysisGoal(userAnalysisGoal);
        prepareSelectionRun(eventConsumer);
        toolContext.start(analysisRunId, conversationId, workbook, rawDataScope);
        try {
            String planMessage = analyzeEachSheet
                    ? buildPlanMessage()
                    : buildFinalAnalysisPlanMessage();
            emit(event(conversationId, nextEventIndex(), EVENT_TYPE_PLAN, EVENT_TYPE_PLAN,
                    planMessage, analysisGoal));
            prepareToolCallRun();
            boolean modelInvoked = analyzeEachSheet
                    && dispatchSelection(analysisRunId, conversationId, workbook, analysisGoal);
            AmazonSelectionReactResult result = toolContext.finish(analysisRunId);
            if (!analyzeEachSheet) {
                emit(event(
                        conversationId,
                        nextEventIndex(),
                        EVENT_TYPE_SUMMARY_PREPARE,
                        PHASE_SUMMARY,
                        "准备结合完整十二表证据和前两阶段结论生成最终综合。",
                        workbook.getSheets().size()));
            }
            String finalAnswer = analyzeEachSheet
                    ? summarizeSelection(analysisRunId, conversationId, workbook, analysisGoal)
                    : summarizeFinalAnalysis(analysisRunId, conversationId, workbook, analysisGoal);
            if (finalAnswer == null || finalAnswer.isBlank()) {
                throw new AmazonSelectionAnalysisException(
                        AmazonSelectionAnalysisException.ErrorCode.MODEL_EMPTY_RESPONSE,
                        "模型未生成最终分析摘要");
            }
            result.setFinalSummary(finalAnswer);
            result.setModelInvoked(modelInvoked || !finalAnswer.isBlank());
            emit(event(conversationId, nextEventIndex(), EVENT_TYPE_SUMMARY, PHASE_SUMMARY,
                    result.getFinalSummary(), result));
            return result;
        } catch (RuntimeException ex) {
            AmazonSelectionAnalysisException failure = asAnalysisFailure(ex);
            log.warn("{} 执行失败，analysisRunId={}, conversationId={}, errorCode={}",
                    getName(), analysisRunId, conversationId, failure.getErrorCode(), ex);
            emitFailure(conversationId, failure);
            throw failure;
        } finally {
            toolContext.remove(analysisRunId);
            this.analysisBudget = CurationAnalysisBudget.unlimited();
        }
    }

    protected abstract boolean dispatchSelection(
            String analysisRunId, String conversationId, ProductWorkbook workbook, String analysisGoal);

    protected abstract String summarizeSelection(
            String analysisRunId, String conversationId, ProductWorkbook workbook, String analysisGoal);

    protected String summarizeFinalAnalysis(
            String analysisRunId, String conversationId, ProductWorkbook workbook, String analysisGoal) {
        return summarizeSelection(analysisRunId, conversationId, workbook, analysisGoal);
    }

    protected AmazonSelectionToolContext getToolContext() {
        return toolContext;
    }

    protected AmazonProductAnalysisTools getAnalysisTools() {
        return analysisTools;
    }

    protected int nextEventIndex() {
        return stepIndex.getAndIncrement();
    }

    protected void emit(AmazonSelectionReactEvent event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }

    @Override
    protected void customizeAdvisorParams(ChatClient.AdvisorSpec advisorSpec) {
        if (eventConsumer != null) {
            advisorSpec.param(ContextCompressionAdvisor.EVENT_CONSUMER_CONTEXT_KEY, eventConsumer);
        }
        advisorSpec.param(ContextCompressionAdvisor.ANALYSIS_BUDGET_CONTEXT_KEY, analysisBudget);
    }

    @Override
    protected void beforeModelCall() {
        analysisBudget.beforeModelCall();
    }

    @Override
    protected void afterModelCall() {
        analysisBudget.afterModelCall();
    }

    protected AmazonSelectionReactEvent event(String conversationId, int stepIndex, String eventType, String phase,
            String message, Object data) {
        return AmazonSelectionReactEvent.builder()
                .eventType(eventType)
                .conversationId(conversationId)
                .stepIndex(stepIndex)
                .phase(phase)
                .message(message)
                .data(data)
                .build();
    }

    protected AmazonSelectionReactEvent sheetEvent(String conversationId, int stepIndex, String eventType, String phase,
            String message, String sheetName, Object data) {
        return AmazonSelectionReactEvent.builder()
                .eventType(eventType)
                .conversationId(conversationId)
                .stepIndex(stepIndex)
                .sheetName(sheetName)
                .phase(phase)
                .message(message)
                .data(data)
                .build();
    }

    protected String callCompactModelText(String conversationId, String prompt, String failureMessage) {
        if (compactChatClient == null) {
            throw new AmazonSelectionAnalysisException(
                    AmazonSelectionAnalysisException.ErrorCode.MODEL_UNAVAILABLE,
                    "未配置可用的市场调研分析模型");
        }
        beforeModelCall();
        try {
            String content = compactChatClient
                    .prompt()
                    .system(getSystemPrompt())
                    .user(prompt)
                    .call()
                    .content();
            afterModelCall();
            if (content == null || content.isBlank()) {
                throw new AmazonSelectionAnalysisException(
                        AmazonSelectionAnalysisException.ErrorCode.MODEL_EMPTY_RESPONSE,
                        failureMessage + "：模型返回空内容");
            }
            return content;
        } catch (AmazonSelectionAnalysisException exception) {
            throw exception;
        } catch (RuntimeException ex) {
            afterModelCall();
            log.warn("{}，conversationId={}", failureMessage, conversationId, ex);
            throw new AmazonSelectionAnalysisException(
                    AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED,
                    failureMessage,
                    ex);
        }
    }

    protected String callCompactModelStream(String conversationId, String prompt, String failureMessage) {
        return callCompactModelStream(conversationId, prompt, failureMessage,
                delta -> emit(event(conversationId, nextEventIndex(), EVENT_TYPE_SUMMARY_DELTA,
                        PHASE_SUMMARY, delta, delta)));
    }

    protected String callCompactModelStream(String conversationId, String prompt, String failureMessage,
            Consumer<String> deltaConsumer) {
        if (compactChatClient == null) {
            throw new AmazonSelectionAnalysisException(
                    AmazonSelectionAnalysisException.ErrorCode.MODEL_UNAVAILABLE,
                    "未配置可用的市场调研分析模型");
        }
        StringBuilder contentBuilder = new StringBuilder();
        beforeModelCall();
        try {
            compactChatClient
                    .prompt()
                    .system(getSystemPrompt())
                    .user(prompt)
                    .stream()
                    .content()
                    .filter(delta -> delta != null && !delta.isEmpty())
                    .doOnNext(delta -> {
                        contentBuilder.append(delta);
                        deltaConsumer.accept(delta);
                    })
                    .blockLast();
            afterModelCall();
            if (contentBuilder.isEmpty()) {
                throw new AmazonSelectionAnalysisException(
                        AmazonSelectionAnalysisException.ErrorCode.MODEL_EMPTY_RESPONSE,
                        failureMessage + "：模型返回空内容");
            }
            return contentBuilder.toString();
        } catch (AmazonSelectionAnalysisException exception) {
            throw exception;
        } catch (RuntimeException ex) {
            afterModelCall();
            log.warn("{}，conversationId={}", failureMessage, conversationId, ex);
            throw new AmazonSelectionAnalysisException(
                    AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED,
                    failureMessage,
                    ex);
        }
    }

    /**
     * 最终摘要使用带 ChatMemory 的完整客户端，使同一 conversationId 下的后续提问可以延续上下文。
     */
    protected String callConversationModelStream(String conversationId, String prompt, String failureMessage) {
        ChatClient conversationChatClient = getChatClient();
        if (conversationChatClient == null) {
            throw new AmazonSelectionAnalysisException(
                    AmazonSelectionAnalysisException.ErrorCode.MODEL_UNAVAILABLE,
                    "未配置可用的市场调研分析模型");
        }
        StringBuilder contentBuilder = new StringBuilder();
        beforeModelCall();
        try {
            ChatClient.ChatClientRequestSpec requestSpec = conversationChatClient
                    .prompt()
                    .system(getSystemPrompt())
                    .user(prompt)
                    .advisors(advisorSpec -> {
                        advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId);
                        customizeAdvisorParams(advisorSpec);
                    });
            if (conversationTools.length > 0) {
                requestSpec.tools(conversationTools);
            }
            requestSpec
                    .stream()
                    .content()
                    .filter(delta -> delta != null && !delta.isEmpty())
                    .doOnNext(delta -> {
                        contentBuilder.append(delta);
                        emit(event(conversationId, nextEventIndex(), EVENT_TYPE_SUMMARY_DELTA,
                                PHASE_SUMMARY, delta, delta));
                    })
                    .blockLast();
            afterModelCall();
            if (contentBuilder.isEmpty()) {
                throw new AmazonSelectionAnalysisException(
                        AmazonSelectionAnalysisException.ErrorCode.MODEL_EMPTY_RESPONSE,
                        failureMessage + "：模型返回空内容");
            }
            return contentBuilder.toString();
        } catch (AmazonSelectionAnalysisException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            afterModelCall();
            log.warn("{}，conversationId={}", failureMessage, conversationId, exception);
            throw new AmazonSelectionAnalysisException(
                    AmazonSelectionAnalysisException.ErrorCode.MODEL_INVOCATION_FAILED,
                    failureMessage,
                    exception);
        }
    }

    protected ChatClient getCompactChatClient() {
        return compactChatClient;
    }

    protected String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    protected String buildPlanMessage() {
        return "启动 " + getName() + "：Java 逐个读取全部 sheet，模型将围绕用户分析目标理解、评分并生成报告。";
    }

    protected String buildFinalAnalysisPlanMessage() {
        return "启动 " + getName() + "：读取完整十二表 evidence，一次性生成最终综合，不重复逐 Sheet 分析。";
    }

    protected String normalizeAnalysisGoal(String userAnalysisGoal) {
        return userAnalysisGoal == null || userAnalysisGoal.isBlank()
                ? "完成通用亚马逊选品数据分析"
                : userAnalysisGoal.trim();
    }

    private void prepareSelectionRun(Consumer<AmazonSelectionReactEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
        this.stepIndex = new AtomicInteger();
    }

    private void requireRunInput(String analysisRunId, String conversationId, ProductWorkbook workbook) {
        if (analysisRunId == null || analysisRunId.isBlank()) {
            throw new IllegalArgumentException("analysisRunId 不能为空");
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        if (workbook == null || workbook.getSheets() == null || workbook.getSheets().isEmpty()) {
            throw new IllegalArgumentException("市场调研证据工作簿不能为空");
        }
    }

    private AmazonSelectionAnalysisException asAnalysisFailure(RuntimeException exception) {
        if (exception instanceof AmazonSelectionAnalysisException analysisException) {
            return analysisException;
        }
        return new AmazonSelectionAnalysisException(
                AmazonSelectionAnalysisException.ErrorCode.AGENT_EXECUTION_FAILED,
                getName() + " 执行失败",
                exception);
    }

    private void emitFailure(String conversationId, AmazonSelectionAnalysisException failure) {
        try {
            emit(event(conversationId, nextEventIndex(), EVENT_TYPE_ERROR, PHASE_ERROR,
                    failure.getMessage(),
                    new AnalysisFailureData(failure.getErrorCode().name(), failure.getMessage())));
        } catch (RuntimeException eventException) {
            log.warn("{} 失败事件发送失败，conversationId={}", getName(), conversationId, eventException);
        }
    }

    private record AnalysisFailureData(String errorCode, String message) {
    }
}
