package cyou.yuanbaomao.graphlearning.lesson.streaming;

import cyou.yuanbaomao.graphlearning.common.GraphOutputObserver;
import cyou.yuanbaomao.graphlearning.common.GraphStateKeys;
import cyou.yuanbaomao.graphlearning.lesson.routing.ReviewWorkflow;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StreamingLessonTest {

    @Test
    void shouldCollectNodeOutputsWithoutUsingConsoleLogs() throws Exception {
        GraphOutputObserver observer = new GraphOutputObserver();

        ReviewWorkflow.build()
                .stream(ReviewWorkflow.initialState(60))
                .doOnNext(observer)
                .blockLast();

        assertThat(observer.nodeNames()).containsExactly("__START__", "inspect", "finish", "__END__");
        assertThat(observer.result().state().get(GraphStateKeys.RESULT)).isEqualTo(ReviewWorkflow.PASS);
    }
}
