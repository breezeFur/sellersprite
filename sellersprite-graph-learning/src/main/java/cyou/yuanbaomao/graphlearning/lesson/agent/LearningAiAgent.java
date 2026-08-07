package cyou.yuanbaomao.graphlearning.lesson.agent;

/**
 * Graph 与 AI Agent 之间的最小边界。
 */
@FunctionalInterface
public interface LearningAiAgent {

    AiAgentResult execute(AiAgentRequest request);
}
