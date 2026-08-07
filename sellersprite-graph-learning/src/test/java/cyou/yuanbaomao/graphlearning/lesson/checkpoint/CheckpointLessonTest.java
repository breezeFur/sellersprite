package cyou.yuanbaomao.graphlearning.lesson.checkpoint;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckpointLessonTest {

    @Test
    void shouldResumeFailedNodeWithoutRepeatingCompletedNode() throws Exception {
        AtomicInteger unstableAttempts = new AtomicInteger();
        var graph = CheckpointWorkflow.build(unstableAttempts);
        RunnableConfig initialConfig = CheckpointWorkflow.config("checkpoint-thread-1");
        Map<String, Object> input = Map.of(GraphStateKeys.INPUT, "hello");

        assertThatThrownBy(() -> graph.stream(input, initialConfig).blockLast())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("模拟一次性任务失败");
        assertThat(graph.lastStateOf(initialConfig)).isPresent();

        RunnableConfig resumeConfig = RunnableConfig.builder(initialConfig).resume().build();
        NodeOutput output = graph.stream(input, resumeConfig).blockLast();

        assertThat(output).isNotNull();
        assertThat(unstableAttempts).hasValue(2);
        assertThat(graph.lastStateOf(initialConfig)).isPresent();
    }
}
