// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品趋势详情(keepa)请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "商品趋势详情(keepa)请求模型")
public class KeepaTrendRequest {

    /** 商品趋势详情(keepa)请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "商品趋势详情(keepa)请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 商品趋势详情(keepa)请求参数：asin；B08GHW4TBS */
    @NotBlank
    @Schema(description = "商品趋势详情(keepa)请求参数：asin；B08GHW4TBS")
    private String asin;

    /** 商品趋势详情(keepa)请求参数：Trend Data Start Timestamp */
    @Schema(description = "商品趋势详情(keepa)请求参数：Trend Data Start Timestamp")
    private Long startTimestamp;

    /** 商品趋势详情(keepa)请求参数：Trend Data End Timestamp */
    @Schema(description = "商品趋势详情(keepa)请求参数：Trend Data End Timestamp")
    private Long endTimestamp;

    /** 商品趋势详情(keepa)请求参数：Only Get Daily Latest Data */
    @Schema(description = "商品趋势详情(keepa)请求参数：Only Get Daily Latest Data")
    private Boolean dailyLatest;

}
