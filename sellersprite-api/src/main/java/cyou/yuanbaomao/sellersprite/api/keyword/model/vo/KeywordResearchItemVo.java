// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.keyword.model.vo;

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
 * 关键词选品明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "关键词选品明细响应模型")
public class KeywordResearchItemVo {

    /** 关键词选品明细响应参数：市场；US */
    @Schema(description = "关键词选品明细响应参数：市场；US")
    private String marketplace;

    /** 关键词选品明细响应参数：关键词；polaroid cameras */
    @Schema(description = "关键词选品明细响应参数：关键词；polaroid cameras")
    private String keywords;

    /** 关键词选品明细响应参数：搜索量；141356 */
    @Schema(description = "关键词选品明细响应参数：搜索量；141356")
    private Integer searches;

    /** 关键词选品明细响应参数：点击量；在某个关键词搜索结果页中被点击的总次数非单个ASIN在关键词下的点击量 */
    @Schema(description = "关键词选品明细响应参数：点击量；在某个关键词搜索结果页中被点击的总次数非单个ASIN在关键词下的点击量")
    private Integer clicks;

    /** 关键词选品明细响应参数：展示量；在某个关键词搜索结果页中所有ASIN的总展示次数非单个ASIN在关键词下的曝光量 */
    @Schema(description = "关键词选品明细响应参数：展示量；在某个关键词搜索结果页中所有ASIN的总展示次数非单个ASIN在关键词下的曝光量")
    private Long impressions;

    /** 关键词选品明细响应参数：月购买量；4029 */
    @Schema(description = "关键词选品明细响应参数：月购买量；4029")
    private Integer purchases;

    /** 关键词选品明细响应参数：增长率；-25.482092 */
    @Schema(description = "关键词选品明细响应参数：增长率；-25.482092")
    private BigDecimal growth;

    /** 关键词选品明细响应参数：月购买率；0.0285 */
    @Schema(description = "关键词选品明细响应参数：月购买率；0.0285")
    private BigDecimal purchaseRate;

    /** 关键词选品明细响应参数：产品数；173 */
    @Schema(description = "关键词选品明细响应参数：产品数；173")
    private Integer products;

    /** 关键词选品明细响应参数：供需比；817.09 */
    @Schema(description = "关键词选品明细响应参数：供需比；817.09")
    private BigDecimal supplyDemandRatio;

    /** 关键词选品明细响应参数：类目 */
    @Schema(description = "关键词选品明细响应参数：类目")
    private List<SearchDepartmentsVo> searchDepartments;

    /** 关键词选品明细响应参数：查询月份；2022.01 */
    @Schema(description = "关键词选品明细响应参数：查询月份；2022.01")
    private String month;

    /** 关键词选品明细响应参数：是否属于补充关键词；N */
    @Schema(description = "关键词选品明细响应参数：是否属于补充关键词；N")
    private String supplement;

    /** 关键词选品明细响应参数：关键词同比增长；139749 */
    @Schema(description = "关键词选品明细响应参数：关键词同比增长；139749")
    private Integer searchMonthlyCv;

    /** 关键词选品明细响应参数：关键词同比增长率；8696.27 */
    @Schema(description = "关键词选品明细响应参数：关键词同比增长率；8696.27")
    private BigDecimal searchMonthlyCr;

    /** 关键词选品明细响应参数：关键词近3个月增长值；-48338 */
    @Schema(description = "关键词选品明细响应参数：关键词近3个月增长值；-48338")
    private Integer searchNearlyCv;

    /** 关键词选品明细响应参数：关键词近3个月增长率；-25.48 */
    @Schema(description = "关键词选品明细响应参数：关键词近3个月增长率；-25.48")
    private BigDecimal searchNearlyCr;

    /** 关键词选品明细响应参数：货币；$ */
    @Schema(description = "关键词选品明细响应参数：货币；$")
    private String currency;

    /** 关键词选品明细响应参数：平均价格；116.24 */
    @Schema(description = "关键词选品明细响应参数：平均价格；116.24")
    private BigDecimal avgPrice;

    /** 关键词选品明细响应参数：平均评分数；2584 */
    @Schema(description = "关键词选品明细响应参数：平均评分数；2584")
    private Integer avgRatings;

    /** 关键词选品明细响应参数：平均评论数；4.5 */
    @Schema(description = "关键词选品明细响应参数：平均评论数；4.5")
    private BigDecimal avgRating;

    /** 关键词选品明细响应参数：关键词关联asin；4.8 */
    @Schema(description = "关键词选品明细响应参数：关键词关联asin；4.8")
    private List<RelationAsinListVo> relationAsinList;

    /** 关键词选品明细响应参数：bid最小价格；0.987 */
    @Schema(description = "关键词选品明细响应参数：bid最小价格；0.987")
    private BigDecimal bidMin;

    /** 关键词选品明细响应参数：bid最大价格；2.54 */
    @Schema(description = "关键词选品明细响应参数：bid最大价格；2.54")
    private BigDecimal bidMax;

