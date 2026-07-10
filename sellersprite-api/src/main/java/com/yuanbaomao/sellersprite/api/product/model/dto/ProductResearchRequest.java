// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.product.model.dto;

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
 * 选产品请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选产品请求模型")
public class ProductResearchRequest {

    /** 选产品请求参数：市场编码；见表 1.2 */
    @NotNull
    @Schema(description = "选产品请求参数：市场编码；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 选产品请求参数：查询月份；格式：yyyyMM，示例：202507，见表 1.1 */
    @Schema(description = "选产品请求参数：查询月份；格式：yyyyMM，示例：202507，见表 1.1")
    private String month;

    /** 选产品请求参数：关键字；N95 */
    @Schema(description = "选产品请求参数：关键字；N95")
    private String keyword;

    /** 选产品请求参数：包含卖家 */
    @Schema(description = "选产品请求参数：包含卖家")
    private String includeSellers;

    /** 选产品请求参数：排除卖家 */
    @Schema(description = "选产品请求参数：排除卖家")
    private String excludeSellers;

    /** 选产品请求参数：匹配方式，1词组匹配 2模糊匹配 3精准匹配；默认2；2 */
    @Schema(description = "选产品请求参数：匹配方式，1词组匹配 2模糊匹配 3精准匹配；默认2；2")
    private Integer matchType;

    /** 选产品请求参数：排除的关键字；portable */
    @Schema(description = "选产品请求参数：排除的关键字；portable")
    private String excludeKeywords;

    /** 选产品请求参数：最低价格；10 */
    @Schema(description = "选产品请求参数：最低价格；10")
    private BigDecimal minPrice;

    /** 选产品请求参数：最高价格；30 */
    @Schema(description = "选产品请求参数：最高价格；30")
    private BigDecimal maxPrice;

    /** 选产品请求参数：最低评分值；1 */
    @Schema(description = "选产品请求参数：最低评分值；1")
    private BigDecimal minRating;

    /** 选产品请求参数：最高评分值；5 */
    @Schema(description = "选产品请求参数：最高评分值；5")
    private BigDecimal maxRating;

    /** 选产品请求参数：最低评分数；1 */
    @Schema(description = "选产品请求参数：最低评分数；1")
    private Integer minRatings;

    /** 选产品请求参数：最高评分数；90 */
    @Schema(description = "选产品请求参数：最高评分数；90")
    private Integer maxRatings;

    /** 选产品请求参数：最低月新增评分数；1 */
    @Schema(description = "选产品请求参数：最低月新增评分数；1")
    private Integer minRatingsCv;

    /** 选产品请求参数：最高月新增评分数；5 */
    @Schema(description = "选产品请求参数：最高月新增评分数；5")
    private Integer maxRatingsCv;

    /** 选产品请求参数：最小卖家数量；3 */
    @Schema(description = "选产品请求参数：最小卖家数量；3")
    private Integer minSellers;

    /** 选产品请求参数：最大卖家数量；10 */
    @Schema(description = "选产品请求参数：最大卖家数量；10")
    private Integer maxSellers;

    /** 选产品请求参数：最小毛利率；10 */
    @Schema(description = "选产品请求参数：最小毛利率；10")
    private BigDecimal minProfit;

    /** 选产品请求参数：最大毛利率；20 */
    @Schema(description = "选产品请求参数：最大毛利率；20")
    private BigDecimal maxProfit;

    /** 选产品请求参数：大类 BSR 最高排名；1 */
    @Schema(description = "选产品请求参数：大类 BSR 最高排名；1")
    private Integer minBsr;

    /** 选产品请求参数：大类 BSR 最低排名；100 */
    @Schema(description = "选产品请求参数：大类 BSR 最低排名；100")
    private Integer maxBsr;

    /** 选产品请求参数：BSR 最低增长数；3 */
    @Schema(description = "选产品请求参数：BSR 最低增长数；3")
    private Integer minBsrCv;

    /** 选产品请求参数：BSR 最高增长数；5 */
    @Schema(description = "选产品请求参数：BSR 最高增长数；5")
    private Integer maxBsrCv;

    /** 选产品请求参数：BSR 最低增长率；30 */
    @Schema(description = "选产品请求参数：BSR 最低增长率；30")
    private BigDecimal minBsrCr;

    /** 选产品请求参数：BSR 最高增长率；60 */
    @Schema(description = "选产品请求参数：BSR 最高增长率；60")
    private BigDecimal maxBsrCr;

    /** 选产品请求参数：最低月销量；20 */
    @Schema(description = "选产品请求参数：最低月销量；20")
    private Integer minUnits;

    /** 选产品请求参数：最高月销量；50 */
    @Schema(description = "选产品请求参数：最高月销量；50")
    private Integer maxUnits;

    /** 选产品请求参数：最低月子体销量；20 */
    @Schema(description = "选产品请求参数：最低月子体销量；20")
    private Integer minAmzUnit;

    /** 选产品请求参数：最高月子体销量；50 */
    @Schema(description = "选产品请求参数：最高月子体销量；50")
    private Integer maxAmzUnit;

    /** 选产品请求参数：最低月销售额；60 */
    @Schema(description = "选产品请求参数：最低月销售额；60")
    private BigDecimal minRevenue;

    /** 选产品请求参数：最高月销售额；200 */
    @Schema(description = "选产品请求参数：最高月销售额；200")
    private BigDecimal maxRevenue;

    /** 选产品请求参数：月销售额最低增长率；20 */
    @Schema(description = "选产品请求参数：月销售额最低增长率；20")
    private BigDecimal minRevenueCr;

    /** 选产品请求参数：月销售额最高增长率；30 */
    @Schema(description = "选产品请求参数：月销售额最高增长率；30")
    private BigDecimal maxRevenueCr;

    /** 选产品请求参数：月销量最低增长率；20 */
    @Schema(description = "选产品请求参数：月销量最低增长率；20")
    private BigDecimal minUnitsCr;

    /** 选产品请求参数：月销量最高增长率；30 */
    @Schema(description = "选产品请求参数：月销量最高增长率；30")
    private BigDecimal maxUnitsCr;

    /** 选产品请求参数：重量单位，默认：g；见表2.7 */
    @Schema(description = "选产品请求参数：重量单位，默认：g；见表2.7")
    private String weightUnit;

    /** 选产品请求参数：最小重量；20 */
    @Schema(description = "选产品请求参数：最小重量；20")
    private BigDecimal minWeights;

    /** 选产品请求参数：最大重量；30 */
    @Schema(description = "选产品请求参数：最大重量；30")
    private BigDecimal maxWeights;

    /** 选产品请求参数：最低变体数；1 */
    @Schema(description = "选产品请求参数：最低变体数；1")
    private Integer minVariations;

    /** 选产品请求参数：最高变体数；3 */
    @Schema(description = "选产品请求参数：最高变体数；3")
    private Integer maxVariations;

    /** 选产品请求参数：是否筛选子类目，Y：是；只有在指定类目时才会生效 */
    @Schema(description = "选产品请求参数：是否筛选子类目，Y：是；只有在指定类目时才会生效")
    private String filterSub;

    /** 选产品请求参数：最小子类排名；只有参数 filterSub=Y 时才生效 */
    @Schema(description = "选产品请求参数：最小子类排名；只有参数 filterSub=Y 时才生效")
    private Integer minSubBsrRank;

    /** 选产品请求参数：最大子类排名；只有参数 filterSub=Y 时才生效 */
    @Schema(description = "选产品请求参数：最大子类排名；只有参数 filterSub=Y 时才生效")
    private Integer maxSubBsrRank;

    /** 选产品请求参数：包含品牌；Apple */
    @Schema(description = "选产品请求参数：包含品牌；Apple")
    private String includeBrands;

    /** 选产品请求参数：排除品牌；Apple */
    @Schema(description = "选产品请求参数：排除品牌；Apple")
    private String excludeBrands;

    /** 选产品请求参数：类目节点字符串列表；见查产品类目接口 */
    @Schema(description = "选产品请求参数：类目节点字符串列表；见查产品类目接口")
    private List<String> nodeIdPaths;

    /** 选产品请求参数：true为类目精确查询 false为查询当前及子类目；默认false */
    @Schema(description = "选产品请求参数：true为类目精确查询 false为查询当前及子类目；默认false")
    private Boolean nodeIdPathEqual;

    /** 选产品请求参数：上架月份；见表 1.3，默认不限制 */
    @Schema(description = "选产品请求参数：上架月份；见表 1.3，默认不限制")
    private Integer availableMonth;

    /** 选产品请求参数：尺寸类型集合,逗号分隔，默认不限制；见表 1.4 */
    @Schema(description = "选产品请求参数：尺寸类型集合,逗号分隔，默认不限制；见表 1.4")
    private String dimensionType;

    /** 选产品请求参数：FBA 最低运费；10 */
    @Schema(description = "选产品请求参数：FBA 最低运费；10")
    private BigDecimal minFba;

    /** 选产品请求参数：FBA 最高运费；20 */
    @Schema(description = "选产品请求参数：FBA 最高运费；20")
    private BigDecimal maxFba;

    /** 选产品请求参数：最低 Listing 页面质量分；0 */
    @Schema(description = "选产品请求参数：最低 Listing 页面质量分；0")
    private BigDecimal minLqs;

    /** 选产品请求参数：最高 Listing 页面质量分；10 */
    @Schema(description = "选产品请求参数：最高 Listing 页面质量分；10")
    private BigDecimal maxLqs;

    /** 选产品请求参数：卖家所属地，默认不限制，多条件查询用逗号隔开；见表 1.5 */
    @Schema(description = "选产品请求参数：卖家所属地，默认不限制，多条件查询用逗号隔开；见表 1.5")
    private String sellerNation;

    /** 选产品请求参数：是否有热销标识 Best Seller；Y:是 */
    @Schema(description = "选产品请求参数：是否有热销标识 Best Seller；Y:是")
    private String badgeBS;

    /** 选产品请求参数：是否有热销标识 Amazon's Choice；Y:是 */
    @Schema(description = "选产品请求参数：是否有热销标识 Amazon's Choice；Y:是")
    private String badgeAC;

    /** 选产品请求参数：是否有新品标识 New Release；Y:是 */
    @Schema(description = "选产品请求参数：是否有新品标识 New Release；Y:是")
    private String badgeNR;

    /** 选产品请求参数：配送方式，多条件查询用逗号隔开；AMZ or FBA or FBM */
    @Schema(description = "选产品请求参数：配送方式，多条件查询用逗号隔开；AMZ or FBA or FBM")
    private String fulfillment;

    /** 选产品请求参数：是否查询变体 asin；N: 含变体, Y: 不含变体 */
    @Schema(description = "选产品请求参数：是否查询变体 asin；N: 含变体, Y: 不含变体")
    private String variation;

    /** 选产品请求参数：页码，从 1 开始；默认：1，总条数限制2000条，可以细分条件拉取整个类目数据 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "选产品请求参数：页码，从 1 开始；默认：1，总条数限制2000条，可以细分条件拉取整个类目数据")
    private Integer page = 1;

    /** 选产品请求参数：每页条数；默认：50，最大：100 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 100, message = "size 不能大于 100")
    @Schema(description = "选产品请求参数：每页条数；默认：50，最大：100")
    private Integer size = 50;

    /** 选产品请求参数：排序 */
    @Schema(description = "选产品请求参数：排序")
    private SortOrder order;

}
