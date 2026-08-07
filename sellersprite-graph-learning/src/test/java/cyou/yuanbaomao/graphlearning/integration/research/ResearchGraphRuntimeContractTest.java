package cyou.yuanbaomao.graphlearning.integration.research;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "graph.learning.project-integration", matches = "true")
class ResearchGraphRuntimeContractTest {

    @Test
    void shouldExposeExistingResearchGraphAndAiRuntime() throws Exception {
        Class<?> configuration = Class.forName(
                "cyou.yuanbaomao.sellersprite.research.graph.config.ResearchGraphConfiguration");
        Class<?> executor = Class.forName(
                "cyou.yuanbaomao.sellersprite.research.graph.runtime.ResearchGraphExecutor");
        Class<?> analysisStage = Class.forName(
                "cyou.yuanbaomao.sellersprite.ai.research.runtime.DefaultResearchAnalysisStageAdapter");

        assertThat(methodNames(configuration)).contains("marketResearchGraph", "marketResearchCheckpointSaver");
        assertThat(methodNames(executor)).contains("submit");
        assertThat(methodNames(analysisStage)).contains("runInitial");
    }

    private static String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).map(java.lang.reflect.Method::getName).toArray(String[]::new);
    }
}
