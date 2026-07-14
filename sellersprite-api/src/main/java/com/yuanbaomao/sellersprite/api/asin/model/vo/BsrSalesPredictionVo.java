// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * BSR销量预测响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "BSR销量预测响应模型")
public class BsrSalesPredictionVo {

    /** BSR销量预测响应参数：市场；US */
    @Schema(description = "BSR销量预测响应参数：市场；US")
    private String marketplace;

    /** BSR销量预测响应参数：1；B07Z82895W */
    @Schema(description = "BSR销量预测响应参数：1；B07Z82895W")
    private Integer bsr;

    /** BSR销量预测响应参数：类目名称；2685 */
    @Schema(description = "BSR销量预测响应参数：类目名称；2685")
    private String categoryLabel;

    /** BSR销量预测响应参数：预测日销量；99 */
    @Schema(description = "BSR销量预测响应参数：预测日销量；99")
    private Integer estDailySales;

    /** BSR销量预测响应参数：预测30天销量；2965 */
    @Schema(description = "BSR销量预测响应参数：预测30天销量；2965")
    private Integer estMonthSales;

    /** BSR销量预测响应参数：明细 */
    @Schema(description = "BSR销量预测响应参数：明细")
    private List<ItemListVo> itemList;

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

    @Data
    @Schema(description = "BSR销量预测响应参数：明细")
    public static class ItemListVo {

        /** BSR销量预测响应参数：bsr；1 */
        @Schema(description = "BSR销量预测响应参数：bsr；1")
        private Integer bsr;

        /** BSR销量预测响应参数：预测日销量；99 */
        @Schema(description = "BSR销量预测响应参数：预测日销量；99")
        private Integer estDailySales;

        /** BSR销量预测响应参数：预测30天销量；2965 */
        @Schema(description = "BSR销量预测响应参数：预测30天销量；2965")
        private Integer estMonthSales;

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

}
