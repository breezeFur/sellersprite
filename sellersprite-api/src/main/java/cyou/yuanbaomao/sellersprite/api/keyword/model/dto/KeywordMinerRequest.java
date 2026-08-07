// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.keyword.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 关键词挖掘请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关键词挖掘请求模型")
public class KeywordMinerRequest {

    /** 关键词挖掘请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "关键词挖掘请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 关键词挖掘请求参数：历史日期，yyyyMM格式，最近30天不传或传空字符串；202201 */
    @Schema(description = "关键词挖掘请求参数：历史日期，yyyyMM格式，最近30天不传或传空字符串；202201")
    private String historyDate;

    /** 关键词挖掘请求参数：关键词 */
    @NotBlank
    @Schema(description = "关键词挖掘请求参数：关键词")
    private String keyword;

    /** 关键词挖掘请求参数：批量查询关键词；["phone stand"] */
    @Schema(description = "关键词挖掘请求参数：批量查询关键词；[\"phone stand\"]")
    private List<String> keywordList;

    /** 关键词挖掘请求参数：最小搜索量；543 */
    @Schema(description = "关键词挖掘请求参数：最小搜索量；543")
    private Integer minSearch;

    /** 关键词挖掘请求参数：最大搜索量；23453 */
    @Schema(description = "关键词挖掘请求参数：最大搜索量；23453")
    private Integer maxSearch;

    /** 关键词挖掘请求参数：最小购买量；6 */
    @Schema(description = "关键词挖掘请求参数：最小购买量；6")
    private Integer minPurchases;

    /** 关键词挖掘请求参数：最大购买量；34 */
    @Schema(description = "关键词挖掘请求参数：最大购买量；34")
    private Integer maxPurchases;

    /** 关键词挖掘请求参数：最小购买率；3 */
    @Schema(description = "关键词挖掘请求参数：最小购买率；3")
    private BigDecimal minPurchasesRate;

    /** 关键词挖掘请求参数：最大购买率；43 */
    @Schema(description = "关键词挖掘请求参数：最大购买率；43")
    private BigDecimal maxPurchasesRate;

    /** 关键词挖掘请求参数：最小SPR；2 */
    @Schema(description = "关键词挖掘请求参数：最小SPR；2")
    private Integer minSPR;

    /** 关键词挖掘请求参数：最大SPR；16 */
    @Schema(description = "关键词挖掘请求参数：最大SPR；16")
    private Integer maxSPR;

    /** 关键词挖掘请求参数：最小标题密度；2 */
    @Schema(description = "关键词挖掘请求参数：最小标题密度；2")
    private Integer minTitleDensity;

    /** 关键词挖掘请求参数：最大标题密度；23 */
    @Schema(description = "关键词挖掘请求参数：最大标题密度；23")
    private Integer maxTitleDensity;

    /** 关键词挖掘请求参数：最小相关度；23，最小0 */
    @Schema(description = "关键词挖掘请求参数：最小相关度；23，最小0")
    private BigDecimal minRelevancy;

    /** 关键词挖掘请求参数：最大相关度；90，最大100 */
    @Schema(description = "关键词挖掘请求参数：最大相关度；90，最大100")
    private BigDecimal maxRelevancy;

    /** 关键词挖掘请求参数：最小搜索排名；33 */
    @Schema(description = "关键词挖掘请求参数：最小搜索排名；33")
    private Integer minSearchRank;

    /** 关键词挖掘请求参数：最大搜索排名；3223 */
    @Schema(description = "关键词挖掘请求参数：最大搜索排名；3223")
    private Integer maxSearchRank;

    /** 关键词挖掘请求参数：最小商品数；54 */
    @Schema(description = "关键词挖掘请求参数：最小商品数；54")
    private Integer minProducts;

    /** 关键词挖掘请求参数：最大商品数；324 */
    @Schema(description = "关键词挖掘请求参数：最大商品数；324")
    private Integer maxProducts;

    /** 关键词挖掘请求参数：最小供需比；11.2 */
    @Schema(description = "关键词挖掘请求参数：最小供需比；11.2")
    private BigDecimal minSupplyDemandRatio;

    /** 关键词挖掘请求参数：最大供需比；45.2 */
    @Schema(description = "关键词挖掘请求参数：最大供需比；45.2")
    private BigDecimal maxSupplyDemandRatio;

