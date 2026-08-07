// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.client;

import java.util.List;

/**
 * SellerSprite 官方文档生成契约索引，用于覆盖率和注释完整性测试。
 */
public final class GeneratedSellerSpriteContractIndex {

    private static final List<SellerSpriteOperation> OPERATIONS = List.of(
            SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP,
            SellerSpriteOperation.PRODUCT_RESEARCH,
            SellerSpriteOperation.PRODUCT_NODE,
            SellerSpriteOperation.ASIN_DETAIL,
            SellerSpriteOperation.ASIN_COUPON_TREND,
            SellerSpriteOperation.ASIN_WITH_COUPON_TREND,
            SellerSpriteOperation.ASIN_SALES_TREND,
            SellerSpriteOperation.ASIN_SALES_PREDICTION,
            SellerSpriteOperation.BSR_SALES_PREDICTION,
            SellerSpriteOperation.ASIN_KEEPA_TREND,
            SellerSpriteOperation.KEYWORD_RESEARCH,
            SellerSpriteOperation.KEYWORD_RESEARCH_TRENDS,
            SellerSpriteOperation.KEYWORD_MINER,
            SellerSpriteOperation.KEYWORD_TRAFFIC_EXTEND,
            SellerSpriteOperation.ABA_RESEARCH_WEEKLY,
            SellerSpriteOperation.ABA_RESEARCH_MONTHLY,
            SellerSpriteOperation.ABA_RESEARCH_TRENDS,
            SellerSpriteOperation.GOOGLE_TRENDS,
            SellerSpriteOperation.KEYWORD_ORDER,
            SellerSpriteOperation.TRAFFIC_KEYWORD,
            SellerSpriteOperation.TRAFFIC_LISTING_PAGE,
            SellerSpriteOperation.TRAFFIC_KEYWORD_STAT,
            SellerSpriteOperation.TRAFFIC_LISTING_STAT,
            SellerSpriteOperation.TRAFFIC_SOURCE,
            SellerSpriteOperation.MARKET_RESEARCH,
            SellerSpriteOperation.MARKET_STATISTICS,
            SellerSpriteOperation.MARKET_GOODS,
            SellerSpriteOperation.MARKET_BRAND,
            SellerSpriteOperation.MARKET_SELLER_LOCATION,
            SellerSpriteOperation.MARKET_SELLER,
            SellerSpriteOperation.MARKET_SELLER_TYPE,
            SellerSpriteOperation.MARKET_PERFORMANCE,
            SellerSpriteOperation.MARKET_SHELF_TIME,
            SellerSpriteOperation.MARKET_SHELF_TREND,
            SellerSpriteOperation.MARKET_RATINGS,
            SellerSpriteOperation.MARKET_RATING,
            SellerSpriteOperation.MARKET_PRICE,
            SellerSpriteOperation.MARKET_EBC,
            SellerSpriteOperation.REVIEW_LIST,
            SellerSpriteOperation.OCR,
            SellerSpriteOperation.GLOBAL_BRAND_RANGE,
            SellerSpriteOperation.GLOBAL_BRAND_DETAIL,
            SellerSpriteOperation.GLOBAL_BRAND_LIST,
            SellerSpriteOperation.GLOBAL_BRAND_STATS);

