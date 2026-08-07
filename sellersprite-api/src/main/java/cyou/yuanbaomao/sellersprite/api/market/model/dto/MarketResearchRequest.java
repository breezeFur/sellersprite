// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.market.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 选市场列表请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场列表请求模型")
public class MarketResearchRequest {

    /** 选市场列表请求参数：站点编码；见表 1.2 */
    @NotNull
    @Schema(description = "选市场列表请求参数：站点编码；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 选市场列表请求参数：筛选日期,默认最近30天；见表 1.1 */
    @Schema(description = "选市场列表请求参数：筛选日期,默认最近30天；见表 1.1")
    private String month;

    /** 选市场列表请求参数：头部Listing数量；10 */
    @Schema(description = "选市场列表请求参数：头部Listing数量；10")
    private Integer topNum;

    /** 选市场列表请求参数：新品定义；default: 3 */
    @Schema(description = "选市场列表请求参数：新品定义；default: 3")
    private Integer newProduct;

    /** 选市场列表请求参数：类目；172282:281407 */
    @Schema(description = "选市场列表请求参数：类目；172282:281407")
    private String nodeIdPath;

    /** 选市场列表请求参数：类目关键字；Electronics:Accessories & Supplies */
    @Schema(description = "选市场列表请求参数：类目关键字；Electronics:Accessories & Supplies")
    private String departmentKeyword;

    /** 选市场列表请求参数：最低月均销量；100 */
    @Schema(description = "选市场列表请求参数：最低月均销量；100")
    private Integer minAvgUnits;

    /** 选市场列表请求参数：最高均月销量；10000 */
    @Schema(description = "选市场列表请求参数：最高均月销量；10000")
    private Integer maxAvgUnits;

    /** 选市场列表请求参数：最低月均销售额；100 */
    @Schema(description = "选市场列表请求参数：最低月均销售额；100")
    private BigDecimal minAvgRevenue;

    /** 选市场列表请求参数：最高月均销售额；900 */
    @Schema(description = "选市场列表请求参数：最高月均销售额；900")
    private BigDecimal maxAvgRevenue;

    /** 选市场列表请求参数：最低平均评分数；100 */
    @Schema(description = "选市场列表请求参数：最低平均评分数；100")
    private Integer minAvgRatings;

    /** 选市场列表请求参数：最高平均评分数；500 */
    @Schema(description = "选市场列表请求参数：最高平均评分数；500")
    private Integer maxAvgRatings;

    /** 选市场列表请求参数：最低平均评分值；2.5 */
    @Schema(description = "选市场列表请求参数：最低平均评分值；2.5")
    private BigDecimal minAvgRating;

    /** 选市场列表请求参数：最高平均评分值；3 */
    @Schema(description = "选市场列表请求参数：最高平均评分值；3")
    private BigDecimal maxAvgRating;

    /** 选市场列表请求参数：最低平均BSR排名；50 */
    @Schema(description = "选市场列表请求参数：最低平均BSR排名；50")
    private Integer minAvgBsr;

    /** 选市场列表请求参数：最高平均BSR排名；100 */
    @Schema(description = "选市场列表请求参数：最高平均BSR排名；100")
    private Integer maxAvgBsr;

    /** 选市场列表请求参数：最低平均价格；30 */
    @Schema(description = "选市场列表请求参数：最低平均价格；30")
    private BigDecimal minAvgPrice;

    /** 选市场列表请求参数：最高平均价格；50 */
    @Schema(description = "选市场列表请求参数：最高平均价格；50")
    private BigDecimal maxAvgPrice;

    /** 选市场列表请求参数：最低重量；30 */
    @Schema(description = "选市场列表请求参数：最低重量；30")
    private BigDecimal minWeight;

    /** 选市场列表请求参数：最高重量；60 */
    @Schema(description = "选市场列表请求参数：最高重量；60")
    private BigDecimal maxWeight;

    /** 选市场列表请求参数：最低体积；20 */
    @Schema(description = "选市场列表请求参数：最低体积；20")
    private BigDecimal minVolume;

    /** 选市场列表请求参数：最高体积；50 */
    @Schema(description = "选市场列表请求参数：最高体积；50")
    private BigDecimal maxVolume;

    /** 选市场列表请求参数：最低平均毛利率；20 */
    @Schema(description = "选市场列表请求参数：最低平均毛利率；20")
    private BigDecimal minAvgProfit;

    /** 选市场列表请求参数：最高平均毛利率；70 */
    @Schema(description = "选市场列表请求参数：最高平均毛利率；70")
    private BigDecimal maxAvgProfit;

    /** 选市场列表请求参数：最低头部月均销量；200 */
    @Schema(description = "选市场列表请求参数：最低头部月均销量；200")
    private Integer minTopAvgUnits;

    /** 选市场列表请求参数：最高头部均月销量；300 */
    @Schema(description = "选市场列表请求参数：最高头部均月销量；300")
    private Integer maxTopAvgUnits;

    /** 选市场列表请求参数：最低头部月均销售额；2000 */
    @Schema(description = "选市场列表请求参数：最低头部月均销售额；2000")
    private BigDecimal minTopAvgRevenue;

    /** 选市场列表请求参数：最高头部月均销售额；3000 */
    @Schema(description = "选市场列表请求参数：最高头部月均销售额；3000")
    private BigDecimal maxTopAvgRevenue;

    /** 选市场列表请求参数：最低头部平均BSR；68 */
    @Schema(description = "选市场列表请求参数：最低头部平均BSR；68")
    private Integer minTopAvgBsr;

    /** 选市场列表请求参数：最高头部平均BSR；998 */
    @Schema(description = "选市场列表请求参数：最高头部平均BSR；998")
    private Integer maxTopAvgBsr;

    /** 选市场列表请求参数：最低商品数量；40 */
    @Schema(description = "选市场列表请求参数：最低商品数量；40")
    private Integer minGoodsCount;

    /** 选市场列表请求参数：最高商品数量；90 */
    @Schema(description = "选市场列表请求参数：最高商品数量；90")
    private Integer maxGoodsCount;

    /** 选市场列表请求参数：最小品牌数量；10 */
    @Schema(description = "选市场列表请求参数：最小品牌数量；10")
    private Integer minBrands;

    /** 选市场列表请求参数：最大品牌数量；20 */
    @Schema(description = "选市场列表请求参数：最大品牌数量；20")
    private Integer maxBrands;

    /** 选市场列表请求参数：最小卖家数量；6 */
    @Schema(description = "选市场列表请求参数：最小卖家数量；6")
    private Integer minSellers;

    /** 选市场列表请求参数：最大卖家数量；10 */
    @Schema(description = "选市场列表请求参数：最大卖家数量；10")
    private Integer maxSellers;

    /** 选市场列表请求参数：最小平均卖家数量；4.4 */
    @Schema(description = "选市场列表请求参数：最小平均卖家数量；4.4")
    private BigDecimal minAvgSellers;

    /** 选市场列表请求参数：最大平均卖家数量；10.4 */
    @Schema(description = "选市场列表请求参数：最大平均卖家数量；10.4")
    private BigDecimal maxAvgSellers;

    /** 选市场列表请求参数：最小商品集中度；45 */
    @Schema(description = "选市场列表请求参数：最小商品集中度；45")
    private BigDecimal minGoodsCrn;

    /** 选市场列表请求参数：最大商品集中度；55 */
    @Schema(description = "选市场列表请求参数：最大商品集中度；55")
    private BigDecimal maxGoodsCrn;

    /** 选市场列表请求参数：最小品牌集中度；45 */
    @Schema(description = "选市场列表请求参数：最小品牌集中度；45")
    private BigDecimal minBrandCrn;

    /** 选市场列表请求参数：最大品牌集中度；55 */
    @Schema(description = "选市场列表请求参数：最大品牌集中度；55")
    private BigDecimal maxBrandCrn;

    /** 选市场列表请求参数：最小卖家集中度；45 */
    @Schema(description = "选市场列表请求参数：最小卖家集中度；45")
    private BigDecimal maxSellerCrn;

    /** 选市场列表请求参数：最大卖家集中度；55 */
    @Schema(description = "选市场列表请求参数：最大卖家集中度；55")
    private BigDecimal minSellerCrn;

    /** 选市场列表请求参数：最小A+数量占比；34 */
    @Schema(description = "选市场列表请求参数：最小A+数量占比；34")
    private BigDecimal minEbcProportion;

    /** 选市场列表请求参数：最大A+数量占比；54 */
    @Schema(description = "选市场列表请求参数：最大A+数量占比；54")
    private BigDecimal maxEbcProportion;

    /** 选市场列表请求参数：最小FBA占比；34 */
    @Schema(description = "选市场列表请求参数：最小FBA占比；34")
    private BigDecimal minFbaProportion;

    /** 选市场列表请求参数：最大FBA占比；54 */
    @Schema(description = "选市场列表请求参数：最大FBA占比；54")
    private BigDecimal maxFbaProportion;

    /** 选市场列表请求参数：最小FBM占比；34 */
    @Schema(description = "选市场列表请求参数：最小FBM占比；34")
    private BigDecimal minFbmProportion;

    /** 选市场列表请求参数：最大FBM占比；54 */
    @Schema(description = "选市场列表请求参数：最大FBM占比；54")
    private BigDecimal maxFbmProportion;

    /** 选市场列表请求参数：最小Amazon自营占比；34 */
    @Schema(description = "选市场列表请求参数：最小Amazon自营占比；34")
    private BigDecimal minAmazonSelfProportion;

    /** 选市场列表请求参数：最大Amazon自营占比；56 */
    @Schema(description = "选市场列表请求参数：最大Amazon自营占比；56")
    private BigDecimal maxAmazonSelfProportion;

    /** 选市场列表请求参数：卖家所属地，见表1.3；US,GB */
    @Schema(description = "选市场列表请求参数：卖家所属地，见表1.3；US,GB")
    private String sellerLocation;

    /** 选市场列表请求参数：最小新品数量占比；34 */
    @Schema(description = "选市场列表请求参数：最小新品数量占比；34")
    private BigDecimal minNewProportion;

    /** 选市场列表请求参数：最大新品数量占比；56 */
    @Schema(description = "选市场列表请求参数：最大新品数量占比；56")
    private BigDecimal maxNewProportion;

    /** 选市场列表请求参数：最小新品数量；4 */
    @Schema(description = "选市场列表请求参数：最小新品数量；4")
    private Integer minNewCount;

    /** 选市场列表请求参数：最大新品数量；20 */
    @Schema(description = "选市场列表请求参数：最大新品数量；20")
    private Integer maxNewCount;

    /** 选市场列表请求参数：最小新品平均评分数；23 */
    @Schema(description = "选市场列表请求参数：最小新品平均评分数；23")
    private Integer minNewAvgRatings;

    /** 选市场列表请求参数：最大新品平均评分数；554 */
    @Schema(description = "选市场列表请求参数：最大新品平均评分数；554")
    private Integer maxNewAvgRatings;

    /** 选市场列表请求参数：最小新品平均价格；34 */
    @Schema(description = "选市场列表请求参数：最小新品平均价格；34")
    private BigDecimal minNewAvgPrice;

    /** 选市场列表请求参数：最大新品平均价格；45 */
    @Schema(description = "选市场列表请求参数：最大新品平均价格；45")
    private BigDecimal maxNewAvgPrice;

    /** 选市场列表请求参数：最小新品平均星级；4 */
    @Schema(description = "选市场列表请求参数：最小新品平均星级；4")
    private BigDecimal minNewAvgRating;

    /** 选市场列表请求参数：最大新品平均星级；4.5 */
    @Schema(description = "选市场列表请求参数：最大新品平均星级；4.5")
    private BigDecimal maxNewAvgRating;

    /** 选市场列表请求参数：最低新品月均销量；400 */
    @Schema(description = "选市场列表请求参数：最低新品月均销量；400")
    private BigDecimal minNewAvgUnits;

    /** 选市场列表请求参数：最高新品月均销量；800 */
    @Schema(description = "选市场列表请求参数：最高新品月均销量；800")
    private BigDecimal maxNewAvgUnits;

    /** 选市场列表请求参数：最低新品月均销售额；900 */
    @Schema(description = "选市场列表请求参数：最低新品月均销售额；900")
    private BigDecimal minNewAvgRevenue;

    /** 选市场列表请求参数：最高新品月均销售额；2000 */
    @Schema(description = "选市场列表请求参数：最高新品月均销售额；2000")
    private BigDecimal maxNewAvgRevenue;

    /** 选市场列表请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "选市场列表请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 选市场列表请求参数：每页条数；默认：50，最大：200 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 200, message = "size 不能大于 200")
    @Schema(description = "选市场列表请求参数：每页条数；默认：50，最大：200")
    private Integer size = 50;

    /** 选市场列表请求参数：排序 */
    @Schema(description = "选市场列表请求参数：排序")
    private SortOrder order;

}
