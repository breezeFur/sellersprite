package cyou.yuanbaomao.graphlearning.lesson.interrupt;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicInterruptLessonTest {

    @Test
    void shouldExposeInterruptionMetadataAndContinueAfterFeedback() throws Exception {
        var graph = DynamicInterruptWorkflow.build();
        RunnableConfig config = DynamicInterruptWorkflow.config("dynamic-interrupt-thread-1");

        NodeOutput interruption = graph.stream(Map.of(), config).blockLast();

        assertThat(interruption).isNotNull();
        assertThat(interruption.node()).isEqualTo(DynamicInterruptWorkflow.REVIEW);

        RunnableConfig resume = RunnableConfig.builder(config)
                .resume()
                .addStateUpdate(Map.of(GraphStateKeys.APPROVED, true))
                .build();
        NodeOutput result = graph.stream(Map.of(), resume).blockLast();

        assertThat(result.state().value(GraphStateKeys.RESULT, String.class)).contains("approved");
    }
}