    private static final List<DocumentedEndpoint> DOCUMENTED_ENDPOINTS = List.of(
            new DocumentedEndpoint(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP, "POST", "/v1/product/competitor-lookup"),
            new DocumentedEndpoint(SellerSpriteOperation.PRODUCT_RESEARCH, "POST", "/v1/product/research"),
            new DocumentedEndpoint(SellerSpriteOperation.PRODUCT_NODE, "GET", "/v1/product/node"),
            new DocumentedEndpoint(SellerSpriteOperation.ASIN_DETAIL, "GET", "/v1/asin/{marketplace}/{asin}"),
            new DocumentedEndpoint(SellerSpriteOperation.ASIN_COUPON_TREND, "GET", "/v1/asin/{marketplace}/{asin}/coupon-trend"),
            new DocumentedEndpoint(SellerSpriteOperation.ASIN_WITH_COUPON_TREND, "GET", "/v1/asin/{marketplace}/{asin}/with-coupon-trend"),
            new DocumentedEndpoint(SellerSpriteOperation.ASIN_SALES_TREND, "GET", "/v1/asin/{marketplace}/{asin}/sales-trend"),
            new DocumentedEndpoint(SellerSpriteOperation.ASIN_SALES_PREDICTION, "GET", "/v1/sales/prediction/asin"),
            new DocumentedEndpoint(SellerSpriteOperation.BSR_SALES_PREDICTION, "GET", "/v1/sales/prediction/bsr"),
            new DocumentedEndpoint(SellerSpriteOperation.ASIN_KEEPA_TREND, "GET", "/v1/keepa/{marketplace}/{asin}"),
            new DocumentedEndpoint(SellerSpriteOperation.KEYWORD_RESEARCH, "POST", "/v1/keyword-research"),
            new DocumentedEndpoint(SellerSpriteOperation.KEYWORD_RESEARCH_TRENDS, "POST", "/v1/keyword-research/trends"),
            new DocumentedEndpoint(SellerSpriteOperation.KEYWORD_MINER, "POST", "/v1/keyword/miner"),
            new DocumentedEndpoint(SellerSpriteOperation.KEYWORD_TRAFFIC_EXTEND, "POST", "/v1/traffic/extend"),
            new DocumentedEndpoint(SellerSpriteOperation.ABA_RESEARCH_WEEKLY, "POST", "/v1/aba/research/weekly"),
            new DocumentedEndpoint(SellerSpriteOperation.ABA_RESEARCH_MONTHLY, "POST", "/v1/aba/research/monthly"),
            new DocumentedEndpoint(SellerSpriteOperation.ABA_RESEARCH_TRENDS, "POST", "/v1/aba/research/trends"),
            new DocumentedEndpoint(SellerSpriteOperation.GOOGLE_TRENDS, "GET", "/v1/google/trends"),
            new DocumentedEndpoint(SellerSpriteOperation.KEYWORD_ORDER, "POST", "/v1/keyword-order"),
            new DocumentedEndpoint(SellerSpriteOperation.TRAFFIC_KEYWORD, "POST", "/v1/traffic/keyword"),
            new DocumentedEndpoint(SellerSpriteOperation.TRAFFIC_LISTING_PAGE, "POST", "/v1/traffic/listing/page"),
            new DocumentedEndpoint(SellerSpriteOperation.TRAFFIC_KEYWORD_STAT, "GET", "/v1/traffic/keyword/stat/{marketplace}/{asin}"),
            new DocumentedEndpoint(SellerSpriteOperation.TRAFFIC_LISTING_STAT, "GET", "/v1/traffic/listing/stat/{marketplace}/{asin}"),
            new DocumentedEndpoint(SellerSpriteOperation.TRAFFIC_SOURCE, "POST", "/v1/traffic/source"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_RESEARCH, "POST", "/v1/market/research"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_STATISTICS, "POST", "/v1/market/statistics"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_GOODS, "POST", "/v1/market/goods"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_BRAND, "POST", "/v1/market/brand"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_SELLER_LOCATION, "POST", "/v1/market/seller/location"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_SELLER, "POST", "/v1/market/seller"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_SELLER_TYPE, "POST", "/v1/market/seller/type"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_PERFORMANCE, "POST", "/v1/market/performance"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_SHELF_TIME, "POST", "/v1/market/shelf/time"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_SHELF_TREND, "POST", "/v1/market/shelf/trend"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_RATINGS, "POST", "/v1/market/ratings"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_RATING, "POST", "/v1/market/rating"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_PRICE, "POST", "/v1/market/price"),
            new DocumentedEndpoint(SellerSpriteOperation.MARKET_EBC, "POST", "/v1/market/ebc"),
            new DocumentedEndpoint(SellerSpriteOperation.REVIEW_LIST, "POST", "/v1/review"),
            new DocumentedEndpoint(SellerSpriteOperation.OCR, "POST", "/v1/ocr"),
            new DocumentedEndpoint(SellerSpriteOperation.GLOBAL_BRAND_RANGE, "GET", "/v1/global/brand/range"),
            new DocumentedEndpoint(SellerSpriteOperation.GLOBAL_BRAND_DETAIL, "GET", "/v1/global/brand/detail"),
            new DocumentedEndpoint(SellerSpriteOperation.GLOBAL_BRAND_LIST, "POST", "/v1/global/brand/list"),
            new DocumentedEndpoint(SellerSpriteOperation.GLOBAL_BRAND_STATS, "POST", "/v1/global/brand/stats"));

