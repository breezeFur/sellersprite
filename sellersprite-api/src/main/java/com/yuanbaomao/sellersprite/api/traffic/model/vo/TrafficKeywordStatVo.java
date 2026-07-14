// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 流量词统计响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "流量词统计响应模型")
public class TrafficKeywordStatVo {

    /** 流量词统计响应参数：市场；US */
    @Schema(description = "流量词统计响应参数：市场；US")
    private String marketplace;

    /** 流量词统计响应参数：asin；B07Z82895W */
    @Schema(description = "流量词统计响应参数：asin；B07Z82895W")
    private String asin;

    /** 流量词统计响应参数：全部流量词条数；2685 */
    @Schema(description = "流量词统计响应参数：全部流量词条数；2685")
    private Integer keywords;

    /** 流量词统计响应参数：自然流量词条数；1848 */
    @Schema(description = "流量词统计响应参数：自然流量词条数；1848")
    private Integer ranks;

    /** 流量词统计响应参数：广告流量词条数；1414 */
    @Schema(description = "流量词统计响应参数：广告流量词条数；1414")
    private Integer ads;

    /** 流量词统计响应参数：最近计算时间 */
    @Schema(description = "流量词统计响应参数：最近计算时间")
    private Long calcTime;

    /** 流量词统计响应参数：流量词类型统计 */
    @Schema(description = "流量词统计响应参数：流量词类型统计")
    private BadgeCountVo badgeCount;

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
    @Schema(description = "流量词统计响应参数：流量词类型统计")
    public static class BadgeCountVo {

        /** 流量词统计响应参数：自然搜索词数量；1070 */
        @Schema(description = "流量词统计响应参数：自然搜索词数量；1070")
        private Integer ns;

        /** 流量词统计响应参数：AC推荐词数量；0 */
        @Schema(description = "流量词统计响应参数：AC推荐词数量；0")
        private Integer ac;

        /** 流量词统计响应参数：ER推荐词数量；42 */
        @Schema(description = "流量词统计响应参数：ER推荐词数量；42")
        private Integer er;

        /** 流量词统计响应参数：4星推荐词数量；0 */
        @Schema(description = "流量词统计响应参数：4星推荐词数量；0")
        private Integer fs;

        /** 流量词统计响应参数：HR广告词数量；117 */
        @Schema(description = "流量词统计响应参数：HR广告词数量；117")
        private Integer hr;

        /** 流量词统计响应参数：品牌广告词数量；334 */
        @Schema(description = "流量词统计响应参数：品牌广告词数量；334")
        private Integer sb;

        /** 流量词统计响应参数：视频广告词数量；208 */
        @Schema(description = "流量词统计响应参数：视频广告词数量；208")
        private Integer sv;

        /** 流量词统计响应参数：SP广告词数量；764 */
        @Schema(description = "流量词统计响应参数：SP广告词数量；764")
        private Integer ad;

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
