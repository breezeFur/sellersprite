// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 选市场列表明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场列表明细响应模型")
public class MarketResearchItemVo {

    /** 选市场列表明细响应参数：市场标志；US */
    @Schema(description = "选市场列表明细响应参数：市场标志；US")
    private String marketplace;

    /** 选市场列表明细响应参数：该市场的货币类型；USD */
    @Schema(description = "选市场列表明细响应参数：该市场的货币类型；USD")
    private String currency;

    /** 选市场列表明细响应参数：节点ID；3732981 */
    @Schema(description = "选市场列表明细响应参数：节点ID；3732981")
    private String nodeId;

    /** 选市场列表明细响应参数：节点名称；Mattresses */
    @Schema(description = "选市场列表明细响应参数：节点名称；Mattresses")
    private String nodeLabelName;

    /** 选市场列表明细响应参数：节点ID路径；1055398:1063306:1063308:3732961:3732981 */
    @Schema(description = "选市场列表明细响应参数：节点ID路径；1055398:1063306:1063308:3732961:3732981")
    private String nodeIdPath;

    /** 选市场列表明细响应参数：节点名称路径；Home & Kitchen:Furniture:Bedroom Furniture:Mattresses & Box Springs:Mattresses */
    @Schema(description = "选市场列表明细响应参数：节点名称路径；Home & Kitchen:Furniture:Bedroom Furniture:Mattresses & Box Springs:Mattresses")
    private String nodeLabelPath;

    /** 选市场列表明细响应参数：节点名称翻译；床垫 */
    @Schema(description = "选市场列表明细响应参数：节点名称翻译；床垫")
    private String nodeLabelLocale;

    /** 选市场列表明细响应参数：节点名称路径翻译；家居用品 厨房:家具:家具卧室:床垫:床垫 */
    @Schema(description = "选市场列表明细响应参数：节点名称路径翻译；家居用品 厨房:家具:家具卧室:床垫:床垫")
    private String nodeLabelPathLocale;

    /** 选市场列表明细响应参数：商品总数；1000 */
    @Schema(description = "选市场列表明细响应参数：商品总数；1000")
    private Integer totalProducts;

    /** 选市场列表明细响应参数：排名；1 */
    @Schema(description = "选市场列表明细响应参数：排名；1")
    private Integer ranking;

    /** 选市场列表明细响应参数：样本数量；100 */
    @Schema(description = "选市场列表明细响应参数：样本数量；100")
    private Integer topProducts;

    /** 选市场列表明细响应参数：品牌数量；34 */
    @Schema(description = "选市场列表明细响应参数：品牌数量；34")
    private Integer brands;

    /** 选市场列表明细响应参数：卖家数量；60 */
    @Schema(description = "选市场列表明细响应参数：卖家数量；60")
    private Integer sellers;

    /** 选市场列表明细响应参数：月总销量；539009 */
    @Schema(description = "选市场列表明细响应参数：月总销量；539009")
    private Integer totalUnits;

    /** 选市场列表明细响应参数：月总销售额；179950610.4 */
    @Schema(description = "选市场列表明细响应参数：月总销售额；179950610.4")
    private BigDecimal totalRevenue;

    /** 选市场列表明细响应参数：月均销量；5390 */
    @Schema(description = "选市场列表明细响应参数：月均销量；5390")
    private Integer avgUnits;

    /** 选市场列表明细响应参数：月均销售额；1799506 */
    @Schema(description = "选市场列表明细响应参数：月均销售额；1799506")
    private BigDecimal avgRevenue;

    /** 选市场列表明细响应参数：平均价格；296.11 */
    @Schema(description = "选市场列表明细响应参数：平均价格；296.11")
    private BigDecimal avgPrice;

    /** 选市场列表明细响应参数：平均评分数；14591 */
    @Schema(description = "选市场列表明细响应参数：平均评分数；14591")
    private Integer avgRatings;

    /** 选市场列表明细响应参数：平均评分值；4.5 */
    @Schema(description = "选市场列表明细响应参数：平均评分值；4.5")
    private BigDecimal avgRating;

    /** 选市场列表明细响应参数：平均BSR；198077 */
    @Schema(description = "选市场列表明细响应参数：平均BSR；198077")
    private Integer avgBsr;

    /** 选市场列表明细响应参数：平均体积(cm³)；529430.46 */
    @Schema(description = "选市场列表明细响应参数：平均体积(cm³)；529430.46")
    private BigDecimal baseAvgVolume;

