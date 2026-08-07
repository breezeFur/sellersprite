package cyou.yuanbaomao.graphlearning.lesson.agent;

import java.util.Objects;
import java.util.function.Function;

/**
 * 无 API Key 的确定性 Agent，用于 Graph 测试和教学。
 */
public final class FakeLearningAiAgent implements LearningAiAgent {

    private final Function<AiAgentRequest, AiAgentResult> handler;

    public FakeLearningAiAgent(Function<AiAgentRequest, AiAgentResult> handler) {
        this.handler = Objects.requireNonNull(handler, "handler cannot be null");
    }

    public static FakeLearningAiAgent approved() {
        return new FakeLearningAiAgent(request -> new AiAgentResult("approved", 0.98));
    }

    @Override
    public AiAgentResult execute(AiAgentRequest request) {
        return handler.apply(request);
    }
}
