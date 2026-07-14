// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 谷歌趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "谷歌趋势响应模型")
public class GoogleTrendVo {

    /** 谷歌趋势响应参数：市场，见表 1.2；US */
    @Schema(description = "谷歌趋势响应参数：市场，见表 1.2；US")
    private String marketplace;

    /** 谷歌趋势响应参数：关键字；phone stand */
    @Schema(description = "谷歌趋势响应参数：关键字；phone stand")
    private String keyword;

    /** 谷歌趋势响应参数：google trend链接 */
    @Schema(description = "谷歌趋势响应参数：google trend链接")
    private String link;

    /** 谷歌趋势响应参数：明细 */
    @Schema(description = "谷歌趋势响应参数：明细")
    private List<ItemsVo> items;

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
    @Schema(description = "谷歌趋势响应参数：明细")
    public static class ItemsVo {

        /** 谷歌趋势响应参数：时间戳；1555804800000 */
        @Schema(description = "谷歌趋势响应参数：时间戳；1555804800000")
        private Long time;

        /** 谷歌趋势响应参数：值；2 */
        @Schema(description = "谷歌趋势响应参数：值；2")
        private Integer value;

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
