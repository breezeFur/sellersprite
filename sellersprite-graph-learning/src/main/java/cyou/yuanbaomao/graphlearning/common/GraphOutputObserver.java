package cyou.yuanbaomao.graphlearning.common;

import com.alibaba.cloud.ai.graph.NodeOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 收集 stream 输出，避免课程测试依赖控制台日志。
 */
public final class GraphOutputObserver implements Consumer<NodeOutput> {

    private final List<NodeOutput> outputs = new CopyOnWriteArrayList<>();

    @Override
    public void accept(NodeOutput output) {
        outputs.add(output);
    }

    public List<NodeOutput> outputs() {
        return List.copyOf(outputs);
    }

    public List<String> nodeNames() {
        return outputs.stream().map(NodeOutput::node).toList();
    }

    public GraphExecutionResult result() {
        if (outputs.isEmpty()) {
            throw new IllegalStateException("Graph 尚未产生任何输出");
        }
        NodeOutput last = outputs.get(outputs.size() - 1);
        return new GraphExecutionResult(new ArrayList<>(nodeNames()), last.state().data());
    }
}