    /** 选市场列表明细响应参数：平均体积(in³)；32307.87 */
    @Schema(description = "选市场列表明细响应参数：平均体积(in³)；32307.87")
    private BigDecimal avgVolume;

    /** 选市场列表明细响应参数：平均重量(g)；35301.19 */
    @Schema(description = "选市场列表明细响应参数：平均重量(g)；35301.19")
    private BigDecimal baseAvgWeight;

    /** 选市场列表明细响应参数：平均重量(pound)；77.8259 */
    @Schema(description = "选市场列表明细响应参数：平均重量(pound)；77.8259")
    private BigDecimal avgWeight;

    /** 选市场列表明细响应参数：平均利润率；68.76 */
    @Schema(description = "选市场列表明细响应参数：平均利润率；68.76")
    private BigDecimal avgProfit;

    /** 选市场列表明细响应参数：平均卖家数；3.3 */
    @Schema(description = "选市场列表明细响应参数：平均卖家数；3.3")
    private BigDecimal avgSellers;

    /** 选市场列表明细响应参数：A+商品占比,百分比；80 */
    @Schema(description = "选市场列表明细响应参数：A+商品占比,百分比；80")
    private BigDecimal ebcProportion;

    /** 选市场列表明细响应参数：Amazon自营占比,百分比；55 */
    @Schema(description = "选市场列表明细响应参数：Amazon自营占比,百分比；55")
    private BigDecimal amazonSelfProportion;

    /** 选市场列表明细响应参数：FBA占比,百分比；22 */
    @Schema(description = "选市场列表明细响应参数：FBA占比,百分比；22")
    private BigDecimal fbaProportion;

    /** 选市场列表明细响应参数：FBM占比,百分比；14 */
    @Schema(description = "选市场列表明细响应参数：FBM占比,百分比；14")
    private BigDecimal fbmProportion;

    /** 选市场列表明细响应参数：最多卖家归属地 code，见表1.3；US */
    @Schema(description = "选市场列表明细响应参数：最多卖家归属地 code，见表1.3；US")
    private String sellerNation;

    /** 选市场列表明细响应参数：最多卖家归属地 label；美国 */
    @Schema(description = "选市场列表明细响应参数：最多卖家归属地 label；美国")
    private String sellerNationLabel;

    /** 选市场列表明细响应参数：最多卖家归属地 占比；59.3 */
    @Schema(description = "选市场列表明细响应参数：最多卖家归属地 占比；59.3")
    private BigDecimal sellerProportion;

    /** 选市场列表明细响应参数：前10商品的图片 */
    @Schema(description = "选市场列表明细响应参数：前10商品的图片")
    private List<Top10ImagesVo> top10Images;

    /** 选市场列表明细响应参数：退货率；3.51 */
    @Schema(description = "选市场列表明细响应参数：退货率；3.51")
    private BigDecimal returnRatio;

    /** 选市场列表明细响应参数：退货率类目平均值；5.54 */
    @Schema(description = "选市场列表明细响应参数：退货率类目平均值；5.54")
    private BigDecimal avgReturnRatio;

    /** 选市场列表明细响应参数：搜索购买比,千分比；0.94926 */
    @Schema(description = "选市场列表明细响应参数：搜索购买比,千分比；0.94926")
    private BigDecimal searchToPurchaseRatio;

