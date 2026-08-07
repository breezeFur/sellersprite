package cyou.yuanbaomao.graphlearning.lesson.checkpoint;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 可注入一次性失败的工作流，用于验证 Checkpoint 恢复位置。
 */
public final class CheckpointWorkflow {

    public static final String PREPARE = "prepare";
    public static final String UNSTABLE_TASK = "unstableTask";
    public static final String FINISH = "finish";

    private CheckpointWorkflow() {
    }

    public static CompiledGraph build(AtomicInteger unstableAttempts) throws GraphStateException {
        MemorySaver saver = new MemorySaver();
        StateGraph graph = GraphBuilderFactory.newGraph("checkpoint-workflow", () -> Map.of(
                GraphStateKeys.INPUT, new ReplaceStrategy(),
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        graph.addNode(PREPARE, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(PREPARE))));
        graph.addNode(UNSTABLE_TASK, node_async(state -> unstableTask(state, unstableAttempts)));
        graph.addNode(FINISH, node_async(state -> Map.of(
                GraphStateKeys.RESULT, "completed",
                GraphStateKeys.TRACE, List.of(FINISH))));
        graph.addEdge(StateGraph.START, PREPARE);
        graph.addEdge(PREPARE, UNSTABLE_TASK);
        graph.addEdge(UNSTABLE_TASK, FINISH);
        graph.addEdge(FINISH, StateGraph.END);
        return graph.compile(com.alibaba.cloud.ai.graph.CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(saver).build())
                .releaseThread(false)
                .build());
    }

    public static RunnableConfig config(String threadId) {
        return RunnableConfig.builder().threadId(threadId).build();
    }

    private static Map<String, Object> unstableTask(OverAllState state, AtomicInteger attempts) {
        if (attempts.incrementAndGet() == 1) {
            throw new IllegalStateException("模拟一次性任务失败");
        }
        return Map.of(GraphStateKeys.TRACE, List.of(UNSTABLE_TASK));
    }
}
