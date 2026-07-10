// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * BSR销量预测请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "BSR销量预测请求模型")
public class BsrSalesPredictionRequest {

    /** BSR销量预测请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "BSR销量预测请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** BSR销量预测请求参数：大类排名；1024 */
    @NotNull
    @Schema(description = "BSR销量预测请求参数：大类排名；1024")
    private Integer bsr;

    /** BSR销量预测请求参数：一级类目节点，查产品类目返回；11260432011 */
    @NotBlank
    @Schema(description = "BSR销量预测请求参数：一级类目节点，查产品类目返回；11260432011")
    private String categoryId;

}
