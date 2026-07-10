// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 谷歌趋势请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "谷歌趋势请求模型")
public class GoogleTrendRequest {

    /** 谷歌趋势请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "谷歌趋势请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 谷歌趋势请求参数：关键字；iphone stand */
    @Schema(description = "谷歌趋势请求参数：关键字；iphone stand")
    private String keyword;

    /** 谷歌趋势请求参数：类别；web:google网页搜索shoppingCart:google购物搜索 */
    @Schema(description = "谷歌趋势请求参数：类别；web:google网页搜索shoppingCart:google购物搜索")
    private String googleProp;

    /** 谷歌趋势请求参数：按照月份；false（默认值） */
    @Schema(description = "谷歌趋势请求参数：按照月份；false（默认值）")
    private Boolean monthly;

}
