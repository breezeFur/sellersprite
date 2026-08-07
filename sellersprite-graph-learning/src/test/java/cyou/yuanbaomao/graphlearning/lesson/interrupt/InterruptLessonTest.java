package cyou.yuanbaomao.graphlearning.lesson.interrupt;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InterruptLessonTest {

    @Test
    void shouldPauseBeforeReviewAndResumeWithHumanState() throws Exception {
        var graph = InterruptWorkflow.build();
        RunnableConfig initialConfig = InterruptWorkflow.config("interrupt-thread-1");

        NodeOutput interruption = graph.stream(Map.of(), initialConfig).blockLast();

        assertThat(interruption).isNotNull();
        assertThat(interruption.node()).isEqualTo(InterruptWorkflow.GENERATE);
        assertThat(graph.lastStateOf(initialConfig)).get()
                .extracting(snapshot -> snapshot.next())
                .isEqualTo(InterruptWorkflow.REVIEW);

        RunnableConfig resumeConfig = RunnableConfig.builder(initialConfig)
                .resume()
                .build();
        NodeOutput result = graph.stream(Map.of(GraphStateKeys.APPROVED, true), resumeConfig).blockLast();

        assertThat(result.state().value(GraphStateKeys.RESULT, String.class)).contains("approved");
    }
}
