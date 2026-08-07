// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.keyword.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 拓展流量词请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "拓展流量词请求模型")
public class TrafficKeywordExtendRequest {

    /** 拓展流量词请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "拓展流量词请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 拓展流量词请求参数：历史日期，yyyyMM格式，最近30天不传或传空字符串；202201 */
    @Schema(description = "拓展流量词请求参数：历史日期，yyyyMM格式，最近30天不传或传空字符串；202201")
    private String historyDate;

    /** 拓展流量词请求参数：asin列表(最多20)；["B07Z82895W"] */
    @NotEmpty
    @Size(max = 20, message = "asinList 最多允许 20 项")
    @Schema(description = "拓展流量词请求参数：asin列表(最多20)；[\"B07Z82895W\"]")
    private List<String> asinList;

    /** 拓展流量词请求参数：查询方式 0 所有变体 1畅销变体 2当前变体，默认2；2 */
    @Schema(description = "拓展流量词请求参数：查询方式 0 所有变体 1畅销变体 2当前变体，默认2；2")
    private Integer queryType;

    /** 拓展流量词请求参数：最小月搜索量；100 */
    @Schema(description = "拓展流量词请求参数：最小月搜索量；100")
    private Integer minSearches;

    /** 拓展流量词请求参数：最大月搜索量；300 */
    @Schema(description = "拓展流量词请求参数：最大月搜索量；300")
    private Integer maxSearches;

    /** 拓展流量词请求参数：最小搜索排名；33 */
    @Schema(description = "拓展流量词请求参数：最小搜索排名；33")
    private Integer minSearchRank;

    /** 拓展流量词请求参数：最大搜索排名；3223 */
    @Schema(description = "拓展流量词请求参数：最大搜索排名；3223")
    private Integer maxSearchRank;

    /** 拓展流量词请求参数：最小购买量；6 */
    @Schema(description = "拓展流量词请求参数：最小购买量；6")
    private Integer minPurchases;

    /** 拓展流量词请求参数：最大购买量；34 */
    @Schema(description = "拓展流量词请求参数：最大购买量；34")
    private Integer maxPurchases;

    /** 拓展流量词请求参数：最小购买率；3 */
    @Schema(description = "拓展流量词请求参数：最小购买率；3")
    private BigDecimal minPurchaseRate;

    /** 拓展流量词请求参数：最大购买率；43 */
    @Schema(description = "拓展流量词请求参数：最大购买率；43")
    private BigDecimal maxPurchaseRate;

    /** 拓展流量词请求参数：最小商品数；10 */
    @Schema(description = "拓展流量词请求参数：最小商品数；10")
    private Integer minProducts;

    /** 拓展流量词请求参数：最大商品数；90 */
    @Schema(description = "拓展流量词请求参数：最大商品数；90")
    private Integer maxProducts;

    /** 拓展流量词请求参数：最小供需比；11.2 */
    @Schema(description = "拓展流量词请求参数：最小供需比；11.2")
    private BigDecimal minSupplyDemandRatio;

    /** 拓展流量词请求参数：最大供需比；45.2 */
    @Schema(description = "拓展流量词请求参数：最大供需比；45.2")
    private BigDecimal maxSupplyDemandRatio;

    /** 拓展流量词请求参数：最小ppc竞价；10.2 */
    @Schema(description = "拓展流量词请求参数：最小ppc竞价；10.2")
    private BigDecimal minBid;

    /** 拓展流量词请求参数：最大ppc竞价；23.1 */
    @Schema(description = "拓展流量词请求参数：最大ppc竞价；23.1")
    private BigDecimal maxBid;

    /** 拓展流量词请求参数：最小广告竞品数；123 */
    @Schema(description = "拓展流量词请求参数：最小广告竞品数；123")
    private Integer minAdProducts;

    /** 拓展流量词请求参数：最大广告竞品数；345 */
    @Schema(description = "拓展流量词请求参数：最大广告竞品数；345")
    private Integer maxAdProducts;

