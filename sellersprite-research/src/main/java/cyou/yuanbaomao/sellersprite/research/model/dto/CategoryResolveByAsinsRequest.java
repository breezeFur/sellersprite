package cyou.yuanbaomao.sellersprite.research.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "通过 ASIN 反查类目候选请求")
public class CategoryResolveByAsinsRequest {

    @NotNull(message = "市场不能为空")
    @Schema(description = "Amazon 市场，例如 US", example = "US")
    private SellerSpriteMarketplace marketplace;

    @NotEmpty(message = "ASIN 列表不能为空")
    @Size(max = 40, message = "最多支持 40 个 ASIN")
    @Schema(description = "待反查的 ASIN 列表，最多 40 个", example = "[\"B08GHW4TBS\"]")
    private List<@Pattern(regexp = "^[A-Za-z0-9]{10}$", message = "ASIN 必须是 10 位字母或数字") String> asins;

    @Schema(description = "历史月份，格式 yyyyMM 或 yyyy-MM（可选）", example = "2026-07")
    private String month;
}
