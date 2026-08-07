package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cyou.yuanbaomao.sellersprite.ai.research.curation.context.ModelInputCompactor;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonProductAnalysisTools;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.AmazonSelectionToolContext;
import cyou.yuanbaomao.sellersprite.ai.research.curation.tool.ResearchRawDataTools;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

class FollowUpAnswerAgentTest {

    @Test
    void shouldBePrototypeAgentWithConciseConversationBoundary() {
        FollowUpAnswerAgent agent = agent();
        Scope scope = FollowUpAnswerAgent.class.getAnnotation(Scope.class);

        assertThat(scope).isNotNull();
        assertThat(scope.value()).isEqualTo(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
        assertThat(agent.getName()).isEqualTo("FollowUpAnswerAgent");
        assertThat(agent.getSystemPrompt())
                .contains("同一会话中的正式报告、历史问答和当前问题")
                .contains("最多 3 点关键依据")
                .contains("约 200-500 个中文字符")
                .contains("用户明确要求详细说明")
                .contains("证据不足时明确说明缺口")
                .contains("禁止重写、复述或重新排版整份市场调研报告");
        assertThat(agent.getNextStepPrompt())
                .contains("围绕当前追问直接作答")
                .contains("不要重写整份报告");
    }

    @Test
    void shouldBuildFollowUpPromptWithoutEmbeddingFullEvidence() {
        FollowUpAnswerAgent agent = agent();
        String prompt = agent.buildFinalSummaryPrompt(
                "follow-up-run-1",
                new ProductWorkbook(),
                "不应进入提示词的完整十二表 evidence：ASIN-B0SECRET",
                "这个市场适合低预算进入吗？",
                "items[].price：[证据已引用]");

        assertThat(prompt)
                .contains("follow-up-run-1")
                .contains("这个市场适合低预算进入吗？")
                .contains("items[].price：[证据已引用]")
                .contains("最多 3 点关键依据")
                .contains("约 200-500 个中文字符")
                .contains("用户明确要求详细说明")
                .contains("证据不足时明确指出缺口")
                .contains("禁止重写、复述或重新排版整份报告")
                .doesNotContain("ASIN-B0SECRET")
                .doesNotContain("完整十二表 evidence");
    }

    @SuppressWarnings("unchecked")
    private FollowUpAnswerAgent agent() {
        ObjectProvider<ChatClient.Builder> chatClientBuilderProvider = mock(ObjectProvider.class);
        ObjectProvider<ToolCallbackProvider> toolCallbackProviderProvider = mock(ObjectProvider.class);
        return new FollowUpAnswerAgent(
                chatClientBuilderProvider,
                toolCallbackProviderProvider,
                new AmazonSelectionToolContext(),
                mock(AmazonProductAnalysisTools.class),
                mock(ResearchRawDataTools.class),
                List.of(),
                mock(ModelInputCompactor.class));
    }
}