    private static final List<Class<?>> MODEL_TYPES = List.of(
            cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.SellerSpritePageVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.BadgeVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.SubcategoryVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.VariationVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.NumericTrendPointVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.StringTrendPointVo.class,
            cyou.yuanbaomao.sellersprite.api.common.model.vo.SubRankTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest.class,
            cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo.class,
            cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest.class,
            cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo.class,
            cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest.class,
            cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinCouponTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinWithCouponTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo.AsinVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo.CouponTrendsVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo.AsinVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo.SalesTrendPointsVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesPredictionRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo.AsinDetailVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo.DailyItemListVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo.MonthItemListVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.BsrSalesPredictionRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo.ItemListVo.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchItemVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchItemVo.SearchDepartmentsVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchItemVo.RelationAsinListVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchItemVo.AraAsinListVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordMinerRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerItemVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerItemVo.DepartmentsVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.TrafficKeywordExtendRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.TrafficKeywordExtendItemVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.TrafficKeywordExtendItemVo.RelationVariationsItemsVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.TrafficKeywordExtendVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaWeeklyResearchRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaWeeklyResearchItemVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaWeeklyResearchItemVo.Top3AsinDtoListVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaWeeklyResearchVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaMonthlyResearchRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaMonthlyResearchItemVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaMonthlyResearchItemVo.Top3AsinDtoListVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaMonthlyResearchVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaKeywordTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaKeywordTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.GoogleTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.GoogleTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.GoogleTrendVo.ItemsVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordOrderRequest.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordOrderItemVo.class,
            cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordOrderVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo.ItemsVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo.ItemsRankPositionVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo.ItemsAdPositionVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo.StatsVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.dto.RelatedTrafficRequest.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.RelatedTrafficVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordStatRequest.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo.BadgeCountVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficListingStatRequest.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo.ItemsVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficSourceRequest.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceItemVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceItemVo.AsinInfoVo.class,
            cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchItemVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchItemVo.Top10ImagesVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketGoodsConcentrationRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketGoodsConcentrationVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketBrandConcentrationRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketBrandConcentrationVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerLocationRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerLocationVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerConcentrationRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerConcentrationVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerTypeRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerTypeVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketDemandTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketDemandTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketDemandTrendVo.ItemsVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTimeRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTimeVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTrendRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTrendVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingsDistributionRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingsDistributionVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingDistributionRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingDistributionVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketPriceDistributionRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketPriceDistributionVo.class,
            cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketEbcDistributionRequest.class,
            cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketEbcDistributionVo.class,
            cyou.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest.class,
            cyou.yuanbaomao.sellersprite.api.review.model.vo.ReviewListItemVo.class,
            cyou.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo.class,
            cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest.class,
            cyou.yuanbaomao.sellersprite.api.tool.model.vo.OcrVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkRangeVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkDetailRequest.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.LogosVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.AppealsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.EventsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.GoodsServicesClassificationVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.MarkDescriptionDetailsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.MarkDisclaimerDetailsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.NationalGoodsServicesClassificationVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.PrioritiesVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.PublicationsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.QcVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.ReferenceVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.ReferenceApplicationVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.ReferenceRegistrationVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.WordMarkSpecificationVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.WordMarkSpecificationMarkTranslationVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.WordMarkSpecificationMarkVerbalElementVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo.WordMarkSpecificationMarkSignificantVerbalElementVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkListRequest.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListItemVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListItemVo.ApplicantsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListItemVo.ApplicantsFullAddressVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListItemVo.ApplicantsFullNameVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListItemVo.LogosVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkStatsRequest.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.OfficeVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.BrandNameVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.StatusVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.ApplicantVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.NiceClassVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.ApplicationYearVo.class,
            cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo.ExpiryYearVo.class);

