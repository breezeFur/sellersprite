package cyou.yuanbaomao.graphlearning.common;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 统一创建教学 Graph，隐藏重复的 StateGraph 初始化代码。
 */
public final class GraphBuilderFactory {

    private GraphBuilderFactory() {
    }

    public static StateGraph newGraph(String name,
            Supplier<Map<String, KeyStrategy>> stateStrategies) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(stateStrategies, "stateStrategies cannot be null");
        return new StateGraph(name, stateStrategies::get);
    }

    public static CompiledGraph compile(StateGraph graph) throws GraphStateException {
        return Objects.requireNonNull(graph, "graph cannot be null").compile();
    }
}