    /** 关键词挖掘请求参数：最小广告竞品数；123 */
    @Schema(description = "关键词挖掘请求参数：最小广告竞品数；123")
    private Integer minAdProducts;

    /** 关键词挖掘请求参数：最大广告竞品数；345 */
    @Schema(description = "关键词挖掘请求参数：最大广告竞品数；345")
    private Integer maxAdProducts;

    /** 关键词挖掘请求参数：最小单词个数；2 */
    @Schema(description = "关键词挖掘请求参数：最小单词个数；2")
    private Integer minWordCount;

    /** 关键词挖掘请求参数：最大单词个数；4 */
    @Schema(description = "关键词挖掘请求参数：最大单词个数；4")
    private Integer maxWordCount;

    /** 关键词挖掘请求参数：最小点击集中度；23.4 */
    @Schema(description = "关键词挖掘请求参数：最小点击集中度；23.4")
    private BigDecimal minMonopolyClickRate;

    /** 关键词挖掘请求参数：最大点击集中度；53.1 */
    @Schema(description = "关键词挖掘请求参数：最大点击集中度；53.1")
    private BigDecimal maxMonopolyClickRate;

    /** 关键词挖掘请求参数：最小ppc竞价；10.2 */
    @Schema(description = "关键词挖掘请求参数：最小ppc竞价；10.2")
    private BigDecimal minBid;

    /** 关键词挖掘请求参数：最大ppc竞价；23.1 */
    @Schema(description = "关键词挖掘请求参数：最大ppc竞价；23.1")
    private BigDecimal maxBid;

    /** 关键词挖掘请求参数：最小均价；43.3 */
    @Schema(description = "关键词挖掘请求参数：最小均价；43.3")
    private BigDecimal minPrice;

    /** 关键词挖掘请求参数：最大均价；234.2 */
    @Schema(description = "关键词挖掘请求参数：最大均价；234.2")
    private BigDecimal maxPrice;

    /** 关键词挖掘请求参数：最小评分数；100 */
    @Schema(description = "关键词挖掘请求参数：最小评分数；100")
    private Integer minRatings;

    /** 关键词挖掘请求参数：最大评分数；399 */
    @Schema(description = "关键词挖掘请求参数：最大评分数；399")
    private Integer maxRatings;

    /** 关键词挖掘请求参数：最小评分值；3 */
    @Schema(description = "关键词挖掘请求参数：最小评分值；3")
    private BigDecimal minRating;

    /** 关键词挖掘请求参数：最大评分值；4.9 */
    @Schema(description = "关键词挖掘请求参数：最大评分值；4.9")
    private BigDecimal maxRating;

    /** 关键词挖掘请求参数：亚马逊推荐词；true */
    @Schema(description = "关键词挖掘请求参数：亚马逊推荐词；true")
    private Boolean amazonChoice;

    /** 关键词挖掘请求参数：过滤词根 0包含所有 1只包含词根；0 */
    @Schema(description = "关键词挖掘请求参数：过滤词根 0包含所有 1只包含词根；0")
    private Integer filterRootWord;

    /** 关键词挖掘请求参数：2: 广泛匹配, 3: 词组匹配；2 */
    @Schema(description = "关键词挖掘请求参数：2: 广泛匹配, 3: 词组匹配；2")
    private Integer matchType;

    /** 关键词挖掘请求参数：包含的词；["phone stand"] */
    @Schema(description = "关键词挖掘请求参数：包含的词；[\"phone stand\"]")
    private List<String> includeKeywords;

    /** 关键词挖掘请求参数：排除的词；["phone stand"] */
    @Schema(description = "关键词挖掘请求参数：排除的词；[\"phone stand\"]")
    private List<String> excludeKeywords;

    /** 关键词挖掘请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "关键词挖掘请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 关键词挖掘请求参数：每页条数；默认：50，最大：100 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 100, message = "size 不能大于 100")
    @Schema(description = "关键词挖掘请求参数：每页条数；默认：50，最大：100")
    private Integer size = 50;

    /** 关键词挖掘请求参数：排序 */
    @Schema(description = "关键词挖掘请求参数：排序")
    private SortOrder order;

}
