package cyou.yuanbaomao.graphlearning.common;

import java.util.List;
import java.util.Map;

/**
 * 将 Graph 最终状态和执行轨迹收敛成便于断言的结果对象。
 */
public record GraphExecutionResult(List<String> nodeNames, Map<String, Object> state) {

    public GraphExecutionResult {
        nodeNames = List.copyOf(nodeNames);
        state = Map.copyOf(state);
    }
}