    /** 拓展流量词请求参数：最小均价；20 */
    @Schema(description = "拓展流量词请求参数：最小均价；20")
    private BigDecimal minAvgPrice;

    /** 拓展流量词请求参数：最大均价；30.3 */
    @Schema(description = "拓展流量词请求参数：最大均价；30.3")
    private BigDecimal maxAvgPrice;

    /** 拓展流量词请求参数：最小单词个数；2 */
    @Schema(description = "拓展流量词请求参数：最小单词个数；2")
    private Integer minWordCount;

    /** 拓展流量词请求参数：最大单词个数；4 */
    @Schema(description = "拓展流量词请求参数：最大单词个数；4")
    private Integer maxWordCount;

    /** 拓展流量词请求参数：包含的词；["phone stand"] */
    @Schema(description = "拓展流量词请求参数：包含的词；[\"phone stand\"]")
    private List<String> includeKeywords;

    /** 拓展流量词请求参数：排除的词；["phone stand"] */
    @Schema(description = "拓展流量词请求参数：排除的词；[\"phone stand\"]")
    private List<String> excludeKeywords;

    /** 拓展流量词请求参数：最小SPR；2 */
    @Schema(description = "拓展流量词请求参数：最小SPR；2")
    private Integer minSPR;

    /** 拓展流量词请求参数：最大SPR；16 */
    @Schema(description = "拓展流量词请求参数：最大SPR；16")
    private Integer maxSPR;

    /** 拓展流量词请求参数：最小标题密度；2 */
    @Schema(description = "拓展流量词请求参数：最小标题密度；2")
    private Integer minTitleDensity;

    /** 拓展流量词请求参数：最大标题密度；23 */
    @Schema(description = "拓展流量词请求参数：最大标题密度；23")
    private Integer maxTitleDensity;

    /** 拓展流量词请求参数：最小点击集中度；23.4 */
    @Schema(description = "拓展流量词请求参数：最小点击集中度；23.4")
    private BigDecimal minMonopolyClickRate;

    /** 拓展流量词请求参数：最大点击集中度；53.1 */
    @Schema(description = "拓展流量词请求参数：最大点击集中度；53.1")
    private BigDecimal maxMonopolyClickRate;

    /** 拓展流量词请求参数：最小流量占比；45 */
    @Schema(description = "拓展流量词请求参数：最小流量占比；45")
    private BigDecimal minTrafficPercentage;

    /** 拓展流量词请求参数：最大流量占比；23 */
    @Schema(description = "拓展流量词请求参数：最大流量占比；23")
    private BigDecimal maxTrafficPercentage;

    /** 拓展流量词请求参数：最小转化率；0.23 */
    @Schema(description = "拓展流量词请求参数：最小转化率；0.23")
    private BigDecimal minConversionRate;

    /** 拓展流量词请求参数：最大转化率；1.4 */
    @Schema(description = "拓展流量词请求参数：最大转化率；1.4")
    private BigDecimal maxConversionRate;

    /** 拓展流量词请求参数：最小asin数；4 */
    @Schema(description = "拓展流量词请求参数：最小asin数；4")
    private Integer minCompetitors;

    /** 拓展流量词请求参数：最大asin数；23 */
    @Schema(description = "拓展流量词请求参数：最大asin数；23")
    private Integer maxCompetitors;

    /** 拓展流量词请求参数：亚马逊推荐词；TRUE */
    @Schema(description = "拓展流量词请求参数：亚马逊推荐词；TRUE")
    private Boolean amazonChoice;

    /** 拓展流量词请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "拓展流量词请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 拓展流量词请求参数：每页条数，最大50；默认：50 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 50, message = "size 不能大于 50")
    @Schema(description = "拓展流量词请求参数：每页条数，最大50；默认：50")
    private Integer size = 50;

    /** 拓展流量词请求参数：排序 */
    @Schema(description = "拓展流量词请求参数：排序")
    private SortOrder order;

}