    private static final List<OfficialExample> OFFICIAL_EXAMPLES = List.of(
            new OfficialExample(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP, "/sellersprite/examples/product_competitor_lookup.json", cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo.class, false),
            new OfficialExample(SellerSpriteOperation.PRODUCT_RESEARCH, "/sellersprite/examples/product_research.json", cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo.class, false),
            new OfficialExample(SellerSpriteOperation.PRODUCT_NODE, "/sellersprite/examples/product_node.json", cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo.class, true),
            new OfficialExample(SellerSpriteOperation.ASIN_DETAIL, "/sellersprite/examples/asin_detail.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo.class, false),
            new OfficialExample(SellerSpriteOperation.ASIN_COUPON_TREND, "/sellersprite/examples/asin_coupon_trend.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo.class, true),
            new OfficialExample(SellerSpriteOperation.ASIN_WITH_COUPON_TREND, "/sellersprite/examples/asin_with_coupon_trend.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo.class, false),
            new OfficialExample(SellerSpriteOperation.ASIN_SALES_TREND, "/sellersprite/examples/asin_sales_trend.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo.class, false),
            new OfficialExample(SellerSpriteOperation.ASIN_SALES_PREDICTION, "/sellersprite/examples/asin_sales_prediction.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo.class, false),
            new OfficialExample(SellerSpriteOperation.BSR_SALES_PREDICTION, "/sellersprite/examples/bsr_sales_prediction.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo.class, false),
            new OfficialExample(SellerSpriteOperation.ASIN_KEEPA_TREND, "/sellersprite/examples/asin_keepa_trend.json", cyou.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo.class, false),
            new OfficialExample(SellerSpriteOperation.KEYWORD_RESEARCH, "/sellersprite/examples/keyword_research.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo.class, false),
            new OfficialExample(SellerSpriteOperation.KEYWORD_RESEARCH_TRENDS, "/sellersprite/examples/keyword_research_trends.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchTrendVo.class, true),
            new OfficialExample(SellerSpriteOperation.KEYWORD_MINER, "/sellersprite/examples/keyword_miner.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerVo.class, false),
            new OfficialExample(SellerSpriteOperation.KEYWORD_TRAFFIC_EXTEND, "/sellersprite/examples/keyword_traffic_extend.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.TrafficKeywordExtendVo.class, false),
            new OfficialExample(SellerSpriteOperation.ABA_RESEARCH_WEEKLY, "/sellersprite/examples/aba_research_weekly.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaWeeklyResearchVo.class, false),
            new OfficialExample(SellerSpriteOperation.ABA_RESEARCH_MONTHLY, "/sellersprite/examples/aba_research_monthly.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaMonthlyResearchVo.class, false),
            new OfficialExample(SellerSpriteOperation.ABA_RESEARCH_TRENDS, "/sellersprite/examples/aba_research_trends.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaKeywordTrendVo.class, true),
            new OfficialExample(SellerSpriteOperation.GOOGLE_TRENDS, "/sellersprite/examples/google_trends.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.GoogleTrendVo.class, false),
            new OfficialExample(SellerSpriteOperation.KEYWORD_ORDER, "/sellersprite/examples/keyword_order.json", cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordOrderVo.class, false),
            new OfficialExample(SellerSpriteOperation.TRAFFIC_KEYWORD, "/sellersprite/examples/traffic_keyword.json", cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo.class, false),
            new OfficialExample(SellerSpriteOperation.TRAFFIC_LISTING_PAGE, "/sellersprite/examples/traffic_listing_page.json", cyou.yuanbaomao.sellersprite.api.traffic.model.vo.RelatedTrafficVo.class, false),
            new OfficialExample(SellerSpriteOperation.TRAFFIC_KEYWORD_STAT, "/sellersprite/examples/traffic_keyword_stat.json", cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo.class, false),
            new OfficialExample(SellerSpriteOperation.TRAFFIC_LISTING_STAT, "/sellersprite/examples/traffic_listing_stat.json", cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo.class, false),
            new OfficialExample(SellerSpriteOperation.TRAFFIC_SOURCE, "/sellersprite/examples/traffic_source.json", cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceVo.class, false),
            new OfficialExample(SellerSpriteOperation.MARKET_RESEARCH, "/sellersprite/examples/market_research.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo.class, false),
            new OfficialExample(SellerSpriteOperation.MARKET_STATISTICS, "/sellersprite/examples/market_statistics.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo.class, false),
            new OfficialExample(SellerSpriteOperation.MARKET_GOODS, "/sellersprite/examples/market_goods.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketGoodsConcentrationVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_BRAND, "/sellersprite/examples/market_brand.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketBrandConcentrationVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_SELLER_LOCATION, "/sellersprite/examples/market_seller_location.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerLocationVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_SELLER, "/sellersprite/examples/market_seller.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerConcentrationVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_SELLER_TYPE, "/sellersprite/examples/market_seller_type.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerTypeVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_SHELF_TIME, "/sellersprite/examples/market_shelf_time.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTimeVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_SHELF_TREND, "/sellersprite/examples/market_shelf_trend.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTrendVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_RATINGS, "/sellersprite/examples/market_ratings.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingsDistributionVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_RATING, "/sellersprite/examples/market_rating.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingDistributionVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_PRICE, "/sellersprite/examples/market_price.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketPriceDistributionVo.class, true),
            new OfficialExample(SellerSpriteOperation.MARKET_EBC, "/sellersprite/examples/market_ebc.json", cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketEbcDistributionVo.class, true),
            new OfficialExample(SellerSpriteOperation.GLOBAL_BRAND_RANGE, "/sellersprite/examples/global_brand_range.json", cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkRangeVo.class, true));

    private GeneratedSellerSpriteContractIndex() {
    }

    public static List<SellerSpriteOperation> getOperations() {
        return OPERATIONS;
    }

    public static List<Class<?>> getModelTypes() {
        return MODEL_TYPES;
    }

    public static List<DocumentedEndpoint> getDocumentedEndpoints() {
        return DOCUMENTED_ENDPOINTS;
    }

    public static int getDocumentedRequestFieldCount() {
        return 529;
    }

    public static int getDocumentedResponseFieldCount() {
        return 1197;
    }

    public static List<OfficialExample> getOfficialExamples() {
        return OFFICIAL_EXAMPLES;
    }

    /**
     * 官方响应示例与强类型 data 模型的对应关系。
     *
     * @param operation SellerSprite 操作
     * @param resourcePath classpath 下的官方响应示例
     * @param dataType data 字段的元素或对象类型
     * @param collection data 是否为数组
     */
    public record OfficialExample(SellerSpriteOperation operation, String resourcePath,
                                  Class<?> dataType, boolean collection) {
    }

    /**
     * 官方文档中的 HTTP 方法与远端路径。
     *
     * @param operation SellerSprite 操作
     * @param method 官方 HTTP 方法
     * @param path 官方远端路径
     */
    public record DocumentedEndpoint(SellerSpriteOperation operation, String method, String path) {
    }
}
