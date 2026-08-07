package cyou.yuanbaomao.graphlearning.lesson.subgraph;

import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubgraphLessonTest {

    @Test
    void shouldComposeCompiledValidationSubgraphIntoParentGraph() throws Exception {
        var state = SubgraphWorkflow.build().invoke(Map.of()).orElseThrow();

        assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains("subgraph-completed");
        assertThat(state.value(GraphStateKeys.TRACE, List.class).orElseThrow())
                .contains("validateInput", "normalize", SubgraphWorkflow.TASK, SubgraphWorkflow.FINISH);
    }
}
