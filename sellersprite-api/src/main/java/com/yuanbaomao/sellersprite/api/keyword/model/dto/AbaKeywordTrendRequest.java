// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ABA 数据选品-关键词趋势请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ABA 数据选品-关键词趋势请求模型")
public class AbaKeywordTrendRequest {

    /** ABA 数据选品-关键词趋势请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "ABA 数据选品-关键词趋势请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** ABA 数据选品-关键词趋势请求参数：关键词 */
    @NotBlank
    @Schema(description = "ABA 数据选品-关键词趋势请求参数：关键词")
    private String keyword;

    /** ABA 数据选品-关键词趋势请求参数：时间粒度；W：周，M：月 */
    @Schema(description = "ABA 数据选品-关键词趋势请求参数：时间粒度；W：周，M：月")
    private String timeGranularity;

}
