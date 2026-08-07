package cyou.yuanbaomao.graphlearning.lesson.parallel;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeAggregationStrategy;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.graphlearning.common.GraphBuilderFactory;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 三分支扇出、单节点汇聚的并行示例。
 */
public final class ParallelWorkflow {

    public static final String PREPARE = "prepare";
    public static final String TASK_A = "taskA";
    public static final String TASK_B = "taskB";
    public static final String TASK_C = "taskC";
    public static final String AGGREGATE = "aggregate";

    private ParallelWorkflow() {
    }

    public static CompiledGraph build() throws GraphStateException {
        StateGraph graph = GraphBuilderFactory.newGraph("parallel-workflow", () -> Map.of(
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        graph.addNode(PREPARE, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(PREPARE))));
        graph.addNode(TASK_A, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(TASK_A))));
        graph.addNode(TASK_B, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(TASK_B))));
        graph.addNode(TASK_C, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(TASK_C))));
        graph.addNode(AGGREGATE, node_async(state -> Map.of(
                GraphStateKeys.RESULT, "aggregated",
                GraphStateKeys.TRACE, List.of(AGGREGATE))));
        graph.addEdge(StateGraph.START, PREPARE);
        graph.addEdge(PREPARE, List.of(TASK_A, TASK_B, TASK_C));
        graph.addEdge(List.of(TASK_A, TASK_B, TASK_C), AGGREGATE);
        graph.addEdge(AGGREGATE, StateGraph.END);
        return graph.compile();
    }

    public static RunnableConfig config(String threadId, ExecutorService executor) {
        return RunnableConfig.builder()
                .threadId(threadId)
                .defaultParallelExecutor(executor)
                .addParallelNodeAggregationStrategy(AGGREGATE, NodeAggregationStrategy.ALL_OF)
                .build();
    }
}
