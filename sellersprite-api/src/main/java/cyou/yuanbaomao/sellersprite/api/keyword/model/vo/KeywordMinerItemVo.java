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
 * 关键词挖掘明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "关键词挖掘明细响应模型")
public class KeywordMinerItemVo {

    /** 关键词挖掘明细响应参数：市场，见表 1.2；US */
    @Schema(description = "关键词挖掘明细响应参数：市场，见表 1.2；US")
    private String marketplace;

    /** 关键词挖掘明细响应参数：关键词；phone stand for recording */
    @Schema(description = "关键词挖掘明细响应参数：关键词；phone stand for recording")
    private String keyword;

    /** 关键词挖掘明细响应参数：关键词中文翻译；用于录音的电话支架 */
    @Schema(description = "关键词挖掘明细响应参数：关键词中文翻译；用于录音的电话支架")
    private String keywordCn;

    /** 关键词挖掘明细响应参数：关键词英文翻译；録音用電話スタンド */
    @Schema(description = "关键词挖掘明细响应参数：关键词英文翻译；録音用電話スタンド")
    private String keywordJp;

    /** 关键词挖掘明细响应参数：类目 */
    @Schema(description = "关键词挖掘明细响应参数：类目")
    private List<DepartmentsVo> departments;

    /** 关键词挖掘明细响应参数：搜索月份；2022.01 */
    @Schema(description = "关键词挖掘明细响应参数：搜索月份；2022.01")
    private String month;

    /** 关键词挖掘明细响应参数：是否属于补充关键词（无当前月搜索量）；N */
    @Schema(description = "关键词挖掘明细响应参数：是否属于补充关键词（无当前月搜索量）；N")
    private String supplement;

    /** 关键词挖掘明细响应参数：搜索量；21582 */
    @Schema(description = "关键词挖掘明细响应参数：搜索量；21582")
    private Integer searches;

    /** 关键词挖掘明细响应参数：月购买量；1996 */
    @Schema(description = "关键词挖掘明细响应参数：月购买量；1996")
    private Integer purchases;

    /** 关键词挖掘明细响应参数：月购买率；0.0925 */
    @Schema(description = "关键词挖掘明细响应参数：月购买率；0.0925")
    private BigDecimal purchaseRate;

    /** 关键词挖掘明细响应参数：点击垄断率；0.3 */
    @Schema(description = "关键词挖掘明细响应参数：点击垄断率；0.3")
    private BigDecimal monopolyClickRate;

    /** 关键词挖掘明细响应参数：商品数；1645 */
    @Schema(description = "关键词挖掘明细响应参数：商品数；1645")
    private Integer products;

    /** 关键词挖掘明细响应参数：广告竞品数；34 */
    @Schema(description = "关键词挖掘明细响应参数：广告竞品数；34")
    private Integer adProducts;

    /** 关键词挖掘明细响应参数：供需比；13.12 */
    @Schema(description = "关键词挖掘明细响应参数：供需比；13.12")
    private BigDecimal supplyDemandRatio;

    /** 关键词挖掘明细响应参数：平均价格；36.14 */
    @Schema(description = "关键词挖掘明细响应参数：平均价格；36.14")
    private BigDecimal avgPrice;

    /** 关键词挖掘明细响应参数：平均评分数；12223 */
    @Schema(description = "关键词挖掘明细响应参数：平均评分数；12223")
    private Integer avgRatings;

    /** 关键词挖掘明细响应参数：平均评分值；4.5 */
    @Schema(description = "关键词挖掘明细响应参数：平均评分值；4.5")
    private BigDecimal avgRating;

    /** 关键词挖掘明细响应参数：最小PPC价格；1.34 */
    @Schema(description = "关键词挖掘明细响应参数：最小PPC价格；1.34")
    private BigDecimal bidMin;

    /** 关键词挖掘明细响应参数：最大PPC价格；3.21 */
    @Schema(description = "关键词挖掘明细响应参数：最大PPC价格；3.21")
    private BigDecimal bidMax;

    /** 关键词挖掘明细响应参数：PPC价格；1.6 */
    @Schema(description = "关键词挖掘明细响应参数：PPC价格；1.6")
    private BigDecimal bid;

    /** 关键词挖掘明细响应参数：转化共享率；0.3084 */
    @Schema(description = "关键词挖掘明细响应参数：转化共享率；0.3084")
    private BigDecimal cvsShareRate;

    /** 关键词挖掘明细响应参数：单词个数；4 */
    @Schema(description = "关键词挖掘明细响应参数：单词个数；4")
    private Integer wordCount;

    /** 关键词挖掘明细响应参数：标题密度；42.9 */
    @Schema(description = "关键词挖掘明细响应参数：标题密度；42.9")
    private Integer titleDensity;

    /** 关键词挖掘明细响应参数：SPR；6 */
    @Schema(description = "关键词挖掘明细响应参数：SPR；6")
    private Integer spr;

    /** 关键词挖掘明细响应参数：相关度；28.6 */
    @Schema(description = "关键词挖掘明细响应参数：相关度；28.6")
    private BigDecimal relevancy;

    /** 关键词挖掘明细响应参数：亚马逊推荐词 true是的 false不是；false */
    @Schema(description = "关键词挖掘明细响应参数：亚马逊推荐词 true是的 false不是；false")
    private Boolean amazonChoice;

    /** 关键词挖掘明细响应参数：搜索排名；17910 */
    @Schema(description = "关键词挖掘明细响应参数：搜索排名；17910")
    private Integer searchRank;

    /** 关键词挖掘明细响应参数：点击量；10 */
    @Schema(description = "关键词挖掘明细响应参数：点击量；10")
    private Integer clicks;

    /** 关键词挖掘明细响应参数：展示量；20 */
    @Schema(description = "关键词挖掘明细响应参数：展示量；20")
    private Long impressions;

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
    @Schema(description = "关键词挖掘明细响应参数：类目")
    public static class DepartmentsVo {

        /** 关键词挖掘明细响应参数：类目代码；electronics */
        @Schema(description = "关键词挖掘明细响应参数：类目代码；electronics")
        private String code;

        /** 关键词挖掘明细响应参数：类目名称；Electronics */
        @Schema(description = "关键词挖掘明细响应参数：类目名称；Electronics")
        private String label;

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
