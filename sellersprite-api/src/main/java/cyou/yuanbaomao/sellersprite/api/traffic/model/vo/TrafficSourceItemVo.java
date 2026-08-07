// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.traffic.model.vo;

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
 * 查流量来源(关键词流向)明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "查流量来源(关键词流向)明细响应模型")
public class TrafficSourceItemVo {

    /** 查流量来源(关键词流向)明细响应参数：全部流量词；1 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：全部流量词；1")
    private Integer keywords;

    /** 查流量来源(关键词流向)明细响应参数：自然搜索词；12 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：自然搜索词；12")
    private Integer searchKeywords;

    /** 查流量来源(关键词流向)明细响应参数：AC推荐词；13 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：AC推荐词；13")
    private String acKeywords;

    /** 查流量来源(关键词流向)明细响应参数：ER推荐词；13 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：ER推荐词；13")
    private Integer editorialKeywords;

    /** 查流量来源(关键词流向)明细响应参数：4星推荐词；14 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：4星推荐词；14")
    private Integer fourStarsKeywords;

    /** 查流量来源(关键词流向)明细响应参数：HR推荐词；1 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：HR推荐词；1")
    private Integer hrKeywords;

    /** 查流量来源(关键词流向)明细响应参数：SP广告词；3 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：SP广告词；3")
    private Integer adKeywords;

    /** 查流量来源(关键词流向)明细响应参数：视频广告词；4 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：视频广告词；4")
    private Integer videoKeywords;

    /** 查流量来源(关键词流向)明细响应参数：品牌广告词；5 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：品牌广告词；5")
    private Integer brandKeywords;

    /** 查流量来源(关键词流向)明细响应参数：流量来源概览；[“SEARCH”, “OFFICIAL”, “AD”] */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：流量来源概览；[“SEARCH”, “OFFICIAL”, “AD”]")
    private List<String> badgeLabels;

    /** 查流量来源(关键词流向)明细响应参数：流量来源明细；{“SEARCH”: [“NATURAL_SEARCHING”],”OFFICIAL”: [“AMAZON_CHOICE”],”AD”: [“SPONSOR_BRAND”,”SPONSOR_VIDEO”,”HIGHLY_RATED”,”ADS”]} */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：流量来源明细；{“SEARCH”: [“NATURAL_SEARCHING”],”OFFICIAL”: [“AMAZON_CHOICE”],”AD”: [“SPONSOR_BRAND”,”SPONSOR_VIDEO”,”HIGHLY_RATED”,”ADS”]}")
    private JsonNode badgeDetails;

    /** 查流量来源(关键词流向)明细响应参数：Asin相关信息 */
    @Schema(description = "查流量来源(关键词流向)明细响应参数：Asin相关信息")
    private AsinInfoVo asinInfo;

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
    @Schema(description = "查流量来源(关键词流向)明细响应参数：Asin相关信息")
    public static class AsinInfoVo {

        /** 查流量来源(关键词流向)明细响应参数：asin；B078J8VPVW */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：asin；B078J8VPVW")
        private String asin;

        /** 查流量来源(关键词流向)明细响应参数：该asin对应亚马逊地址；https://www.amazon.com/dp/B08GHW4TBS */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：该asin对应亚马逊地址；https://www.amazon.com/dp/B08GHW4TBS")
        private String asinUrl;

        /** 查流量来源(关键词流向)明细响应参数：货币code；$ */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：货币code；$")
        private String currency;

        /** 查流量来源(关键词流向)明细响应参数：价格；23 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：价格；23")
        private BigDecimal price;

        /** 查流量来源(关键词流向)明细响应参数：评分；234 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：评分；234")
        private BigDecimal rating;

        /** 查流量来源(关键词流向)明细响应参数：评分数；23 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：评分数；23")
        private Integer reviews;

        /** 查流量来源(关键词流向)明细响应参数：标题；Diapers Size 2, 186 Count - Pampers Swaddlers Disposable Baby Diapers, ONE MONTH SUPPLY */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：标题；Diapers Size 2, 186 Count - Pampers Swaddlers Disposable Baby Diapers, ONE MONTH SUPPLY")
        private String title;

        /** 查流量来源(关键词流向)明细响应参数：sku；["Color: Beige","Size: 47 inches"] */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：sku；[\"Color: Beige\",\"Size: 47 inches\"]")
        private String sku;

        /** 查流量来源(关键词流向)明细响应参数：变体数；2 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：变体数；2")
        private Integer variations;

        /** 查流量来源(关键词流向)明细响应参数：类目ID；12097479011 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：类目ID；12097479011")
        private Long nodeId;

        /** 查流量来源(关键词流向)明细响应参数：类目ID路径；172282:24046923011:172541:12097479011 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：类目ID路径；172282:24046923011:172541:12097479011")
        private String nodeIdPath;

        /** 查流量来源(关键词流向)明细响应参数：类目路径；Electronics:Headphones, Earbuds & Accessories:Headphones & Earbuds:Over-Ear Headphones */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：类目路径；Electronics:Headphones, Earbuds & Accessories:Headphones & Earbuds:Over-Ear Headphones")
        private String nodeLabelPath;

        /** 查流量来源(关键词流向)明细响应参数：大类排名(BSR)；175204 */
        @Schema(description = "查流量来源(关键词流向)明细响应参数：大类排名(BSR)；175204")
        private Long bsrRank;

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
