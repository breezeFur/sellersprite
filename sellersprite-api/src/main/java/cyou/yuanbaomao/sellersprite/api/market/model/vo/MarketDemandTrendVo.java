// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.market.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 选市场-商品需求趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "选市场-商品需求趋势响应模型")
public class MarketDemandTrendVo {

    /** 选市场-商品需求趋势响应参数：asin数量；22187 */
    @Schema(description = "选市场-商品需求趋势响应参数：asin数量；22187")
    private String asinCount;

    /** 选市场-商品需求趋势响应参数：退货率，百分比；1.38 */
    @Schema(description = "选市场-商品需求趋势响应参数：退货率，百分比；1.38")
    private String returnRatio;

    /** 选市场-商品需求趋势响应参数：搜索购买比，千分比；3.17875 */
    @Schema(description = "选市场-商品需求趋势响应参数：搜索购买比，千分比；3.17875")
    private BigDecimal searchToPurchaseRatio;

    /** 选市场-商品需求趋势响应参数：类目平均退货率，百分比；2.72 */
    @Schema(description = "选市场-商品需求趋势响应参数：类目平均退货率，百分比；2.72")
    private BigDecimal avgReturnRatio;

    /** 选市场-商品需求趋势响应参数：类目平均搜索购买比，千分比；2.6 */
    @Schema(description = "选市场-商品需求趋势响应参数：类目平均搜索购买比，千分比；2.6")
    private BigDecimal avgSearchToPurchaseRatio;

    /** 选市场-商品需求趋势响应参数：月浏览趋势 */
    @Schema(description = "选市场-商品需求趋势响应参数：月浏览趋势")
    private List<ItemsVo> items;

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                getClass().getName(), name, value);
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

    @Slf4j

    @Data
    @Schema(description = "选市场-商品需求趋势响应参数：月浏览趋势")
    public static class ItemsVo {

        /** 选市场-商品需求趋势响应参数：时间，yyyy-MM-dd格式；2022-09-10 */
        @Schema(description = "选市场-商品需求趋势响应参数：时间，yyyy-MM-dd格式；2022-09-10")
        private String date;

        /** 选市场-商品需求趋势响应参数：浏览量；2 */
        @Schema(description = "选市场-商品需求趋势响应参数：浏览量；2")
        private Integer glanceViews;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

}
