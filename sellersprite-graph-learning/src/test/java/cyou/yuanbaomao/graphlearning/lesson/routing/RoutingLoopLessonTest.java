package cyou.yuanbaomao.graphlearning.lesson.routing;

import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingLoopLessonTest {

    @Test
    void shouldApproveWhenRepairRaisesScore() throws Exception {
        var state = ReviewWorkflow.build().invoke(ReviewWorkflow.initialState(20)).orElseThrow();

        assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains(ReviewWorkflow.PASS);
        assertThat(state.value(GraphStateKeys.ATTEMPT, Integer.class)).contains(2);
        assertThat(state.value(GraphStateKeys.TRACE, java.util.List.class).orElseThrow())
                .containsExactly("inspect", "repair", "inspect", "repair", "inspect", "finish");
    }

    @Test
    void shouldRejectWhenRepairLimitIsReached() throws Exception {
        var state = ReviewWorkflow.build().invoke(ReviewWorkflow.initialState(-200)).orElseThrow();

        assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains(ReviewWorkflow.FAIL);
        assertThat(state.value(GraphStateKeys.ATTEMPT, Integer.class)).contains(3);
    }

    @Test
    void shouldFinishImmediatelyWhenScorePasses() throws Exception {
        var state = ReviewWorkflow.build().invoke(ReviewWorkflow.initialState(60)).orElseThrow();

        assertThat(state.value(GraphStateKeys.RESULT, String.class)).contains(ReviewWorkflow.PASS);
        assertThat(state.value(GraphStateKeys.ATTEMPT, Integer.class)).contains(0);
    }
}
