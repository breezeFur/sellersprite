package cyou.yuanbaomao.graphlearning.lesson.routing;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.graphlearning.common.GraphBuilderFactory;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 通用审核、修复和结束流程，用于学习条件边与循环。
 */
public final class ReviewWorkflow {

    public static final String PASS = "PASS";
    public static final String RETRY = "RETRY";
    public static final String FAIL = "FAIL";
    public static final String INSPECT = "inspect";
    public static final String REPAIR = "repair";
    public static final String FINISH = "finish";

    private static final int PASS_SCORE = 60;
    private static final int MAX_ATTEMPTS = 3;
    private static final int SCORE_STEP = 20;

    private ReviewWorkflow() {
    }

    public static CompiledGraph build() throws GraphStateException {
        StateGraph graph = GraphBuilderFactory.newGraph("review-workflow", ReviewWorkflow::strategies);
        graph.addNode(INSPECT, node_async(ReviewWorkflow::inspect));
        graph.addNode(REPAIR, node_async(ReviewWorkflow::repair));
        graph.addNode(FINISH, node_async(ReviewWorkflow::finish));
        graph.addEdge(StateGraph.START, INSPECT);
        graph.addConditionalEdges(INSPECT, AsyncEdgeAction.edge_async(ReviewWorkflow::route),
                Map.of(PASS, FINISH, RETRY, REPAIR, FAIL, FINISH));
        graph.addEdge(REPAIR, INSPECT);
        graph.addEdge(FINISH, StateGraph.END);
        return graph.compile();
    }

    public static Map<String, KeyStrategy> strategies() {
        return Map.of(
                GraphStateKeys.SCORE, new ReplaceStrategy(),
                GraphStateKeys.ATTEMPT, new ReplaceStrategy(),
                GraphStateKeys.DECISION, new ReplaceStrategy(),
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy());
    }

    public static Map<String, Object> initialState(int score) {
        return Map.of(GraphStateKeys.SCORE, score, GraphStateKeys.ATTEMPT, 0);
    }

    private static Map<String, Object> inspect(OverAllState state) {
        int score = requiredInt(state, GraphStateKeys.SCORE);
        int attempts = state.value(GraphStateKeys.ATTEMPT, Integer.class).orElse(0);
        String decision = score >= PASS_SCORE ? PASS : attempts >= MAX_ATTEMPTS ? FAIL : RETRY;
        return Map.of(GraphStateKeys.DECISION, decision,
                GraphStateKeys.TRACE, List.of(INSPECT));
    }

    private static Map<String, Object> repair(OverAllState state) {
        int score = requiredInt(state, GraphStateKeys.SCORE);
        int attempts = state.value(GraphStateKeys.ATTEMPT, Integer.class).orElse(0);
        return Map.of(GraphStateKeys.SCORE, score + SCORE_STEP,
                GraphStateKeys.ATTEMPT, attempts + 1,
                GraphStateKeys.TRACE, List.of(REPAIR));
    }

    private static Map<String, Object> finish(OverAllState state) {
        String decision = state.value(GraphStateKeys.DECISION, String.class)
                .orElseThrow(() -> new IllegalStateException("缺少审核决定"));
        return Map.of(GraphStateKeys.RESULT, decision,
                GraphStateKeys.TRACE, List.of(FINISH));
    }

    private static String route(OverAllState state) {
        return state.value(GraphStateKeys.DECISION, String.class)
                .orElseThrow(() -> new IllegalStateException("条件边缺少 decision"));
    }

    private static int requiredInt(OverAllState state, String key) {
        return state.value(key, Integer.class)
                .orElseThrow(() -> new IllegalStateException("缺少整数状态: " + key));
    }
}
