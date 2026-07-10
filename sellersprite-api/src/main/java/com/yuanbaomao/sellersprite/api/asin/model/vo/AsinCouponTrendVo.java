// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * ASIN优惠趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ASIN优惠趋势响应模型")
public class AsinCouponTrendVo {

    /** ASIN优惠趋势响应参数：marketplace */
    @Schema(description = "ASIN优惠趋势响应参数：marketplace")
    private String marketplace;

    /** ASIN优惠趋势响应参数：asin */
    @Schema(description = "ASIN优惠趋势响应参数：asin")
    private String asin;

    /** ASIN优惠趋势响应参数：日期 */
    @Schema(description = "ASIN优惠趋势响应参数：日期")
    private String date;

    /** ASIN优惠趋势响应参数：优惠类型；M: 减免金额, P: 百分比折扣 */
    @Schema(description = "ASIN优惠趋势响应参数：优惠类型；M: 减免金额, P: 百分比折扣")
    private String type;

    /** ASIN优惠趋势响应参数：ASIN价格 */
    @Schema(description = "ASIN优惠趋势响应参数：ASIN价格")
    private BigDecimal asinPrice;

    /** ASIN优惠趋势响应参数：优惠金额 */
    @Schema(description = "ASIN优惠趋势响应参数：优惠金额")
    private BigDecimal couponPrice;

    /** ASIN优惠趋势响应参数：实际价格 */
    @Schema(description = "ASIN优惠趋势响应参数：实际价格")
    private BigDecimal finalPrice;

}
