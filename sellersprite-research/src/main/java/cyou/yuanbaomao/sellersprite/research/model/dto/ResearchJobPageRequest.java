package cyou.yuanbaomao.sellersprite.research.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "我的市场调研历史报告分页查询")
public class ResearchJobPageRequest {

    private static final long DEFAULT_PAGE_NUMBER = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;

    @Min(value = 1, message = "页码必须大于等于1")
    @Schema(description = "当前页码", example = "1")
    private long current = DEFAULT_PAGE_NUMBER;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "每页数量", example = "20")
    private long size = DEFAULT_PAGE_SIZE;

    @Size(max = 128, message = "搜索关键字不能超过128个字符")
    @Schema(description = "报告名称、调研关键词或任务ID关键字")
    private String keyword;

    @Schema(description = "任务状态")
    private ResearchJobStatus status;

    @Schema(description = "Amazon站点")
    private SellerSpriteMarketplace marketplace;

    @Pattern(
            regexp = "^$|^\\d{4}-(0[1-9]|1[0-2])$",
            message = "调研月份必须为yyyy-MM格式")
    @Schema(description = "调研月份，yyyy-MM格式", example = "2026-07")
    private String month;
}
