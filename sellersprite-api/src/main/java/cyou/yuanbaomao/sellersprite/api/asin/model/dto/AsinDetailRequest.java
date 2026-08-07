// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ASIN 详情请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ASIN 详情请求模型")
public class AsinDetailRequest {

    /** ASIN 详情请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "ASIN 详情请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** ASIN 详情请求参数：asin；B08GHW4TBS */
    @NotBlank
    @Schema(description = "ASIN 详情请求参数：asin；B08GHW4TBS")
    private String asin;

}
