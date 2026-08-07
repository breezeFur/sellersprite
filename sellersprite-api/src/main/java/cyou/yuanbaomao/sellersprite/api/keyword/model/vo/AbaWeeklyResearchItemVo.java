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
 * ABA 数据选品-按周明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "ABA 数据选品-按周明细响应模型")
public class AbaWeeklyResearchItemVo {

    /** ABA 数据选品-按周明细响应参数：市场；US */
    @Schema(description = "ABA 数据选品-按周明细响应参数：市场；US")
    private String marketplace;

    /** ABA 数据选品-按周明细响应参数：查询日期；20230610，限定为周六的日期 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：查询日期；20230610，限定为周六的日期")
    private String date;

    /** ABA 数据选品-按周明细响应参数：关键词；portable charger */
    @Schema(description = "ABA 数据选品-按周明细响应参数：关键词；portable charger")
    private String keyword;

    /** ABA 数据选品-按周明细响应参数：关键词中文；便携式充电器 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：关键词中文；便携式充电器")
    private String keywordCn;

    /** ABA 数据选品-按周明细响应参数：关键词日文 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：关键词日文")
    private String keywordJp;

    /** ABA 数据选品-按周明细响应参数：类目；["Cell Phones & Accessories"] */
    @Schema(description = "ABA 数据选品-按周明细响应参数：类目；[\"Cell Phones & Accessories\"]")
    private List<String> departments;

    /** ABA 数据选品-按周明细响应参数：搜索排名；62 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：搜索排名；62")
    private Integer searchRank;

    /** ABA 数据选品-按周明细响应参数：排名增长量；19 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：排名增长量；19")
    private Integer searchRankCv;

    /** ABA 数据选品-按周明细响应参数：排名增长率；0.2346 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：排名增长率；0.2346")
    private BigDecimal searchRankCr;

    /** ABA 数据选品-按周明细响应参数：搜索量；46147979 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：搜索量；46147979")
    private Integer searches;

    /** ABA 数据选品-按周明细响应参数：购买量；2492 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：购买量；2492")
    private Integer purchases;

    /** ABA 数据选品-按周明细响应参数：购买率；0.0054 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：购买率；0.0054")
    private BigDecimal purchaseRate;

    /** ABA 数据选品-按周明细响应参数：点击量；1380 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：点击量；1380")
    private Integer clicks;

    /** ABA 数据选品-按周明细响应参数：展示量；73560 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：展示量；73560")
    private JsonNode impressions;

    /** ABA 数据选品-按周明细响应参数：首页商品标题中包含该关键词的商品数(精确匹配) */
    @Schema(description = "ABA 数据选品-按周明细响应参数：首页商品标题中包含该关键词的商品数(精确匹配)")
    private Integer titleDensityExact;

    /** ABA 数据选品-按周明细响应参数：精确 CPR（8天内确保关键词上首页的销量数） */
    @Schema(description = "ABA 数据选品-按周明细响应参数：精确 CPR（8天内确保关键词上首页的销量数）")
    private Integer cprExact;

    /** ABA 数据选品-按周明细响应参数：上周的排名 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：上周的排名")
    private Integer w1SearchRank;

    /** ABA 数据选品-按周明细响应参数：上周的排名变化值 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：上周的排名变化值")
    private Integer w1RankGrowthValue;

    /** ABA 数据选品-按周明细响应参数：上周的排名变化率 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：上周的排名变化率")
    private BigDecimal w1RankGrowthRate;

    /** ABA 数据选品-按周明细响应参数：4周前的排名 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：4周前的排名")
    private Integer w4SearchRank;

    /** ABA 数据选品-按周明细响应参数：4周前的排名变化值 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：4周前的排名变化值")
    private Integer w4RankGrowthValue;

    /** ABA 数据选品-按周明细响应参数：4周前的排名变化率 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：4周前的排名变化率")
    private BigDecimal w4RankGrowthRate;

    /** ABA 数据选品-按周明细响应参数：12周前的排名 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：12周前的排名")
    private Integer w12SearchRank;

    /** ABA 数据选品-按周明细响应参数：12周前的排名变化值 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：12周前的排名变化值")
    private Integer w12RankGrowthValue;

    /** ABA 数据选品-按周明细响应参数：12周前的排名变化率 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：12周前的排名变化率")
    private BigDecimal w12RankGrowthRate;

    /** ABA 数据选品-按周明细响应参数：点击前三品牌 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：点击前三品牌")
    private List<String> top3Brands;

    /** ABA 数据选品-按周明细响应参数：ppc竞价 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：ppc竞价")
    private BigDecimal bid;

    /** ABA 数据选品-按周明细响应参数：最大ppc竞价 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：最大ppc竞价")
    private BigDecimal bidMax;

    /** ABA 数据选品-按周明细响应参数：最小ppc竞价 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：最小ppc竞价")
    private BigDecimal bidMin;

    /** ABA 数据选品-按周明细响应参数：前三点击asin */
    @Schema(description = "ABA 数据选品-按周明细响应参数：前三点击asin")
    private List<Top3AsinDtoListVo> top3AsinDtoList;

    /** ABA 数据选品-按周明细响应参数：前三点击比；54.2 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：前三点击比；54.2")
    private BigDecimal clickShareRate;

    /** ABA 数据选品-按周明细响应参数：前三转化总比；43.5 */
    @Schema(description = "ABA 数据选品-按周明细响应参数：前三转化总比；43.5")
    private BigDecimal cvsShareRate;

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
    @Schema(description = "ABA 数据选品-按周明细响应参数：前三点击asin")
    public static class Top3AsinDtoListVo {

        /** ABA 数据选品-按周明细响应参数：asin */
        @Schema(description = "ABA 数据选品-按周明细响应参数：asin")
        private String asin;

        /** ABA 数据选品-按周明细响应参数：图片URL */
        @Schema(description = "ABA 数据选品-按周明细响应参数：图片URL")
        private String imageUrl;

        /** ABA 数据选品-按周明细响应参数：点击集中度 */
        @Schema(description = "ABA 数据选品-按周明细响应参数：点击集中度")
        private BigDecimal clickRate;

        /** ABA 数据选品-按周明细响应参数：转化率 */
        @Schema(description = "ABA 数据选品-按周明细响应参数：转化率")
        private BigDecimal conversionRate;

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
