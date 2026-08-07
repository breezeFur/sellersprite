package cyou.yuanbaomao.sellersprite.research.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "创建市场调研任务请求")
public class ResearchJobCreateRequest {

    @NotBlank
    @Size(max = 128)
    @Schema(description = "报告名称", example = "美容仪美国站市场调研")
    private String reportName;

    @NotNull
    @Schema(description = "Amazon市场", example = "US")
    private SellerSpriteMarketplace marketplace;

    @NotBlank
    @Size(max = 1024)
    @Pattern(regexp = ResearchConstants.NODE_ID_PATH_PATTERN, message = "类目节点路径格式不正确")
    @Schema(description = "SellerSprite类目节点路径", example = "172282:281407")
    private String nodeIdPath;

    @NotBlank
    @Pattern(regexp = ResearchConstants.RESEARCH_MONTH_PATTERN, message = "月份必须为yyyy-MM格式")
    @Schema(description = "调研月份，任务内使用yyyy-MM格式", example = "2026-07")
    private String month;

    @Size(max = 256)
    @Schema(description = "可选核心关键词", example = "facial cleansing device")
    private String keyword;

    @Size(max = ResearchConstants.MAX_SEED_ASINS)
    @Schema(description = "可选种子ASIN，最多20个")
    private List<@Pattern(regexp = "^[A-Za-z0-9]{10}$", message = "ASIN必须是10位字母或数字") String> seedAsins;

    @Valid
    @NotNull
    @Schema(description = "采集子图参数；证据和报告节点不接受参数")
    private CollectionGraphConfig collectionConfig;

    @Size(max = 4_000)
    @Schema(description = "可选Agent分析目标")
    private String analysisGoal;
}
