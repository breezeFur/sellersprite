package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import cyou.yuanbaomao.sellersprite.research.service.ResearchRawDataInspectionService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Agent 只读原始数据工具；job 与阶段权限由 analysisRunId 的服务端上下文决定。 */
@Component
@RequiredArgsConstructor
public class ResearchRawDataTools {

    private final AmazonSelectionToolContext toolContext;
    private final ResearchRawDataInspectionService inspectionService;

    @Tool(
            name = "inspectResearchRawDatasetCatalog",
            description = "查看当前分析阶段允许访问的原始数据集字段目录、类型、观测数、短样例和证据引用提示")
    public String inspectResearchRawDatasetCatalog(
            @ToolParam(description = "提示词中提供的分析运行 ID") String analysisRunId) {
        ResearchRawDataAccessScope scope = toolContext.getRequiredRawDataScope(analysisRunId);
        return inspectionService.describeCatalog(scope.jobId(), scope.stageCode());
    }

    @Tool(
            name = "queryResearchRawDatasetFields",
            description = "从当前阶段允许的数据集中读取指定字段的有限实际值；只能使用字段目录中的 datasetCode 和规范化字段路径")
    public String queryResearchRawDatasetFields(
            @ToolParam(description = "提示词中提供的分析运行 ID") String analysisRunId,
            @ToolParam(description = "字段目录列出的精确 datasetCode") String datasetCode,
            @ToolParam(description = "逗号或换行分隔的规范化字段路径，最多 12 个") String fieldPaths,
            @ToolParam(description = "每个字段返回的最大非空值数量，范围 1-20") Integer limit) {
        ResearchRawDataAccessScope scope = toolContext.getRequiredRawDataScope(analysisRunId);
        return inspectionService.queryFields(
                scope.jobId(),
                scope.stageCode(),
                datasetCode,
                parseFieldPaths(fieldPaths),
                limit);
    }

    private List<String> parseFieldPaths(String fieldPaths) {
        if (fieldPaths == null || fieldPaths.isBlank()) {
            return List.of();
        }
        return Arrays.stream(fieldPaths.split("[,，\r\n]+"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
