// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 关键词选品请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关键词选品请求模型")
public class KeywordResearchRequest {

    /** 关键词选品请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "关键词选品请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 关键词选品请求参数：筛选日期,yyyyMM格式，支持近24个月的；202203 */
    @Schema(description = "关键词选品请求参数：筛选日期,yyyyMM格式，支持近24个月的；202203")
    private String month;

    /** 关键词选品请求参数：查询类目，见关键词选品类目接口，传递code；["automotive","baby-products"] */
    @Schema(description = "关键词选品请求参数：查询类目，见关键词选品类目接口，传递code；[\"automotive\",\"baby-products\"]")
    private List<String> departments;

    /** 关键词选品请求参数：关键词；N95 */
    @Schema(description = "关键词选品请求参数：关键词；N95")
    private String keywords;

    /** 关键词选品请求参数：排除的关键字；portable */
    @Schema(description = "关键词选品请求参数：排除的关键字；portable")
    private String excludeKeywords;

    /** 关键词选品请求参数：最小月搜索量；100 */
    @Schema(description = "关键词选品请求参数：最小月搜索量；100")
    private Integer minSearches;

    /** 关键词选品请求参数：最大月搜索量；300 */
    @Schema(description = "关键词选品请求参数：最大月搜索量；300")
    private Integer maxSearches;

    /** 关键词选品请求参数：最小月搜索量增长率；10 */
    @Schema(description = "关键词选品请求参数：最小月搜索量增长率；10")
    private BigDecimal minSearchesCr;

    /** 关键词选品请求参数：最大月搜索量增长率；50.8 */
    @Schema(description = "关键词选品请求参数：最大月搜索量增长率；50.8")
    private BigDecimal maxSearchesCr;

    /** 关键词选品请求参数：最小商品数；10 */
    @Schema(description = "关键词选品请求参数：最小商品数；10")
    private Integer minProducts;

    /** 关键词选品请求参数：最大商品数；90 */
    @Schema(description = "关键词选品请求参数：最大商品数；90")
    private Integer maxProducts;

    /** 关键词选品请求参数：最小购买量；100 */
    @Schema(description = "关键词选品请求参数：最小购买量；100")
    private Integer minPurchases;

    /** 关键词选品请求参数：最大购买量；500 */
    @Schema(description = "关键词选品请求参数：最大购买量；500")
    private Integer maxPurchases;

    /** 关键词选品请求参数：最小购买率；3.2 */
    @Schema(description = "关键词选品请求参数：最小购买率；3.2")
    private BigDecimal minPurchaseRate;

    /** 关键词选品请求参数：最大购买率；10.5 */
    @Schema(description = "关键词选品请求参数：最大购买率；10.5")
    private BigDecimal maxPurchaseRate;

    /** 关键词选品请求参数：新细分市场；false */
    @Schema(description = "关键词选品请求参数：新细分市场；false")
    private Boolean withYearlyGrowth;

    /** 关键词选品请求参数：最小月搜索量同比增长值；1000 */
    @Schema(description = "关键词选品请求参数：最小月搜索量同比增长值；1000")
    private Integer minSearchMonthCv;

    /** 关键词选品请求参数：最大月搜索量同比增长值；3000 */
    @Schema(description = "关键词选品请求参数：最大月搜索量同比增长值；3000")
    private Integer maxSearchMonthCv;

    /** 关键词选品请求参数：最小月搜索量同比增长率；5.3 */
    @Schema(description = "关键词选品请求参数：最小月搜索量同比增长率；5.3")
    private BigDecimal minSearchMonthCr;

    /** 关键词选品请求参数：最大月搜索量同比增长率；30.1 */
    @Schema(description = "关键词选品请求参数：最大月搜索量同比增长率；30.1")
    private BigDecimal maxSearchMonthCr;

    /** 关键词选品请求参数：最小月搜索量近3个月增长值；6000 */
    @Schema(description = "关键词选品请求参数：最小月搜索量近3个月增长值；6000")
    private Integer minSearchNearlyCv;