    /** 选市场列表明细响应参数：头部Listing前3名产品总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名产品总销量")
    private Integer top3ProductSales;

    /** 选市场列表明细响应参数：头部Listing前3名品牌总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名品牌总销量")
    private Integer top3BrandSales;

    /** 选市场列表明细响应参数：头部Listing前3名卖家总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名卖家总销量")
    private Integer top3SellerSales;

    /** 选市场列表明细响应参数：头部Listing前3名产品总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名产品总销售额")
    private BigDecimal top3ProductRevenue;

    /** 选市场列表明细响应参数：头部Listing前3名品牌总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名品牌总销售额")
    private BigDecimal top3BrandRevenue;

    /** 选市场列表明细响应参数：头部Listing前3名卖家总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名卖家总销售额")
    private BigDecimal top3SellerRevenue;

    /** 选市场列表明细响应参数：头部Listing前3名商品集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名商品集中度")
    private BigDecimal top3ProductCrn;

    /** 选市场列表明细响应参数：头部Listing前3名品牌集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名品牌集中度")
    private BigDecimal top3BrandCrn;

    /** 选市场列表明细响应参数：头部Listing前3名卖家集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前3名卖家集中度")
    private BigDecimal top3SellerCrn;

    /** 选市场列表明细响应参数：头部Listing前5名产品总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名产品总销量")
    private Integer top5ProductSales;

    /** 选市场列表明细响应参数：头部Listing前5名品牌总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名品牌总销量")
    private Integer top5BrandSales;

    /** 选市场列表明细响应参数：头部Listing前5名卖家总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名卖家总销量")
    private Integer top5SellerSales;

    /** 选市场列表明细响应参数：头部Listing前5名产品总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名产品总销售额")
    private BigDecimal top5ProductRevenue;

    /** 选市场列表明细响应参数：头部Listing前5名品牌总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名品牌总销售额")
    private BigDecimal top5BrandRevenue;

    /** 选市场列表明细响应参数：头部Listing前5名卖家总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名卖家总销售额")
    private BigDecimal top5SellerRevenue;

    /** 选市场列表明细响应参数：头部Listing前5名商品集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名商品集中度")
    private BigDecimal top5ProductCrn;

    /** 选市场列表明细响应参数：头部Listing前5名品牌集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名品牌集中度")
    private BigDecimal top5BrandCrn;

    /** 选市场列表明细响应参数：头部Listing前5名卖家集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前5名卖家集中度")
    private BigDecimal top5SellerCrn;

    /** 选市场列表明细响应参数：头部Listing前10名产品总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名产品总销量")
    private Integer top10ProductSales;

    /** 选市场列表明细响应参数：头部Listing前10名品牌总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名品牌总销量")
    private Integer top10BrandSales;

    /** 选市场列表明细响应参数：头部Listing前10名卖家总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名卖家总销量")
    private Integer top10SellerSales;

    /** 选市场列表明细响应参数：头部Listing前10名产品总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名产品总销售额")
    private BigDecimal top10ProductRevenue;

    /** 选市场列表明细响应参数：头部Listing前10名品牌总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名品牌总销售额")
    private BigDecimal top10BrandRevenue;

    /** 选市场列表明细响应参数：头部Listing前10名卖家总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名卖家总销售额")
    private BigDecimal top10SellerRevenue;

    /** 选市场列表明细响应参数：头部Listing前10名商品集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名商品集中度")
    private BigDecimal top10ProductCrn;

    /** 选市场列表明细响应参数：头部Listing前10名品牌集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名品牌集中度")
    private BigDecimal top10BrandCrn;

    /** 选市场列表明细响应参数：头部Listing前10名卖家集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前10名卖家集中度")
    private BigDecimal top10SellerCrn;

    /** 选市场列表明细响应参数：头部Listing前20名产品总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名产品总销量")
    private Integer top20ProductSales;

    /** 选市场列表明细响应参数：头部Listing前20名品牌总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名品牌总销量")
    private Integer top20BrandSales;

    /** 选市场列表明细响应参数：头部Listing前20名卖家总销量 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名卖家总销量")
    private Integer top20SellerSales;

    /** 选市场列表明细响应参数：头部Listing前20名产品总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名产品总销售额")
    private BigDecimal top20ProductRevenue;

    /** 选市场列表明细响应参数：头部Listing前20名品牌总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名品牌总销售额")
    private BigDecimal top20BrandRevenue;

    /** 选市场列表明细响应参数：头部Listing前20名卖家总销售额 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名卖家总销售额")
    private BigDecimal top20SellerRevenue;

    /** 选市场列表明细响应参数：头部Listing前20名商品集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名商品集中度")
    private BigDecimal top20ProductCrn;

    /** 选市场列表明细响应参数：头部Listing前20名品牌集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名品牌集中度")
    private BigDecimal top20BrandCrn;

    /** 选市场列表明细响应参数：头部Listing前20名卖家集中度 */
    @Schema(description = "选市场列表明细响应参数：头部Listing前20名卖家集中度")
    private BigDecimal top20SellerCrn;

