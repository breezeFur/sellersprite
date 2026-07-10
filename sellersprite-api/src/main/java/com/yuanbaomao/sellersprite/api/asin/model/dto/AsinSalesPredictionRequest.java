// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ASIN 销量预测请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ASIN 销量预测请求模型")
public class AsinSalesPredictionRequest {

    /** ASIN 销量预测请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "ASIN 销量预测请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** ASIN 销量预测请求参数：asin；B07Z82895W */
    @NotBlank
    @Schema(description = "ASIN 销量预测请求参数：asin；B07Z82895W")
    private String asin;

}
