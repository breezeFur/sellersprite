package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactEvent;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ContextCompressionAdvisor implements BaseAdvisor {

    public static final String EVENT_CONSUMER_CONTEXT_KEY = "context_compression_event_consumer";
    public static final String ANALYSIS_BUDGET_CONTEXT_KEY = "curation_analysis_budget";

    private static final int ORDER_BEFORE_CHAT_MEMORY = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 20;
    private static final String EVENT_TYPE_CONTEXT_COMPRESS_START = "context_compress_start";
    private static final String EVENT_TYPE_CONTEXT_COMPRESS_DONE = "context_compress_done";
    private static final String EVENT_TYPE_CONTEXT_COMPRESS_FAILED = "context_compress_failed";
    private static final String PHASE_CONTEXT = "context";

    private final ChatMemoryRepository chatMemoryRepository;
    private final ContextWindowEstimator contextWindowEstimator;
    private final ContextCompressionAgent contextCompressionAgent;
    private final CurationAnalysisProperties analysisProperties;

    public ContextCompressionAdvisor(ChatMemoryRepository chatMemoryRepository,
            ContextWindowEstimator contextWindowEstimator,
            ContextCompressionAgent contextCompressionAgent,
            CurationAnalysisProperties analysisProperties) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.contextWindowEstimator = contextWindowEstimator;
        this.contextCompressionAgent = contextCompressionAgent;
        this.analysisProperties = analysisProperties;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        CurationAnalysisProperties.ContextCompression properties = analysisProperties.getContextCompression();
        if (!properties.isEnabled()) {
            return request;
        }
        String conversationId = getConversationId(request);
        if (!StringUtils.hasText(conversationId)) {
            return request;
        }
        Consumer<AmazonSelectionReactEvent> eventConsumer = getEventConsumer(request);
        try {
            List<Message> memoryMessages = chatMemoryRepository.findByConversationId(conversationId);
            if (memoryMessages.size() < properties.getMinMessagesToCompress()) {
                return request;
            }
            ContextWindowSnapshot snapshot = contextWindowEstimator.snapshot(memoryMessages,
                    request.prompt().getInstructions(), properties.getMaxContextTokens(), properties.getTriggerRatio());
            if (!snapshot.requiresCompression()) {
                return request;
            }

            emit(eventConsumer, event(conversationId, EVENT_TYPE_CONTEXT_COMPRESS_START,
                    "上下文接近模型窗口，正在压缩较早历史。",
                    new ContextCompressionEventData(snapshot.estimatedTokens(), snapshot.thresholdTokens(),
                            memoryMessages.size(), 0, false)));
            ContextCompressionResult result = contextCompressionAgent.compress(
                    conversationId, memoryMessages, getAnalysisBudget(request));
            if (result.compressedMessages().isEmpty()) {
                emit(eventConsumer, event(conversationId, EVENT_TYPE_CONTEXT_COMPRESS_FAILED,
                        "上下文压缩未生成有效结果，继续使用原始上下文。",
                        new ContextCompressionEventData(snapshot.estimatedTokens(), snapshot.thresholdTokens(),
                                memoryMessages.size(), 0, result.modelInvoked())));
                return request;
            }
            chatMemoryRepository.saveAll(conversationId, result.compressedMessages());
            emit(eventConsumer, event(conversationId, EVENT_TYPE_CONTEXT_COMPRESS_DONE,
                    "上下文压缩完成，已准备新的对话记忆。",
                    new ContextCompressionEventData(snapshot.estimatedTokens(), snapshot.thresholdTokens(),
                            memoryMessages.size(), result.compressedMessages().size(), result.modelInvoked())));
            log.info("AI 上下文已压缩，conversationId={}, estimatedTokens={}, thresholdTokens={}, beforeMessages={}, afterMessages={}, modelInvoked={}",
                    conversationId, snapshot.estimatedTokens(), snapshot.thresholdTokens(), memoryMessages.size(),
                    result.compressedMessages().size(), result.modelInvoked());
            return request;
        } catch (RuntimeException ex) {
            emit(eventConsumer, event(conversationId, EVENT_TYPE_CONTEXT_COMPRESS_FAILED,
                    "上下文压缩失败，继续使用原始上下文。", ex.getMessage()));
            if (isBudgetFailure(ex)) {
                throw ex;
            }
            log.warn("AI 上下文压缩失败，conversationId={}", conversationId, ex);
            return request;
        }
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public int getOrder() {
        return ORDER_BEFORE_CHAT_MEMORY;
    }

    private String getConversationId(ChatClientRequest request) {
        Object value = request.context().get(ChatMemory.CONVERSATION_ID);
        return value == null ? "" : value.toString();
    }

    @SuppressWarnings("unchecked")
    private Consumer<AmazonSelectionReactEvent> getEventConsumer(ChatClientRequest request) {
        Object value = request.context().get(EVENT_CONSUMER_CONTEXT_KEY);
        if (value instanceof Consumer<?>) {
            return (Consumer<AmazonSelectionReactEvent>) value;
        }
        return null;
    }

    private CurationAnalysisBudget getAnalysisBudget(ChatClientRequest request) {
        Object value = request.context().get(ANALYSIS_BUDGET_CONTEXT_KEY);
        return value instanceof CurationAnalysisBudget budget
                ? budget
                : CurationAnalysisBudget.unlimited();
    }

    private void emit(Consumer<AmazonSelectionReactEvent> eventConsumer, AmazonSelectionReactEvent event) {
        if (eventConsumer != null) {
            try {
                eventConsumer.accept(event);
            } catch (RuntimeException ex) {
                if (isBudgetFailure(ex)) {
                    throw ex;
                }
                log.warn("AI 上下文压缩事件发送失败，conversationId={}, eventType={}",
                        event.getConversationId(), event.getEventType(), ex);
            }
        }
    }

    private boolean isBudgetFailure(RuntimeException exception) {
        if (!(exception instanceof AmazonSelectionAnalysisException analysisException)) {
            return false;
        }
        return switch (analysisException.getErrorCode()) {
            case SHEET_LIMIT_EXCEEDED, MODEL_CALL_LIMIT_EXCEEDED, EXECUTION_DURATION_EXCEEDED -> true;
            default -> false;
        };
    }

    private AmazonSelectionReactEvent event(String conversationId, String eventType, String message, Object data) {
        return AmazonSelectionReactEvent.builder()
                .eventType(eventType)
                .conversationId(conversationId)
                .phase(PHASE_CONTEXT)
                .message(message)
                .data(data)
                .build();
    }

    private record ContextCompressionEventData(int estimatedTokens, int thresholdTokens, int beforeMessages,
            int afterMessages, boolean modelInvoked) {
    }
}
