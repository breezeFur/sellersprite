// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.vo;

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
 * 拓展流量词明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "拓展流量词明细响应模型")
public class TrafficKeywordExtendItemVo {

    /** 拓展流量词明细响应参数：关键字；N95 */
    @Schema(description = "拓展流量词明细响应参数：关键字；N95")
    private String keyword;

    /** 拓展流量词明细响应参数：关键词中文翻译；用于录音的电话支架 */
    @Schema(description = "拓展流量词明细响应参数：关键词中文翻译；用于录音的电话支架")
    private String keywordCn;

    /** 拓展流量词明细响应参数：搜索量；21582 */
    @Schema(description = "拓展流量词明细响应参数：搜索量；21582")
    private Integer searches;

    /** 拓展流量词明细响应参数：月购买量；1996 */
    @Schema(description = "拓展流量词明细响应参数：月购买量；1996")
    private Integer purchases;

    /** 拓展流量词明细响应参数：月购买率；0.0925 */
    @Schema(description = "拓展流量词明细响应参数：月购买率；0.0925")
    private BigDecimal purchaseRate;

    /** 拓展流量词明细响应参数：商品数；1645 */
    @Schema(description = "拓展流量词明细响应参数：商品数；1645")
    private Integer products;

    /** 拓展流量词明细响应参数：最小PPC价格；1.34 */
    @Schema(description = "拓展流量词明细响应参数：最小PPC价格；1.34")
    private BigDecimal bidMin;

    /** 拓展流量词明细响应参数：最大PPC价格；3.21 */
    @Schema(description = "拓展流量词明细响应参数：最大PPC价格；3.21")
    private BigDecimal bidMax;

    /** 拓展流量词明细响应参数：PPC价格；1.6 */
    @Schema(description = "拓展流量词明细响应参数：PPC价格；1.6")
    private BigDecimal bid;

    /** 拓展流量词明细响应参数：流量词类型；见表1.10 */
    @Schema(description = "拓展流量词明细响应参数：流量词类型；见表1.10")
    private List<String> badges;

    /** 拓展流量词明细响应参数：更新时间 */
    @Schema(description = "拓展流量词明细响应参数：更新时间")
    private Long updatedTime;

    /** 拓展流量词明细响应参数：周搜索量排名；25 */
    @Schema(description = "拓展流量词明细响应参数：周搜索量排名；25")
    private Integer searchesRank;

    /** 拓展流量词明细响应参数：周搜索量排名时间范围 */
    @Schema(description = "拓展流量词明细响应参数：周搜索量排名时间范围")
    private Long searchesRankTimeFrom;

    /** 拓展流量词明细响应参数：searchesRankTimeTo */
    @Schema(description = "拓展流量词明细响应参数：searchesRankTimeTo")
    private Long searchesRankTimeTo;

    /** 拓展流量词明细响应参数：最近1天广告竞品数；70 */
    @Schema(description = "拓展流量词明细响应参数：最近1天广告竞品数；70")
    private Integer latest1daysAds;

    /** 拓展流量词明细响应参数：最近7天广告竞品数；100 */
    @Schema(description = "拓展流量词明细响应参数：最近7天广告竞品数；100")
    private Integer latest7daysAds;

    /** 拓展流量词明细响应参数：最近30天广告竞品数；280 */
    @Schema(description = "拓展流量词明细响应参数：最近30天广告竞品数；280")
    private Integer latest30daysAds;

    /** 拓展流量词明细响应参数：供需比；3.8 */
    @Schema(description = "拓展流量词明细响应参数：供需比；3.8")
    private BigDecimal supplyDemandRatio;

    /** 拓展流量词明细响应参数：流量占比；0.015 */
    @Schema(description = "拓展流量词明细响应参数：流量占比；0.015")
    private BigDecimal trafficPercentage;

    /** 拓展流量词明细响应参数：预估周搜索量；40 */
    @Schema(description = "拓展流量词明细响应参数：预估周搜索量；40")
    private BigDecimal calculatedWeeklySearches;

    /** 拓展流量词明细响应参数：平均价格；36.14 */
    @Schema(description = "拓展流量词明细响应参数：平均价格；36.14")
    private BigDecimal avgPrice;

    /** 拓展流量词明细响应参数：平均评分数；12223 */
    @Schema(description = "拓展流量词明细响应参数：平均评分数；12223")
    private Integer avgRatings;

    /** 拓展流量词明细响应参数：平均评分值；4.5 */
    @Schema(description = "拓展流量词明细响应参数：平均评分值；4.5")
    private BigDecimal avgRating;

    /** 拓展流量词明细响应参数：标题密度；42.9 */
    @Schema(description = "拓展流量词明细响应参数：标题密度；42.9")
    private Integer titleDensity;

    /** 拓展流量词明细响应参数：SPR；6 */
    @Schema(description = "拓展流量词明细响应参数：SPR；6")
    private Integer spr;

    /** 拓展流量词明细响应参数：点击垄断率；0.3 */
    @Schema(description = "拓展流量词明细响应参数：点击垄断率；0.3")
    private BigDecimal monopolyClickRate;

    /** 拓展流量词明细响应参数：前三点击；0.0813 */
    @Schema(description = "拓展流量词明细响应参数：前三点击；0.0813")
    private BigDecimal top3ClickingRate;

    /** 拓展流量词明细响应参数：前三转化；0.2011 */
    @Schema(description = "拓展流量词明细响应参数：前三转化；0.2011")
    private BigDecimal top3ConversionRate;

    /** 拓展流量词明细响应参数：来自于哪些变体 */
    @Schema(description = "拓展流量词明细响应参数：来自于哪些变体")
    private List<RelationVariationsItemsVo> relationVariationsItems;

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
    @Schema(description = "拓展流量词明细响应参数：来自于哪些变体")
    public static class RelationVariationsItemsVo {

        /** 拓展流量词明细响应参数：站点；3 */
        @Schema(description = "拓展流量词明细响应参数：站点；3")
        private String marketplace;

        /** 拓展流量词明细响应参数：asin；B08P6SC34B */
        @Schema(description = "拓展流量词明细响应参数：asin；B08P6SC34B")
        private String asin;

        /** 拓展流量词明细响应参数：图片链接；10 */
        @Schema(description = "拓展流量词明细响应参数：图片链接；10")
        private String imageUrl;

        /** 拓展流量词明细响应参数：流量占比；54.6 */
        @Schema(description = "拓展流量词明细响应参数：流量占比；54.6")
        private BigDecimal trafficPercentage;

        /** 拓展流量词明细响应参数：标题 */
        @Schema(description = "拓展流量词明细响应参数：标题")
        private String title;

        /** 拓展流量词明细响应参数：价格；60 */
        @Schema(description = "拓展流量词明细响应参数：价格；60")
        private BigDecimal price;

        /** 拓展流量词明细响应参数：评论数；10 */
        @Schema(description = "拓展流量词明细响应参数：评论数；10")
        private BigDecimal reviews;

        /** 拓展流量词明细响应参数：评分；4.5 */
        @Schema(description = "拓展流量词明细响应参数：评分；4.5")
        private BigDecimal rating;

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
