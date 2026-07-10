// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关键词选品-趋势数据请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关键词选品-趋势数据请求模型")
public class KeywordResearchTrendRequest {

    /** 关键词选品-趋势数据请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "关键词选品-趋势数据请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 关键词选品-趋势数据请求参数：keyword */
    @NotBlank
    @Schema(description = "关键词选品-趋势数据请求参数：keyword")
    private String keyword;

}
