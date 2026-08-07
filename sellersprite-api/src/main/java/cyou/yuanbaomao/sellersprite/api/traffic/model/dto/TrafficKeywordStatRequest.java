// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.traffic.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 流量词统计请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "流量词统计请求模型")
public class TrafficKeywordStatRequest {

    /** 流量词统计请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "流量词统计请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 流量词统计请求参数：asin；B07Z82895W */
    @NotBlank
    @Schema(description = "流量词统计请求参数：asin；B07Z82895W")
    private String asin;

    /** 流量词统计请求参数：查询月份；202605 */
    @Schema(description = "流量词统计请求参数：查询月份；202605")
    private String month;

}
