package cyou.yuanbaomao.graphlearning.lesson.interrupt;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
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
 * 动态中断工作流，展示 InterruptableAction 与人工反馈元数据。
 */
public final class DynamicInterruptWorkflow {

    public static final String PREPARE = "prepare";
    public static final String REVIEW = "dynamicReview";
    public static final String FINISH = "finish";

    private DynamicInterruptWorkflow() {
    }

    public static CompiledGraph build() throws GraphStateException {
        StateGraph graph = GraphBuilderFactory.newGraph("dynamic-interrupt-workflow", () -> Map.of(
                GraphStateKeys.APPROVED, new ReplaceStrategy(),
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        graph.addNode(PREPARE, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(PREPARE))));
        graph.addNode(REVIEW, AsyncNodeActionWithConfig.of(new DynamicReviewAction()));
        graph.addNode(FINISH, node_async(state -> Map.of(
                GraphStateKeys.RESULT,
                Boolean.TRUE.equals(state.value(GraphStateKeys.APPROVED, Boolean.class).orElse(false))
                        ? "approved" : "rejected",
                GraphStateKeys.TRACE, List.of(FINISH))));
        graph.addEdge(StateGraph.START, PREPARE);
        graph.addEdge(PREPARE, REVIEW);
        graph.addEdge(REVIEW, FINISH);
        graph.addEdge(FINISH, StateGraph.END);
        return graph.compile(com.alibaba.cloud.ai.graph.CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                .releaseThread(false)
                .build());
    }

    public static RunnableConfig config(String threadId) {
        return RunnableConfig.builder().threadId(threadId).build();
    }
}
