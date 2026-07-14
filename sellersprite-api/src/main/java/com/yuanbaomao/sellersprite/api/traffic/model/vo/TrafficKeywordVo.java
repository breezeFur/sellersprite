// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.vo;

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
 * 关键词反查(流量词列表)响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关键词反查(流量词列表)响应模型")
public class TrafficKeywordVo {

    /** 关键词反查(流量词列表)响应参数：市场编码；见表 1.2 */
    @Schema(description = "关键词反查(流量词列表)响应参数：市场编码；见表 1.2")
    private String marketplace;

    /** 关键词反查(流量词列表)响应参数：asin；B07Z82895W */
    @Schema(description = "关键词反查(流量词列表)响应参数：asin；B07Z82895W")
    private String asin;

    /** 关键词反查(流量词列表)响应参数：总条数；2685 */
    @Schema(description = "关键词反查(流量词列表)响应参数：总条数；2685")
    private Integer total;

    /** 关键词反查(流量词列表)响应参数：词条；1848 */
    @Schema(description = "关键词反查(流量词列表)响应参数：词条；1848")
    private List<ItemsVo> items;

    /** 关键词反查(流量词列表)响应参数：高频词 */
    @Schema(description = "关键词反查(流量词列表)响应参数：高频词")
    private List<StatsVo> stats;

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
    @Schema(description = "关键词反查(流量词列表)响应参数：词条；1848")
    public static class ItemsVo {

        /** 关键词反查(流量词列表)响应参数：关键词；该ASIN近30天或某个自然月进入过亚马逊搜索结果前3页的词 */
        @Schema(description = "关键词反查(流量词列表)响应参数：关键词；该ASIN近30天或某个自然月进入过亚马逊搜索结果前3页的词")
        private String keyword;

        /** 关键词反查(流量词列表)响应参数：关键词中文翻译；手机支架 */
        @Schema(description = "关键词反查(流量词列表)响应参数：关键词中文翻译；手机支架")
        private String keywordCn;

        /** 关键词反查(流量词列表)响应参数：月搜索量；指的是一个自然月的月搜索量，比如2025年8月，该关键词在亚马逊站内的搜索总次数 */
        @Schema(description = "关键词反查(流量词列表)响应参数：月搜索量；指的是一个自然月的月搜索量，比如2025年8月，该关键词在亚马逊站内的搜索总次数")
        private Integer searches;

        /** 关键词反查(流量词列表)响应参数：商品数；指搜索该关键词后出现了多少个相关的产品 */
        @Schema(description = "关键词反查(流量词列表)响应参数：商品数；指搜索该关键词后出现了多少个相关的产品")
        private Integer products;

        /** 关键词反查(流量词列表)响应参数：月购买量；在亚马逊站内搜索该关键词后产生购买的次数，比如：某用户搜索iphone charger，然后1次购买了1个iphone充电器，2条数据线(关联推荐的商品)，则购买量=1 */
        @Schema(description = "关键词反查(流量词列表)响应参数：月购买量；在亚马逊站内搜索该关键词后产生购买的次数，比如：某用户搜索iphone charger，然后1次购买了1个iphone充电器，2条数据线(关联推荐的商品)，则购买量=1")
        private Integer purchases;

        /** 关键词反查(流量词列表)响应参数：购买率；指买家输入该搜索词并点击此细分市场中的任意商品后，买家的购买次数占买家输入该搜索词总次数的比例 */
        @Schema(description = "关键词反查(流量词列表)响应参数：购买率；指买家输入该搜索词并点击此细分市场中的任意商品后，买家的购买次数占买家输入该搜索词总次数的比例")
        private BigDecimal purchaseRate;

        /** 关键词反查(流量词列表)响应参数：PPC竞价；亚马逊站内广告Bid价格，系统提供【词组匹配】的Bid建议价格以及范围 */
        @Schema(description = "关键词反查(流量词列表)响应参数：PPC竞价；亚马逊站内广告Bid价格，系统提供【词组匹配】的Bid建议价格以及范围")
        private BigDecimal bid;

        /** 关键词反查(流量词列表)响应参数：PPC竞价范围 */
        @Schema(description = "关键词反查(流量词列表)响应参数：PPC竞价范围")
        private BigDecimal bidMax;

