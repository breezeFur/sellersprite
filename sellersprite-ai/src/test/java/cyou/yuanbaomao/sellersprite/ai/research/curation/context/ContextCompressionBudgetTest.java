package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;

class ContextCompressionBudgetTest {

    private static final String CONVERSATION_ID = "conversation-1";

    @Test
    @SuppressWarnings("unchecked")
    void shouldCountCompressionModelCallAgainstSharedBudget() {
        ObjectProvider<ChatClient.Builder> builderProvider = mock(ObjectProvider.class);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        when(builderProvider.getIfAvailable()).thenReturn(builder);
        when(builder.clone()).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("压缩后的事实摘要");
        ContextCompressionAgent compressionAgent = new ContextCompressionAgent(
                builderProvider, new CurationAnalysisProperties());
        AtomicInteger persistedCalls = new AtomicInteger();
        CurationAnalysisBudget budget = new CurationAnalysisBudget(
                10, 16, 60_000L, persistedCalls::incrementAndGet);

        ContextCompressionResult result = compressionAgent.compress(
                CONVERSATION_ID, messages(), budget);

        assertThat(result.modelInvoked()).isTrue();
        assertThat(persistedCalls).hasValue(1);
        verify(responseSpec).content();
    }

    @Test
    void shouldNotSwallowBudgetFailureInCompressionAdvisor() {
        ChatMemoryRepository memoryRepository = mock(ChatMemoryRepository.class);
        ContextWindowEstimator estimator = mock(ContextWindowEstimator.class);
        ContextCompressionAgent compressionAgent = mock(ContextCompressionAgent.class);
        CurationAnalysisProperties properties = new CurationAnalysisProperties();
        ContextCompressionAdvisor advisor = new ContextCompressionAdvisor(
                memoryRepository, estimator, compressionAgent, properties);
        CurationAnalysisBudget budget = new CurationAnalysisBudget(10, 1, 60_000L, () -> {
        });
        AmazonSelectionAnalysisException limitFailure = new AmazonSelectionAnalysisException(
                AmazonSelectionAnalysisException.ErrorCode.MODEL_CALL_LIMIT_EXCEEDED,
                "模型调用次数已达到分析上限 1");
        List<Message> messages = messages();
        when(memoryRepository.findByConversationId(CONVERSATION_ID)).thenReturn(messages);
        when(estimator.snapshot(eq(messages), org.mockito.ArgumentMatchers.anyList(),
                eq(properties.getContextCompression().getMaxContextTokens()),
                eq(properties.getContextCompression().getTriggerRatio())))
                .thenReturn(new ContextWindowSnapshot(120_000, 128_000, 102_400, 0.8D, true));
        when(compressionAgent.compress(CONVERSATION_ID, messages, budget)).thenThrow(limitFailure);
        ChatClientRequest request = new ChatClientRequest(
                new Prompt(List.of(new UserMessage("继续分析"))),
                Map.of(
                        ChatMemory.CONVERSATION_ID, CONVERSATION_ID,
                        ContextCompressionAdvisor.ANALYSIS_BUDGET_CONTEXT_KEY, budget));

        assertThatThrownBy(() -> advisor.before(request, mock(AdvisorChain.class)))
                .isSameAs(limitFailure);
        verify(compressionAgent).compress(CONVERSATION_ID, messages, budget);
    }

    private List<Message> messages() {
        return List.of(
                new UserMessage("问题1"),
                new UserMessage("回答1"),
                new UserMessage("问题2"),
                new UserMessage("回答2"),
                new UserMessage("问题3"),
                new UserMessage("回答3"),
                new UserMessage("问题4"),
                new UserMessage("回答4"));
    }
}
