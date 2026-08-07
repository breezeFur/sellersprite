package cyou.yuanbaomao.graphlearning.lesson.interrupt;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 动态人工介入节点：没有反馈时返回 InterruptionMetadata，有反馈时继续执行。
 */
public final class DynamicReviewAction implements AsyncNodeAction, InterruptableAction {

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state) {
        return CompletableFuture.completedFuture(Map.of(GraphStateKeys.TRACE, List.of("dynamicReview")));
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        if (config.metadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY).isPresent()) {
            return Optional.empty();
        }
        return Optional.of(InterruptionMetadata.builder(nodeId, state).build());
    }
}
