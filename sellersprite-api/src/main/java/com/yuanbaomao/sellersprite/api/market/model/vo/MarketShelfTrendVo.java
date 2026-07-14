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
 * 选市场-上架趋势分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-上架趋势分布响应模型")
public class MarketShelfTrendVo {

    /** 选市场-上架趋势分布响应参数：类型说明；2014 */
    @Schema(description = "选市场-上架趋势分布响应参数：类型说明；2014")
    private String label;

    /** 选市场-上架趋势分布响应参数：年份，yyyy格式；2014 */
    @Schema(description = "选市场-上架趋势分布响应参数：年份，yyyy格式；2014")
    private String year;

    /** 选市场-上架趋势分布响应参数：包含的asin列表；["B00P19MFYE"] */
    @Schema(description = "选市场-上架趋势分布响应参数：包含的asin列表；[\"B00P19MFYE\"]")
    private List<String> asins;

    /** 选市场-上架趋势分布响应参数：产品数；1 */
    @Schema(description = "选市场-上架趋势分布响应参数：产品数；1")
    private Integer products;

    /** 选市场-上架趋势分布响应参数：销售额；2515 */
    @Schema(description = "选市场-上架趋势分布响应参数：销售额；2515")
    private BigDecimal revenue;

    /** 选市场-上架趋势分布响应参数：销量；18837.35 */
    @Schema(description = "选市场-上架趋势分布响应参数：销量；18837.35")
    private Integer units;

    /** 选市场-上架趋势分布响应参数：销量占比；0.4478 */
    @Schema(description = "选市场-上架趋势分布响应参数：销量占比；0.4478")
    private BigDecimal unitsRatio;

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
