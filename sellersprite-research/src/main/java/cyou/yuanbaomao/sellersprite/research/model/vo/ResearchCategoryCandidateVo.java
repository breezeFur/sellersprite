package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "类目反查候选结果")
public class ResearchCategoryCandidateVo {

    @Schema(description = "类目节点 ID 路径", example = "1055398:1063252:1063280")
    private String nodeIdPath;

    @Schema(description = "当前类目节点 ID", example = "1063280")
    private String nodeId;

    @Schema(description = "类目名称路径（英文）", example = "Home & Kitchen:Bedding:Blankets & Throws")
    private String nodeLabelPath;

    @Schema(description = "当前类目节点英文名", example = "Blankets & Throws")
    private String nodeLabel;

    @Schema(description = "当前类目节点中文名（如有）", example = "毯子、盖毯")
    private String nodeLabelLocale;

    @Schema(description = "展示名称", example = "Blankets & Throws")
    private String displayName;

    @Schema(description = "匹配到的 ASIN 数量", example = "3")
    private Integer matchedCount;

    @Schema(description = "匹配到的 ASIN 列表", example = "[\"B08GHW4TBS\"]")
    private List<String> matchedAsins;

    @Schema(description = "占请求有效 ASIN 的比例（百分比 0-100）", example = "60.0")
    private Double matchedRatio;
}
