package cyou.yuanbaomao.graphlearning.lesson.basic;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.graphlearning.common.GraphBuilderFactory;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
import static org.assertj.core.api.Assertions.assertThat;

class BasicGraphLessonTest {

    @Test
    void shouldExecuteLinearGraphAndReplaceOrAppendState() throws Exception {
        StateGraph graph = GraphBuilderFactory.newGraph("basic-lesson", () -> Map.of(
                GraphStateKeys.INPUT, new ReplaceStrategy(),
                GraphStateKeys.PROCESSED, new ReplaceStrategy(),
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        graph.addNode("input", node_async(state -> Map.of(GraphStateKeys.TRACE, List.of("input"))));
        graph.addNode("transform", node_async(state -> Map.of(
                GraphStateKeys.PROCESSED,
                state.value(GraphStateKeys.INPUT, String.class).orElseThrow().trim().toUpperCase(),
                GraphStateKeys.TRACE, List.of("transform"))));
        graph.addNode("output", node_async(state -> Map.of(
                GraphStateKeys.RESULT, state.value(GraphStateKeys.PROCESSED, String.class).orElseThrow() + "!",
                GraphStateKeys.TRACE, List.of("output"))));
        graph.addEdge(StateGraph.START, "input");
        graph.addEdge("input", "transform");
        graph.addEdge("transform", "output");
        graph.addEdge("output", StateGraph.END);

        CompiledGraph compiled = graph.compile();
        var state = compiled.invoke(Map.of(GraphStateKeys.INPUT, " hello ")).orElseThrow();

        assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains("HELLO!");
        assertThat(state.value(GraphStateKeys.TRACE, List.class).orElseThrow())
                .containsExactly("input", "transform", "output");
    }
}
