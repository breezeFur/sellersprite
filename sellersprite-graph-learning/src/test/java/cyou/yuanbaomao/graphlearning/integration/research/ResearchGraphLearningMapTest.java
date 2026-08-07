package cyou.yuanbaomao.graphlearning.integration.research;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResearchGraphLearningMapTest {

    @Test
    void shouldKeepBusinessMappingOutsideGenericLessons() {
        assertThat(ResearchGraphLearningMap.phases())
                .contains("MysqlSaver/threadId -> lesson.checkpoint")
                .hasSize(6);
    }
}