        /** 关键词反查(流量词列表)响应参数：PPC竞价范围 */
        @Schema(description = "关键词反查(流量词列表)响应参数：PPC竞价范围")
        private BigDecimal bidMin;

        /** 关键词反查(流量词列表)响应参数：曝光位置；ASIN在对应关键词的搜索结果下曝光的具体位置， 见表1.10 */
        @Schema(description = "关键词反查(流量词列表)响应参数：曝光位置；ASIN在对应关键词的搜索结果下曝光的具体位置， 见表1.10")
        private List<String> badges;

        /** 关键词反查(流量词列表)响应参数：自然排名 */
        @Schema(description = "关键词反查(流量词列表)响应参数：自然排名")
        private ItemsRankPositionVo rankPosition;

        /** 关键词反查(流量词列表)响应参数：广告排名 */
        @Schema(description = "关键词反查(流量词列表)响应参数：广告排名")
        private ItemsAdPositionVo adPosition;

        /** 关键词反查(流量词列表)响应参数：周搜索量排名；数据来源于亚马逊ABA数据的关键词搜索频率排名（Search Frequency Rank），数字越小表示排名越靠前，搜索量越高 */
        @Schema(description = "关键词反查(流量词列表)响应参数：周搜索量排名；数据来源于亚马逊ABA数据的关键词搜索频率排名（Search Frequency Rank），数字越小表示排名越靠前，搜索量越高")
        private Integer searchesRank;

        /** 关键词反查(流量词列表)响应参数：周搜索量排名时间范围 */
        @Schema(description = "关键词反查(流量词列表)响应参数：周搜索量排名时间范围")
        private Long searchesRankTimeFrom;

        /** 关键词反查(流量词列表)响应参数：searchesRankTimeTo */
        @Schema(description = "关键词反查(流量词列表)响应参数：searchesRankTimeTo")
        private Long searchesRankTimeTo;

        /** 关键词反查(流量词列表)响应参数：最近1天广告竞品数；表示近1天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告 */
        @Schema(description = "关键词反查(流量词列表)响应参数：最近1天广告竞品数；表示近1天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告")
        private Integer latest1daysAds;

        /** 关键词反查(流量词列表)响应参数：最近7天广告竞品数；表示近7天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告 */
        @Schema(description = "关键词反查(流量词列表)响应参数：最近7天广告竞品数；表示近7天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告")
        private Integer latest7daysAds;

        /** 关键词反查(流量词列表)响应参数：最近30天广告竞品数；表示近30天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告 */
        @Schema(description = "关键词反查(流量词列表)响应参数：最近30天广告竞品数；表示近30天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告")
        private Integer latest30daysAds;

        /** 关键词反查(流量词列表)响应参数：供需比；搜索量(需求) / 商品数(供应)，在同类市场中，需供比值越高，则代表该市场需求越强劲 */
        @Schema(description = "关键词反查(流量词列表)响应参数：供需比；搜索量(需求) / 商品数(供应)，在同类市场中，需供比值越高，则代表该市场需求越强劲")
        private BigDecimal supplyDemandRatio;

        /** 关键词反查(流量词列表)响应参数：流量占比；指的是产品通过不同流量词获得的曝光量占比 */
        @Schema(description = "关键词反查(流量词列表)响应参数：流量占比；指的是产品通过不同流量词获得的曝光量占比")
        private BigDecimal trafficPercentage;

        /** 关键词反查(流量词列表)响应参数：流量占比类型；见表2.0 */
        @Schema(description = "关键词反查(流量词列表)响应参数：流量占比类型；见表2.0")
        private String trafficKeywordType;

        /** 关键词反查(流量词列表)响应参数：转换效果类型；见表2.1 */
        @Schema(description = "关键词反查(流量词列表)响应参数：转换效果类型；见表2.1")
        private String conversionKeywordType;

        /** 关键词反查(流量词列表)响应参数：预估周曝光量；指的是该关键词本周内给产品带来的预估曝光量，非该词在亚马逊的总搜索量 */
        @Schema(description = "关键词反查(流量词列表)响应参数：预估周曝光量；指的是该关键词本周内给产品带来的预估曝光量，非该词在亚马逊的总搜索量")
        private BigDecimal calculatedWeeklySearches;

