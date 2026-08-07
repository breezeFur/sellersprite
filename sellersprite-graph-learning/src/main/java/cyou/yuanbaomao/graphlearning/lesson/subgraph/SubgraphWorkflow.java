package cyou.yuanbaomao.graphlearning.lesson.subgraph;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.graphlearning.common.GraphBuilderFactory;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 将校验流程编译为子图，再作为父图节点组合。
 */
public final class SubgraphWorkflow {

    public static final String VALIDATION = "validation";
    public static final String TASK = "task";
    public static final String FINISH = "finish";

    private SubgraphWorkflow() {
    }

    public static CompiledGraph build() throws GraphStateException {
        CompiledGraph validationSubgraph = buildValidationSubgraph();
        StateGraph parent = GraphBuilderFactory.newGraph("parent-workflow", () -> Map.of(
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        parent.addNode(VALIDATION, validationSubgraph);
        parent.addNode(TASK, node_async(state -> Map.of(GraphStateKeys.TRACE, List.of(TASK))));
        parent.addNode(FINISH, node_async(state -> Map.of(
                GraphStateKeys.RESULT, "subgraph-completed",
                GraphStateKeys.TRACE, List.of(FINISH))));
        parent.addEdge(StateGraph.START, VALIDATION);
        parent.addEdge(VALIDATION, TASK);
        parent.addEdge(TASK, FINISH);
        parent.addEdge(FINISH, StateGraph.END);
        return parent.compile();
    }

    private static CompiledGraph buildValidationSubgraph() throws GraphStateException {
        StateGraph validation = GraphBuilderFactory.newGraph("validation-subgraph", () -> Map.of(
                GraphStateKeys.TRACE, new AppendStrategy()));
        validation.addNode("validateInput", node_async(state -> Map.of(
                GraphStateKeys.TRACE, List.of("validateInput"))));
        validation.addNode("normalize", node_async(state -> Map.of(
                GraphStateKeys.TRACE, List.of("normalize"))));
        validation.addEdge(StateGraph.START, "validateInput");
        validation.addEdge("validateInput", "normalize");
        validation.addEdge("normalize", StateGraph.END);
        return validation.compile();
    }
}
