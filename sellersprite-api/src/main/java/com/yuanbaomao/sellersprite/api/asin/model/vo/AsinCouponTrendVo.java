// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

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

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

}