        /** 关键词反查(流量词列表)响应参数：展示量；指一个自然月，比如2024年3月，在某个关键词搜索结果页中所有ASIN的总展示次数，非单个ASIN在关键词下的曝光量 */
        @Schema(description = "关键词反查(流量词列表)响应参数：展示量；指一个自然月，比如2024年3月，在某个关键词搜索结果页中所有ASIN的总展示次数，非单个ASIN在关键词下的曝光量")
        private Long impressions;

        /** 关键词反查(流量词列表)响应参数：更新时间 */
        @Schema(description = "关键词反查(流量词列表)响应参数：更新时间")
        private Long updatedTime;

        /** 关键词反查(流量词列表)响应参数：点击量；指一个自然月，比如2024年3月，在某个关键词搜索结果页中被点击的总次数，非单个ASIN在关键词下的点击量 */
        @Schema(description = "关键词反查(流量词列表)响应参数：点击量；指一个自然月，比如2024年3月，在某个关键词搜索结果页中被点击的总次数，非单个ASIN在关键词下的点击量")
        private Integer clicks;

        /** 关键词反查(流量词列表)响应参数：流量分布-自然占比；0.9312 */
        @Schema(description = "关键词反查(流量词列表)响应参数：流量分布-自然占比；0.9312")
        private BigDecimal naturalRatio;

        /** 关键词反查(流量词列表)响应参数：流量分布-广告占比；0.0688 */
        @Schema(description = "关键词反查(流量词列表)响应参数：流量分布-广告占比；0.0688")
        private BigDecimal adRatio;

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

    @Data
    @Schema(description = "关键词反查(流量词列表)响应参数：自然排名")
    public static class ItemsRankPositionVo {

        /** 关键词反查(流量词列表)响应参数：第几页；3 */
        @Schema(description = "关键词反查(流量词列表)响应参数：第几页；3")
        private Integer page;

        /** 关键词反查(流量词列表)响应参数：每页多少条数据；60 */
        @Schema(description = "关键词反查(流量词列表)响应参数：每页多少条数据；60")
        private Integer pageSize;

        /** 关键词反查(流量词列表)响应参数：当前页排第几；10 */
        @Schema(description = "关键词反查(流量词列表)响应参数：当前页排第几；10")
        private Integer index;

        /** 关键词反查(流量词列表)响应参数：总结果中排第几；106 */
        @Schema(description = "关键词反查(流量词列表)响应参数：总结果中排第几；106")
        private Integer position;

        /** 关键词反查(流量词列表)响应参数：排名时间 */
        @Schema(description = "关键词反查(流量词列表)响应参数：排名时间")
        private Long updatedTime;

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

    @Data
    @Schema(description = "关键词反查(流量词列表)响应参数：广告排名")
    public static class ItemsAdPositionVo {

        /** 关键词反查(流量词列表)响应参数：第几页；2 */
        @Schema(description = "关键词反查(流量词列表)响应参数：第几页；2")
        private Integer page;

        /** 关键词反查(流量词列表)响应参数：每页多少条数据；63 */
        @Schema(description = "关键词反查(流量词列表)响应参数：每页多少条数据；63")
        private Integer pageSize;

        /** 关键词反查(流量词列表)响应参数：当前页排第几；37 */
        @Schema(description = "关键词反查(流量词列表)响应参数：当前页排第几；37")
        private Integer index;

        /** 关键词反查(流量词列表)响应参数：总结果中排第几；85 */
        @Schema(description = "关键词反查(流量词列表)响应参数：总结果中排第几；85")
        private Integer position;

        /** 关键词反查(流量词列表)响应参数：排名时间 */
        @Schema(description = "关键词反查(流量词列表)响应参数：排名时间")
        private Long updatedTime;

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

    @Data
    @Schema(description = "关键词反查(流量词列表)响应参数：高频词")
    public static class StatsVo {

        /** 关键词反查(流量词列表)响应参数：词；phone */
        @Schema(description = "关键词反查(流量词列表)响应参数：词；phone")
        private String keywords;

        /** 关键词反查(流量词列表)响应参数：总条数；90 */
        @Schema(description = "关键词反查(流量词列表)响应参数：总条数；90")
        private Integer total;

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
