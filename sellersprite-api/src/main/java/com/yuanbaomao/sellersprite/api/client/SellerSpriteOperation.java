package com.yuanbaomao.sellersprite.api.client;

import org.springframework.http.HttpMethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SellerSprite 当前公开操作清单。
 *
 * <p>清单基于 2026-07-10 官方文档，共 44 个业务接口和 1 个次数查询。</p>
 */
@Getter
@RequiredArgsConstructor
public enum SellerSpriteOperation {

    ACCOUNT_VISITS(SellerSpriteDomain.ACCOUNT, HttpMethod.GET, "/v1/visits", "查询可用次数"),

    PRODUCT_COMPETITOR_LOOKUP(SellerSpriteDomain.PRODUCT, HttpMethod.POST,
            "/v1/product/competitor-lookup", "查竞品"),
    PRODUCT_RESEARCH(SellerSpriteDomain.PRODUCT, HttpMethod.POST,
            "/v1/product/research", "选产品"),
    PRODUCT_NODE(SellerSpriteDomain.PRODUCT, HttpMethod.GET,
            "/v1/product/node", "查产品类目"),

    ASIN_DETAIL(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/asin/{marketplace}/{asin}", "ASIN 详情"),
    ASIN_COUPON_TREND(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/asin/{marketplace}/{asin}/coupon-trend", "ASIN 优惠趋势"),
    ASIN_WITH_COUPON_TREND(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/asin/{marketplace}/{asin}/with-coupon-trend", "ASIN 详情及优惠趋势"),
    ASIN_SALES_TREND(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/asin/{marketplace}/{asin}/sales-trend", "ASIN 销量趋势"),
    ASIN_SALES_PREDICTION(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/sales/prediction/asin", "ASIN 销量预测"),
    BSR_SALES_PREDICTION(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/sales/prediction/bsr", "BSR 销量预测"),
    ASIN_KEEPA_TREND(SellerSpriteDomain.ASIN, HttpMethod.GET,
            "/v1/keepa/{marketplace}/{asin}", "Keepa 商品趋势详情"),

    KEYWORD_RESEARCH(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/keyword-research", "关键词选品"),
    KEYWORD_RESEARCH_TRENDS(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/keyword-research/trends", "关键词选品趋势"),
    KEYWORD_MINER(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/keyword/miner", "关键词挖掘"),
    KEYWORD_TRAFFIC_EXTEND(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/traffic/extend", "拓展流量词"),
    ABA_RESEARCH_WEEKLY(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/aba/research/weekly", "ABA 数据选品按周"),
    ABA_RESEARCH_MONTHLY(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/aba/research/monthly", "ABA 数据选品按月"),
    ABA_RESEARCH_TRENDS(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/aba/research/trends", "ABA 关键词趋势"),
    GOOGLE_TRENDS(SellerSpriteDomain.KEYWORD, HttpMethod.GET,
            "/v1/google/trends", "谷歌趋势"),
    KEYWORD_ORDER(SellerSpriteDomain.KEYWORD, HttpMethod.POST,
            "/v1/keyword-order", "出单词反查"),

    TRAFFIC_KEYWORD(SellerSpriteDomain.TRAFFIC, HttpMethod.POST,
            "/v1/traffic/keyword", "关键词反查"),
    TRAFFIC_LISTING_PAGE(SellerSpriteDomain.TRAFFIC, HttpMethod.POST,
            "/v1/traffic/listing/page", "关联流量列表"),
    TRAFFIC_KEYWORD_STAT(SellerSpriteDomain.TRAFFIC, HttpMethod.GET,
            "/v1/traffic/keyword/stat/{marketplace}/{asin}", "流量词统计"),
    TRAFFIC_LISTING_STAT(SellerSpriteDomain.TRAFFIC, HttpMethod.GET,
            "/v1/traffic/listing/stat/{marketplace}/{asin}", "关联流量统计"),
    TRAFFIC_SOURCE(SellerSpriteDomain.TRAFFIC, HttpMethod.POST,
            "/v1/traffic/source", "查流量来源"),

    MARKET_RESEARCH(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/research", "选市场列表"),
    MARKET_STATISTICS(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/statistics", "选市场统计"),
    MARKET_GOODS(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/goods", "商品集中度"),
    MARKET_BRAND(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/brand", "品牌集中度"),
    MARKET_SELLER_LOCATION(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/seller/location", "卖家所属地分布"),
    MARKET_SELLER(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/seller", "卖家集中度"),
    MARKET_SELLER_TYPE(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/seller/type", "卖家类型分布"),
    MARKET_PERFORMANCE(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/performance", "商品需求趋势"),
    MARKET_SHELF_TIME(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/shelf/time", "上架时间分布"),
    MARKET_SHELF_TREND(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/shelf/trend", "上架趋势分布"),
    MARKET_RATINGS(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/ratings", "评分数分布"),
    MARKET_RATING(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/rating", "评分值分布"),
    MARKET_PRICE(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/price", "价格分布"),
    MARKET_EBC(SellerSpriteDomain.MARKET, HttpMethod.POST,
            "/v1/market/ebc", "A+ 视频分布"),

    REVIEW_LIST(SellerSpriteDomain.REVIEW, HttpMethod.POST,
            "/v1/review", "查评论"),

    GLOBAL_BRAND_RANGE(SellerSpriteDomain.TRADEMARK, HttpMethod.GET,
            "/v1/global/brand/range", "全球商标数据范围"),
    GLOBAL_BRAND_DETAIL(SellerSpriteDomain.TRADEMARK, HttpMethod.GET,
            "/v1/global/brand/detail", "全球商标详情"),
    GLOBAL_BRAND_LIST(SellerSpriteDomain.TRADEMARK, HttpMethod.POST,
            "/v1/global/brand/list", "全球商标列表"),
    GLOBAL_BRAND_STATS(SellerSpriteDomain.TRADEMARK, HttpMethod.POST,
            "/v1/global/brand/stats", "全球商标统计"),

    OCR(SellerSpriteDomain.TOOL, HttpMethod.POST,
            "/v1/ocr", "图片文字识别");

    /** 接口所属业务域。 */
    private final SellerSpriteDomain domain;

    /** 官方 HTTP 方法。 */
    private final HttpMethod method;

    /** 相对于 SellerSprite 网关的官方请求路径。 */
    private final String path;

    /** 接口中文业务名称。 */
    private final String description;
}