    /** 关键词选品请求参数：最大月搜索量近3个月增长值；20000 */
    @Schema(description = "关键词选品请求参数：最大月搜索量近3个月增长值；20000")
    private Integer maxSearchNearlyCv;

    /** 关键词选品请求参数：最小月搜索量近3个月增长率；10.3 */
    @Schema(description = "关键词选品请求参数：最小月搜索量近3个月增长率；10.3")
    private BigDecimal minSearchNearlyCr;

    /** 关键词选品请求参数：最大月搜索量近3个月增长率；20.4 */
    @Schema(description = "关键词选品请求参数：最大月搜索量近3个月增长率；20.4")
    private BigDecimal maxSearchNearlyCr;

    /** 关键词选品请求参数：市场周期；见表1.7 */
    @Schema(description = "关键词选品请求参数：市场周期；见表1.7")
    private String marketPeriod;

    /** 关键词选品请求参数：最小均价；20 */
    @Schema(description = "关键词选品请求参数：最小均价；20")
    private BigDecimal minAvgPrice;

    /** 关键词选品请求参数：最大均价；30.3 */
    @Schema(description = "关键词选品请求参数：最大均价；30.3")
    private BigDecimal maxAvgPrice;

    /** 关键词选品请求参数：最小评分数；2000 */
    @Schema(description = "关键词选品请求参数：最小评分数；2000")
    private Integer minRatings;

    /** 关键词选品请求参数：最大评分数；3000 */
    @Schema(description = "关键词选品请求参数：最大评分数；3000")
    private Integer maxRatings;

    /** 关键词选品请求参数：最小评分值；3.2 */
    @Schema(description = "关键词选品请求参数：最小评分值；3.2")
    private BigDecimal minRating;

    /** 关键词选品请求参数：最大评分值；4.1 */
    @Schema(description = "关键词选品请求参数：最大评分值；4.1")
    private BigDecimal maxRating;

    /** 关键词选品请求参数：最小PPC竞价；6.2 */
    @Schema(description = "关键词选品请求参数：最小PPC竞价；6.2")
    private BigDecimal minBid;

    /** 关键词选品请求参数：最大PPC竞价；10.6 */
    @Schema(description = "关键词选品请求参数：最大PPC竞价；10.6")
    private BigDecimal maxBid;

    /** 关键词选品请求参数：最小点击集中度；20.1 */
    @Schema(description = "关键词选品请求参数：最小点击集中度；20.1")
    private BigDecimal minAraClickRate;

    /** 关键词选品请求参数：最大点击集中度；56.4 */
    @Schema(description = "关键词选品请求参数：最大点击集中度；56.4")
    private BigDecimal maxAraClickRate;

    /** 关键词选品请求参数：最小货流值；10.1 */
    @Schema(description = "关键词选品请求参数：最小货流值；10.1")
    private BigDecimal minGoodsValue;

    /** 关键词选品请求参数：最大货流值；41.1 */
    @Schema(description = "关键词选品请求参数：最大货流值；41.1")
    private BigDecimal maxGoodsValue;

    /** 关键词选品请求参数：最小供需比；5.6 */
    @Schema(description = "关键词选品请求参数：最小供需比；5.6")
    private BigDecimal minSupplyDemandRatio;

    /** 关键词选品请求参数：最大供需比；10.4 */
    @Schema(description = "关键词选品请求参数：最大供需比；10.4")
    private BigDecimal maxSupplyDemandRatio;

    /** 关键词选品请求参数：最小单词个数；1 */
    @Schema(description = "关键词选品请求参数：最小单词个数；1")
    private Integer minWordCount;

    /** 关键词选品请求参数：最大单词个数；3 */
    @Schema(description = "关键词选品请求参数：最大单词个数；3")
    private Integer maxWordCount;

    /** 关键词选品请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "关键词选品请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 关键词选品请求参数：每页条数，默认15；最大：15 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 15, message = "size 不能大于 15")
    @Schema(description = "关键词选品请求参数：每页条数，默认15；最大：15")
    private Integer size = 15;

    /** 关键词选品请求参数：排序 */
    @Schema(description = "关键词选品请求参数：排序")
    private SortOrder order;

}
