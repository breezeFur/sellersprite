package cyou.yuanbaomao.graphlearning.lesson.agent;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.graphlearning.common.GraphBuilderFactory;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * AI 只负责生成结构化结果，Graph 负责判断和路由。
 */
public final class AgentReviewWorkflow {

    public static final String CLASSIFY = "classify";
    public static final String SCORE = "score";
    public static final String FINISH = "finish";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private AgentReviewWorkflow() {
    }

    public static CompiledGraph build(LearningAiAgent agent) throws GraphStateException {
        StateGraph graph = GraphBuilderFactory.newGraph("agent-review-workflow", () -> Map.of(
                GraphStateKeys.INPUT, new ReplaceStrategy(),
                GraphStateKeys.CLASSIFICATION, new ReplaceStrategy(),
                GraphStateKeys.CONFIDENCE, new ReplaceStrategy(),
                GraphStateKeys.DECISION, new ReplaceStrategy(),
                GraphStateKeys.RESULT, new ReplaceStrategy(),
                GraphStateKeys.TRACE, new AppendStrategy()));
        graph.addNode(CLASSIFY, node_async(state -> classify(state, agent)));
        graph.addNode(SCORE, node_async(AgentReviewWorkflow::score));
        graph.addNode(FINISH, node_async(AgentReviewWorkflow::finish));
        graph.addEdge(StateGraph.START, CLASSIFY);
        graph.addEdge(CLASSIFY, SCORE);
        graph.addConditionalEdges(SCORE, edge_async(AgentReviewWorkflow::route),
                Map.of(APPROVED, FINISH, REJECTED, FINISH));
        graph.addEdge(FINISH, StateGraph.END);
        return graph.compile();
    }

    private static Map<String, Object> classify(OverAllState state, LearningAiAgent agent) {
        String input = state.value(GraphStateKeys.INPUT, String.class)
                .orElseThrow(() -> new IllegalStateException("缺少 AI 输入"));
        AiAgentResult result = agent.execute(new AiAgentRequest(input));
        if (result.classification() == null || result.classification().isBlank()
                || result.confidence() < 0 || result.confidence() > 1) {
            throw new IllegalStateException("AI 返回的结构化结果非法");
        }
        return Map.of(GraphStateKeys.CLASSIFICATION, result.classification(),
                GraphStateKeys.CONFIDENCE, result.confidence(),
                GraphStateKeys.TRACE, List.of(CLASSIFY));
    }

    private static Map<String, Object> score(OverAllState state) {
        double confidence = state.value(GraphStateKeys.CONFIDENCE, Double.class)
                .orElseThrow(() -> new IllegalStateException("缺少 AI 置信度"));
        return Map.of(GraphStateKeys.DECISION, confidence >= 0.8 ? APPROVED : REJECTED,
                GraphStateKeys.TRACE, List.of(SCORE));
    }

    private static Map<String, Object> finish(OverAllState state) {
        return Map.of(GraphStateKeys.RESULT,
                state.value(GraphStateKeys.DECISION, String.class).orElseThrow(),
                GraphStateKeys.TRACE, List.of(FINISH));
    }

    private static String route(OverAllState state) {
        return state.value(GraphStateKeys.DECISION, String.class).orElseThrow();
    }
}
