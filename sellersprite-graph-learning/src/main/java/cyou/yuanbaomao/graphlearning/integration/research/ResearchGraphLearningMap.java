package cyou.yuanbaomao.graphlearning.integration.research;

import java.util.List;

/**
 * 将通用课程映射到现有市场调研 Graph 的学习索引，不在基础课程中引入业务实体。
 */
public final class ResearchGraphLearningMap {

    private ResearchGraphLearningMap() {
    }

    public static List<String> phases() {
        return List.of(
                "validate -> lesson.basic",
                "collect -> lesson.parallel",
                "validateDataset -> lesson.routing",
                "analyze -> lesson.agent",
                "render/publish -> lesson.streaming",
                "MysqlSaver/threadId -> lesson.checkpoint");
    }
}