    /** 关键词选品明细响应参数：bid价格；1.26 */
    @Schema(description = "关键词选品明细响应参数：bid价格；1.26")
    private BigDecimal bid;

    /** 关键词选品明细响应参数：点击垄断率；0.2633 */
    @Schema(description = "关键词选品明细响应参数：点击垄断率；0.2633")
    private BigDecimal araClickRate;

    /** 关键词选品明细响应参数：共享转化率；0.2633 */
    @Schema(description = "关键词选品明细响应参数：共享转化率；0.2633")
    private BigDecimal araShareRate;

    /** 关键词选品明细响应参数：点击前三ASIN */
    @Schema(description = "关键词选品明细响应参数：点击前三ASIN")
    private List<AraAsinListVo> araAsinList;

    /** 关键词选品明细响应参数：货流值；0.0108 */
    @Schema(description = "关键词选品明细响应参数：货流值；0.0108")
    private BigDecimal goodsValue;

    /** 关键词选品明细响应参数：TOP3 品牌；["LEGO","Jorumo","Nifeliz"] */
    @Schema(description = "关键词选品明细响应参数：TOP3 品牌；[\"LEGO\",\"Jorumo\",\"Nifeliz\"]")
    private List<String> brands;

    /** 关键词选品明细响应参数：TOP3 类目；["Toys","Home","Mobile_Apps"] */
    @Schema(description = "关键词选品明细响应参数：TOP3 类目；[\"Toys\",\"Home\",\"Mobile_Apps\"]")
    private List<String> categories;

    /** 关键词选品明细响应参数：标题密度首页商品包含该关键词的数量（不含广告位）；21 */
    @Schema(description = "关键词选品明细响应参数：标题密度首页商品包含该关键词的数量（不含广告位）；21")
    private String titleDensityExact;

    /** 关键词选品明细响应参数：市场周期；S11,S12 */
    @Schema(description = "关键词选品明细响应参数：市场周期；S11,S12")
    private String marketPeriod;

    /** 关键词选品明细响应参数：品牌；Fujifilm */
    @Schema(description = "关键词选品明细响应参数：品牌；Fujifilm")
    private String brand;

    /** 关键词选品明细响应参数：是否存在品牌词；false */
    @Schema(description = "关键词选品明细响应参数：是否存在品牌词；false")
    private Boolean hasBrandWord;

    /** 关键词选品明细响应参数：中文翻译；宝丽来相机 */
    @Schema(description = "关键词选品明细响应参数：中文翻译；宝丽来相机")
    private String keywordCn;

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
    @Schema(description = "关键词选品明细响应参数：类目")
    public static class SearchDepartmentsVo {

        /** 关键词选品明细响应参数：类目代码；electronics */
        @Schema(description = "关键词选品明细响应参数：类目代码；electronics")
        private String code;

        /** 关键词选品明细响应参数：类目名称；Electronics */
        @Schema(description = "关键词选品明细响应参数：类目名称；Electronics")
        private String label;

        /** 关键词选品明细响应参数：类目总计；141356 */
        @Schema(description = "关键词选品明细响应参数：类目总计；141356")
        private Integer total;

        /** 关键词选品明细响应参数：类目占比；1 */
        @Schema(description = "关键词选品明细响应参数：类目占比；1")
        private BigDecimal ratio;

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

    @Slf4j

    @Data
    @Schema(description = "关键词选品明细响应参数：关键词关联asin；4.8")
    public static class RelationAsinListVo {

        /** 关键词选品明细响应参数：价格；59.95 */
        @Schema(description = "关键词选品明细响应参数：价格；59.95")
        private BigDecimal price;

        /** 关键词选品明细响应参数：评分数；20115 */
        @Schema(description = "关键词选品明细响应参数：评分数；20115")
        private Integer ratings;

        /** 关键词选品明细响应参数：评分；4.7 */
        @Schema(description = "关键词选品明细响应参数：评分；4.7")
        private BigDecimal rating;

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

    @Slf4j

    @Data
    @Schema(description = "关键词选品明细响应参数：点击前三ASIN")
    public static class AraAsinListVo {

        /** 关键词选品明细响应参数：asin；B099VDRGG1 */
        @Schema(description = "关键词选品明细响应参数：asin；B099VDRGG1")
        private String asin;

        /** 关键词选品明细响应参数：title；Fujifilm Instax Mini 9 */
        @Schema(description = "关键词选品明细响应参数：title；Fujifilm Instax Mini 9")
        private String title;

        /** 关键词选品明细响应参数：图片；https://m.media-amazon.com/images/I/51aZiZaicYL._AC_US200_.jpg */
        @Schema(description = "关键词选品明细响应参数：图片；https://m.media-amazon.com/images/I/51aZiZaicYL._AC_US200_.jpg")
        private String imageUrl;

        /** 关键词选品明细响应参数：点击率；0.116 */
        @Schema(description = "关键词选品明细响应参数：点击率；0.116")
        private BigDecimal clickRate;

        /** 关键词选品明细响应参数：转化率；0.1217 */
        @Schema(description = "关键词选品明细响应参数：转化率；0.1217")
        private BigDecimal conversionShareRate;

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
