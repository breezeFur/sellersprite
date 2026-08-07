package cyou.yuanbaomao.graphlearning.lesson.agent;

import org.springframework.ai.chat.client.ChatClient;

import java.util.Objects;

/**
 * 可选的真实模型适配器；基础课程使用 FakeLearningAiAgent，不需要模型配置。
 */
public final class SpringAiLearningAgent implements LearningAiAgent {

    private final ChatClient chatClient;

    public SpringAiLearningAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder cannot be null").build();
    }

    @Override
    public AiAgentResult execute(AiAgentRequest request) {
        AiAgentResult result = chatClient.prompt()
                .system("只输出 JSON，字段为 classification 和 confidence。")
                .user(request.prompt())
                .call()
                .entity(AiAgentResult.class);
        if (result == null) {
            throw new IllegalStateException("AI 未返回内容");
        }
        return result;
    }
}
