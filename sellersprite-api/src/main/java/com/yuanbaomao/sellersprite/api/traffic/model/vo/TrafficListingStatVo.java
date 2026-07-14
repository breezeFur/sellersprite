// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 关联流量统计响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关联流量统计响应模型")
public class TrafficListingStatVo {

    /** 关联流量统计响应参数：市场；US */
    @Schema(description = "关联流量统计响应参数：市场；US")
    private String marketplace;

    /** 关联流量统计响应参数：asin；B07Z82895W */
    @Schema(description = "关联流量统计响应参数：asin；B07Z82895W")
    private String asin;

    /** 关联流量统计响应参数：全部流量；1848 */
    @Schema(description = "关联流量统计响应参数：全部流量；1848")
    private Integer relations;

    /** 关联流量统计响应参数：免费流量；1414 */
    @Schema(description = "关联流量统计响应参数：免费流量；1414")
    private Integer freeRelations;

    /** 关联流量统计响应参数：付费流量；286 */
    @Schema(description = "关联流量统计响应参数：付费流量；286")
    private Integer paidRelations;

    /** 关联流量统计响应参数：最近计算时间 */
    @Schema(description = "关联流量统计响应参数：最近计算时间")
    private Long calcTime;

    /** 关联流量统计响应参数：统计概要 */
    @Schema(description = "关联流量统计响应参数：统计概要")
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
    @Schema(description = "关联流量统计响应参数：统计概要")
    public static class ItemsVo {

        /** 关联流量统计响应参数：关联类型，见表2.2,忽略大小写；vav */
        @Schema(description = "关联流量统计响应参数：关联类型，见表2.2,忽略大小写；vav")
        private String relation;

        /** 关联流量统计响应参数：数量；3 */
        @Schema(description = "关联流量统计响应参数：数量；3")
        private Integer count;

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
