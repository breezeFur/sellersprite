// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 选市场-卖家集中度响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-卖家集中度响应模型")
public class MarketSellerConcentrationVo {

    /** 选市场-卖家集中度响应参数：卖家名称；JA Wholesale LLC */
    @Schema(description = "选市场-卖家集中度响应参数：卖家名称；JA Wholesale LLC")
    private String name;

    /** 选市场-卖家集中度响应参数：排名；1 */
    @Schema(description = "选市场-卖家集中度响应参数：排名；1")
    private Integer ranking;

    /** 选市场-卖家集中度响应参数：包含的商品ASIN集合；["B00P19MFYE"] */
    @Schema(description = "选市场-卖家集中度响应参数：包含的商品ASIN集合；[\"B00P19MFYE\"]")
    private List<String> asinSet;

    /** 选市场-卖家集中度响应参数：商品数量，包含新品；4 */
    @Schema(description = "选市场-卖家集中度响应参数：商品数量，包含新品；4")
    private Integer products;

    /** 选市场-卖家集中度响应参数：新品数量；1 */
    @Schema(description = "选市场-卖家集中度响应参数：新品数量；1")
    private Integer newProducts;

    /** 选市场-卖家集中度响应参数：新品销量；45 */
    @Schema(description = "选市场-卖家集中度响应参数：新品销量；45")
    private Integer newUnits;

    /** 选市场-卖家集中度响应参数：新品销售额；2342 */
    @Schema(description = "选市场-卖家集中度响应参数：新品销售额；2342")
    private BigDecimal newRevenue;

    /** 选市场-卖家集中度响应参数：新品销量占比；4.3 */
    @Schema(description = "选市场-卖家集中度响应参数：新品销量占比；4.3")
    private BigDecimal newUnitsRatio;

    /** 选市场-卖家集中度响应参数：新品销售额占比；4 */
    @Schema(description = "选市场-卖家集中度响应参数：新品销售额占比；4")
    private BigDecimal newRevenueRatio;

    /** 选市场-卖家集中度响应参数：平均价格；6.19 */
    @Schema(description = "选市场-卖家集中度响应参数：平均价格；6.19")
    private BigDecimal avgPrice;

    /** 选市场-卖家集中度响应参数：评分数；5695 */
    @Schema(description = "选市场-卖家集中度响应参数：评分数；5695")
    private Integer ratings;

    /** 选市场-卖家集中度响应参数：评分值；4.8 */
    @Schema(description = "选市场-卖家集中度响应参数：评分值；4.8")
    private BigDecimal rating;

    /** 选市场-卖家集中度响应参数：评论数；234 */
    @Schema(description = "选市场-卖家集中度响应参数：评论数；234")
    private Integer reviews;

    /** 选市场-卖家集中度响应参数：总销量；32342 */
    @Schema(description = "选市场-卖家集中度响应参数：总销量；32342")
    private Integer totalUnits;

    /** 选市场-卖家集中度响应参数：总销额；18837.35 */
    @Schema(description = "选市场-卖家集中度响应参数：总销额；18837.35")
    private BigDecimal totalRevenue;

    /** 选市场-卖家集中度响应参数：总销量占比；0.4478 */
    @Schema(description = "选市场-卖家集中度响应参数：总销量占比；0.4478")
    private BigDecimal totalUnitsRatio;

    /** 选市场-卖家集中度响应参数：总销额占比；0.3052 */
    @Schema(description = "选市场-卖家集中度响应参数：总销额占比；0.3052")
    private BigDecimal totalRevenueRatio;

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
