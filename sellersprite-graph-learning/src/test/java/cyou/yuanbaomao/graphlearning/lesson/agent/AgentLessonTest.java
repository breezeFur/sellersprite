package cyou.yuanbaomao.graphlearning.lesson.agent;

import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentLessonTest {

    @Test
    void shouldUseFakeAgentWithoutApiKey() throws Exception {
        var graph = AgentReviewWorkflow.build(FakeLearningAiAgent.approved());
        var state = graph.invoke(Map.of(GraphStateKeys.INPUT, "safe text")).orElseThrow();

        assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains(AgentReviewWorkflow.APPROVED);
        assertThat(state.value(GraphStateKeys.CONFIDENCE, Double.class)).contains(0.98);
    }

    @Test
    void shouldRejectInvalidStructuredAgentOutputBeforeRouting() throws Exception {
        var agent = new FakeLearningAiAgent(request -> new AiAgentResult("", 1.2));
        var graph = AgentReviewWorkflow.build(agent);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> graph.invoke(Map.of(GraphStateKeys.INPUT, "invalid")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI 返回的结构化结果非法");
    }
}
