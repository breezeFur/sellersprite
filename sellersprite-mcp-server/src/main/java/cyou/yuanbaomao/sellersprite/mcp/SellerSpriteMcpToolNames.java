package cyou.yuanbaomao.sellersprite.mcp;

import java.util.List;

/**
 * SellerSprite MCP 工具稳定名称。
 */
public final class SellerSpriteMcpToolNames {

    public static final String ACCOUNT_VISITS = "sellersprite_get_account_visits";
    public static final String LOOKUP_COMPETITORS = "sellersprite_lookup_competitors";
    public static final String RESEARCH_PRODUCTS = "sellersprite_research_products";
    public static final String LIST_PRODUCT_NODES = "sellersprite_list_product_nodes";
    public static final String ASIN_DETAIL = "sellersprite_get_asin_detail";
    public static final String ASIN_COUPON_TREND = "sellersprite_get_asin_coupon_trend";
    public static final String ASIN_WITH_COUPON_TREND = "sellersprite_get_asin_with_coupon_trend";
    public static final String ASIN_SALES_TREND = "sellersprite_get_asin_sales_trend";
    public static final String ASIN_SALES_PREDICTION = "sellersprite_predict_asin_sales";
    public static final String BSR_SALES_PREDICTION = "sellersprite_predict_bsr_sales";
    public static final String ASIN_KEEPA_TREND = "sellersprite_get_keepa_trend";
    public static final String KEYWORD_RESEARCH = "sellersprite_research_keywords";
    public static final String KEYWORD_RESEARCH_TRENDS = "sellersprite_get_keyword_research_trends";
    public static final String KEYWORD_MINER = "sellersprite_mine_keywords";
    public static final String KEYWORD_TRAFFIC_EXTEND = "sellersprite_extend_traffic_keywords";
    public static final String ABA_RESEARCH_WEEKLY = "sellersprite_research_aba_weekly";
    public static final String ABA_RESEARCH_MONTHLY = "sellersprite_research_aba_monthly";
    public static final String ABA_RESEARCH_TRENDS = "sellersprite_get_aba_keyword_trends";
    public static final String GOOGLE_TRENDS = "sellersprite_get_google_trends";
    public static final String KEYWORD_ORDER = "sellersprite_reverse_order_keywords";
    public static final String TRAFFIC_KEYWORD = "sellersprite_reverse_traffic_keywords";
    public static final String TRAFFIC_LISTING_PAGE = "sellersprite_list_related_traffic";
    public static final String TRAFFIC_KEYWORD_STAT = "sellersprite_get_traffic_keyword_stats";
    public static final String TRAFFIC_LISTING_STAT = "sellersprite_get_traffic_listing_stats";
    public static final String TRAFFIC_SOURCE = "sellersprite_get_traffic_sources";
    public static final String MARKET_RESEARCH = "sellersprite_research_markets";
    public static final String MARKET_STATISTICS = "sellersprite_get_market_statistics";
    public static final String MARKET_GOODS = "sellersprite_get_market_goods_concentration";
    public static final String MARKET_BRAND = "sellersprite_get_market_brand_concentration";
    public static final String MARKET_SELLER_LOCATION = "sellersprite_get_market_seller_location_distribution";
    public static final String MARKET_SELLER = "sellersprite_get_market_seller_concentration";
    public static final String MARKET_SELLER_TYPE = "sellersprite_get_market_seller_type_distribution";
    public static final String MARKET_PERFORMANCE = "sellersprite_get_market_demand_trend";
    public static final String MARKET_SHELF_TIME = "sellersprite_get_market_shelf_time_distribution";
    public static final String MARKET_SHELF_TREND = "sellersprite_get_market_shelf_trend_distribution";
    public static final String MARKET_RATINGS = "sellersprite_get_market_ratings_distribution";
    public static final String MARKET_RATING = "sellersprite_get_market_rating_distribution";
    public static final String MARKET_PRICE = "sellersprite_get_market_price_distribution";
    public static final String MARKET_EBC = "sellersprite_get_market_ebc_distribution";
    public static final String REVIEW_LIST = "sellersprite_list_reviews";
    public static final String GLOBAL_BRAND_RANGE = "sellersprite_get_trademark_range";
    public static final String GLOBAL_BRAND_DETAIL = "sellersprite_get_trademark_detail";
    public static final String GLOBAL_BRAND_LIST = "sellersprite_list_trademarks";
    public static final String GLOBAL_BRAND_STATS = "sellersprite_get_trademark_stats";
    public static final String OCR = "sellersprite_recognize_image_text";

    private SellerSpriteMcpToolNames() {
    }

    public static List<String> all() {
        return List.of(
                ACCOUNT_VISITS, LOOKUP_COMPETITORS, RESEARCH_PRODUCTS, LIST_PRODUCT_NODES,
                ASIN_DETAIL, ASIN_COUPON_TREND, ASIN_WITH_COUPON_TREND, ASIN_SALES_TREND,
                ASIN_SALES_PREDICTION, BSR_SALES_PREDICTION, ASIN_KEEPA_TREND,
                KEYWORD_RESEARCH, KEYWORD_RESEARCH_TRENDS, KEYWORD_MINER, KEYWORD_TRAFFIC_EXTEND,
                ABA_RESEARCH_WEEKLY, ABA_RESEARCH_MONTHLY, ABA_RESEARCH_TRENDS, GOOGLE_TRENDS,
                KEYWORD_ORDER, TRAFFIC_KEYWORD, TRAFFIC_LISTING_PAGE, TRAFFIC_KEYWORD_STAT,
                TRAFFIC_LISTING_STAT, TRAFFIC_SOURCE, MARKET_RESEARCH, MARKET_STATISTICS,
                MARKET_GOODS, MARKET_BRAND, MARKET_SELLER_LOCATION, MARKET_SELLER,
                MARKET_SELLER_TYPE, MARKET_PERFORMANCE, MARKET_SHELF_TIME, MARKET_SHELF_TREND,
                MARKET_RATINGS, MARKET_RATING, MARKET_PRICE, MARKET_EBC, REVIEW_LIST,
                GLOBAL_BRAND_RANGE, GLOBAL_BRAND_DETAIL, GLOBAL_BRAND_LIST, GLOBAL_BRAND_STATS, OCR);
    }
}
