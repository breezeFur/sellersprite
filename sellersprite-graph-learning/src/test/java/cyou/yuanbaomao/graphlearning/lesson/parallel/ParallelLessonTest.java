package cyou.yuanbaomao.graphlearning.lesson.parallel;

import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelLessonTest {

    @Test
    void shouldRunThreeBranchesAndAggregateAtConvergenceNode() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            var graph = ParallelWorkflow.build();
            var state = graph.invoke(Map.of(), ParallelWorkflow.config("parallel-thread-1", executor)).orElseThrow();

            assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains("aggregated");
            assertThat(state.value(GraphStateKeys.TRACE, List.class).orElseThrow())
                    .contains(ParallelWorkflow.TASK_A, ParallelWorkflow.TASK_B, ParallelWorkflow.TASK_C,
                            ParallelWorkflow.AGGREGATE);
        }
        finally {
            executor.shutdownNow();
        }
    }
}