    /** 选市场列表明细响应参数：最近1个月新品数量占比 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品数量占比")
    private BigDecimal l1NewRatio;

    /** 选市场列表明细响应参数：最近1个月新品数量 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品数量")
    private Integer l1NewCount;

    /** 选市场列表明细响应参数：最近1个月新品平均价格 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品平均价格")
    private BigDecimal l1NewAvgPrice;

    /** 选市场列表明细响应参数：最近1个月新品平均评论数 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品平均评论数")
    private Integer l1NewAvgReviews;

    /** 选市场列表明细响应参数：最近1个月新品平均星级 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品平均星级")
    private BigDecimal l1NewAvgRating;

    /** 选市场列表明细响应参数：最近1个月新品月均销量 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品月均销量")
    private Integer l1NewAvgSales;

    /** 选市场列表明细响应参数：最近1个月新品月均销售额 */
    @Schema(description = "选市场列表明细响应参数：最近1个月新品月均销售额")
    private BigDecimal l1NewAvgRevenue;

    /** 选市场列表明细响应参数：最近3个月新品数量占比 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品数量占比")
    private BigDecimal l3NewRatio;

    /** 选市场列表明细响应参数：最近3个月新品数量 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品数量")
    private Integer l3NewCount;

    /** 选市场列表明细响应参数：最近3个月新品平均价格 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品平均价格")
    private BigDecimal l3NewAvgPrice;

    /** 选市场列表明细响应参数：最近3个月新品平均评论数 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品平均评论数")
    private Integer l3NewAvgReviews;

    /** 选市场列表明细响应参数：最近3个月新品平均星级 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品平均星级")
    private BigDecimal l3NewAvgRating;

    /** 选市场列表明细响应参数：最近3个月新品月均销量 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品月均销量")
    private Integer l3NewAvgSales;

    /** 选市场列表明细响应参数：最近3个月新品月均销售额 */
    @Schema(description = "选市场列表明细响应参数：最近3个月新品月均销售额")
    private BigDecimal l3NewAvgRevenue;

    /** 选市场列表明细响应参数：最近6个月新品数量占比 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品数量占比")
    private BigDecimal l6NewRatio;

    /** 选市场列表明细响应参数：最近6个月新品数量 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品数量")
    private Integer l6NewCount;

    /** 选市场列表明细响应参数：最近6个月新品平均价格 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品平均价格")
    private BigDecimal l6NewAvgPrice;

    /** 选市场列表明细响应参数：最近6个月新品平均评论数 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品平均评论数")
    private Integer l6NewAvgReviews;

    /** 选市场列表明细响应参数：最近6个月新品平均星级 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品平均星级")
    private BigDecimal l6NewAvgRating;

    /** 选市场列表明细响应参数：最近6个月新品月均销量 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品月均销量")
    private Integer l6NewAvgSales;

    /** 选市场列表明细响应参数：最近6个月新品月均销售额 */
    @Schema(description = "选市场列表明细响应参数：最近6个月新品月均销售额")
    private BigDecimal l6NewAvgRevenue;

    /** 选市场列表明细响应参数：最近12个月新品数量占比 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品数量占比")
    private BigDecimal l12NewRatio;

    /** 选市场列表明细响应参数：最近12个月新品数量 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品数量")
    private Integer l12NewCount;

    /** 选市场列表明细响应参数：最近12个月新品平均价格 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品平均价格")
    private BigDecimal l12NewAvgPrice;

    /** 选市场列表明细响应参数：最近12个月新品平均评论数 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品平均评论数")
    private Integer l12NewAvgReviews;

    /** 选市场列表明细响应参数：最近12个月新品平均星级 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品平均星级")
    private BigDecimal l12NewAvgRating;

    /** 选市场列表明细响应参数：最近12个月新品月均销量 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品月均销量")
    private Integer l12NewAvgSales;

    /** 选市场列表明细响应参数：最近12个月新品月均销售额 */
    @Schema(description = "选市场列表明细响应参数：最近12个月新品月均销售额")
    private BigDecimal l12NewAvgRevenue;

    @Data
    @Schema(description = "选市场列表明细响应参数：前10商品的图片")
    public static class Top10ImagesVo {

        /** 选市场列表明细响应参数：asin；B01IU6RJYA */
        @Schema(description = "选市场列表明细响应参数：asin；B01IU6RJYA")
        private String asin;

        /** 选市场列表明细响应参数：asin图片链接；https://images-na.ssl-images-amazon.com/images/I/51+5VVLcXSL._AC_US200_.jpg */
        @Schema(description = "选市场列表明细响应参数：asin图片链接；https://images-na.ssl-images-amazon.com/images/I/51+5VVLcXSL._AC_US200_.jpg")
        private String image;

    }

}
