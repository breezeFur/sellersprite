package cyou.yuanbaomao.graphlearning.lesson.interrupt;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.graphlearning.common.GraphBuilderFactory;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 使用编译期 interruptBefore 演示人工审核暂停和恢复。
 */
public final class InterruptWorkflow {

    public static final String GENERATE = "generate";
    public static final String REVIEW = "review";
    public static final String FINISH = "finish";

    private InterruptWorkflow() {
    }

    public static CompiledGraph build() throws GraphStateException {
        StateGraph graph = GraphBuilderFactory.newGraph("interrupt-workflow", () -> Map.of(
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.APPROVED, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        graph.addNode(GENERATE, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(GENERATE))));
        graph.addNode(REVIEW, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(REVIEW))));
        graph.addNode(FINISH, node_async(state -> Map.of(
                GraphStateKeys.RESULT, Boolean.TRUE.equals(state.value(GraphStateKeys.APPROVED, Boolean.class)
                        .orElse(false)) ? "approved" : "rejected",
                GraphStateKeys.TRACE, List.of(FINISH))));
        graph.addEdge(StateGraph.START, GENERATE);
        graph.addEdge(GENERATE, REVIEW);
        graph.addEdge(REVIEW, FINISH);
        graph.addEdge(FINISH, StateGraph.END);
        return graph.compile(com.alibaba.cloud.ai.graph.CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                .interruptBefore(REVIEW)
                .releaseThread(false)
                .build());
    }

    public static RunnableConfig config(String threadId) {
        return RunnableConfig.builder().threadId(threadId).build();
    }
}
