package cyou.yuanbaomao.sellersprite.mcp;

import cyou.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaKeywordTrendRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaMonthlyResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaWeeklyResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordMinerRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordOrderRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchTrendRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.TrafficKeywordExtendRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaKeywordTrendVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaMonthlyResearchVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.AbaWeeklyResearchVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.GoogleTrendVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordOrderVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchTrendVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.TrafficKeywordExtendVo;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketBrandConcentrationRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketDemandTrendRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketEbcDistributionRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketGoodsConcentrationRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketPriceDistributionRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingDistributionRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingsDistributionRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerConcentrationRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerLocationRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerTypeRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTimeRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTrendRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketBrandConcentrationVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketDemandTrendVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketEbcDistributionVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketGoodsConcentrationVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketPriceDistributionVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingDistributionVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingsDistributionVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerConcentrationVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerLocationVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerTypeVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTimeVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTrendVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import cyou.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import cyou.yuanbaomao.sellersprite.api.tool.model.vo.OcrVo;
import cyou.yuanbaomao.sellersprite.api.tool.service.ToolService;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.RelatedTrafficRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficSourceRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.RelatedTrafficVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceVo;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkRangeVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo;
import cyou.yuanbaomao.sellersprite.api.trademark.service.TrademarkService;
import cyou.yuanbaomao.sellersprite.mcp.model.McpOcrRequest;
import cyou.yuanbaomao.sellersprite.mcp.model.McpTrademarkListRequest;
import cyou.yuanbaomao.sellersprite.mcp.model.McpTrademarkStatsRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * SellerSprite API 的 MCP 工具适配层。
 *
 * <p>每个工具只负责参数接收和 Service 委派；认证、请求编码、错误转换和上游访问统一由 API 模块处理。</p>
 */
@Service
@Validated
@RequiredArgsConstructor
public class SellerSpriteMcpTools {

    private final AccountService accountService;
    private final ProductService productService;
    private final AsinService asinService;
    private final KeywordService keywordService;
    private final TrafficService trafficService;
    private final MarketService marketService;
    private final ReviewService reviewService;
    private final TrademarkService trademarkService;
    private final ToolService toolService;

    @Tool(name = SellerSpriteMcpToolNames.ACCOUNT_VISITS,
            description = "查询当前 SellerSprite 账户各模块剩余调用次数")
    public VisitsVo getAccountVisits() {
        return accountService.getVisits();
    }

    @Tool(name = SellerSpriteMcpToolNames.LOOKUP_COMPETITORS,
            description = "按品牌、卖家、ASIN、类目或关键词查询竞品")
    public CompetitorLookupVo lookupCompetitors(
            @Valid @ToolParam(description = "查竞品筛选条件") CompetitorLookupRequest request) {
        return productService.lookupCompetitors(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.RESEARCH_PRODUCTS,
            description = "按站点、类目、销量、价格、评分等条件查询选产品结果")
    public ProductResearchVo researchProducts(
            @Valid @ToolParam(description = "选产品筛选条件") ProductResearchRequest request) {
        return productService.researchProducts(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.LIST_PRODUCT_NODES,
            description = "查询 SellerSprite 产品类目节点")
    public List<ProductNodeVo> listProductNodes(
            @ToolParam(description = "Amazon 市场编码，例如 US") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "类目节点 ID 路径，可选") String nodeIdPath,
            @ToolParam(description = "类目搜索关键词，可选") String keyword,
            @ToolParam(description = "历史月份，格式 yyyyMM，可选") String month) {
        return productService.listProductNodes(marketplace, nodeIdPath, keyword, month);
    }

    @Tool(name = SellerSpriteMcpToolNames.ASIN_DETAIL,
            description = "查询指定站点 ASIN 商品详情")
    public AsinDetailVo getAsinDetail(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "10 位 ASIN") String asin) {
        return asinService.getAsinDetail(marketplace, asin);
    }

    @Tool(name = SellerSpriteMcpToolNames.ASIN_COUPON_TREND,
            description = "查询指定 ASIN 的优惠趋势")
    public List<AsinCouponTrendVo> getAsinCouponTrend(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "10 位 ASIN") String asin) {
        return asinService.getCouponTrend(marketplace, asin);
    }

    @Tool(name = SellerSpriteMcpToolNames.ASIN_WITH_COUPON_TREND,
            description = "同时查询 ASIN 详情和优惠趋势")
    public AsinWithCouponTrendVo getAsinWithCouponTrend(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "10 位 ASIN") String asin) {
        return asinService.getAsinWithCouponTrend(marketplace, asin);
    }

    @Tool(name = SellerSpriteMcpToolNames.ASIN_SALES_TREND,
            description = "查询 ASIN 历史销量趋势")
    public AsinSalesTrendVo getAsinSalesTrend(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "10 位 ASIN") String asin) {
        return asinService.getSalesTrend(marketplace, asin);
    }

    @Tool(name = SellerSpriteMcpToolNames.ASIN_SALES_PREDICTION,
            description = "预测指定 ASIN 的销量")
    public AsinSalesPredictionVo predictAsinSales(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "10 位 ASIN") String asin) {
        return asinService.predictAsinSales(marketplace, asin);
    }

    @Tool(name = SellerSpriteMcpToolNames.BSR_SALES_PREDICTION,
            description = "根据 BSR 和类目预测销量")
    public BsrSalesPredictionVo predictBsrSales(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "BSR 排名") Integer bsr,
            @ToolParam(description = "类目 ID") String categoryId) {
        return asinService.predictBsrSales(marketplace, bsr, categoryId);
    }

    @Tool(name = SellerSpriteMcpToolNames.ASIN_KEEPA_TREND,
            description = "查询 ASIN Keepa 经营趋势")
    public KeepaTrendVo getKeepaTrend(
            @Valid @ToolParam(description = "Keepa 趋势查询条件") KeepaTrendRequest request) {
        return asinService.getKeepaTrend(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.KEYWORD_RESEARCH,
            description = "按搜索量、竞争度、转化等条件查询关键词选品结果")
    public KeywordResearchVo researchKeywords(
            @Valid @ToolParam(description = "关键词选品筛选条件") KeywordResearchRequest request) {
        return keywordService.researchKeywords(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.KEYWORD_RESEARCH_TRENDS,
            description = "查询关键词选品趋势数据")
    public List<KeywordResearchTrendVo> getKeywordResearchTrends(
            @Valid @ToolParam(description = "关键词趋势查询条件") KeywordResearchTrendRequest request) {
        return keywordService.getKeywordResearchTrends(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.KEYWORD_MINER,
            description = "根据关键词和筛选条件挖掘相关关键词")
    public KeywordMinerVo mineKeywords(
            @Valid @ToolParam(description = "关键词挖掘条件") KeywordMinerRequest request) {
        return keywordService.mineKeywords(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.KEYWORD_TRAFFIC_EXTEND,
            description = "扩展 ASIN 或关键词的流量词")
    public TrafficKeywordExtendVo extendTrafficKeywords(
            @Valid @ToolParam(description = "流量词扩展条件") TrafficKeywordExtendRequest request) {
        return keywordService.extendTrafficKeywords(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.ABA_RESEARCH_WEEKLY,
            description = "查询 ABA 周度关键词选品数据")
    public AbaWeeklyResearchVo researchAbaWeekly(
            @Valid @ToolParam(description = "ABA 周度查询条件") AbaWeeklyResearchRequest request) {
        return keywordService.researchAbaWeekly(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.ABA_RESEARCH_MONTHLY,
            description = "查询 ABA 月度关键词选品数据")
    public AbaMonthlyResearchVo researchAbaMonthly(
            @Valid @ToolParam(description = "ABA 月度查询条件") AbaMonthlyResearchRequest request) {
        return keywordService.researchAbaMonthly(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.ABA_RESEARCH_TRENDS,
            description = "查询 ABA 关键词趋势")
    public List<AbaKeywordTrendVo> getAbaKeywordTrends(
            @Valid @ToolParam(description = "ABA 关键词趋势条件") AbaKeywordTrendRequest request) {
        return keywordService.getAbaKeywordTrends(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.GOOGLE_TRENDS,
            description = "查询 Google Trends 关键词趋势")
    public GoogleTrendVo getGoogleTrends(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "关键词") String keyword,
            @ToolParam(description = "Google Trends 属性，可选") String googleProp,
            @ToolParam(description = "是否按月聚合") Boolean monthly) {
        return keywordService.getGoogleTrends(marketplace, keyword, googleProp, monthly);
    }

    @Tool(name = SellerSpriteMcpToolNames.KEYWORD_ORDER,
            description = "反查多个 ASIN 的出单关键词")
    public KeywordOrderVo reverseOrderKeywords(
            @Valid @ToolParam(description = "出单词反查条件") KeywordOrderRequest request) {
        return keywordService.reverseOrderKeywords(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.TRAFFIC_KEYWORD,
            description = "反查 ASIN 的流量关键词列表")
    public TrafficKeywordVo reverseTrafficKeywords(
            @Valid @ToolParam(description = "流量词反查条件") TrafficKeywordRequest request) {
        return trafficService.reverseKeywords(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.TRAFFIC_LISTING_PAGE,
            description = "查询 ASIN 关联流量列表")
    public RelatedTrafficVo listRelatedTraffic(
            @Valid @ToolParam(description = "关联流量查询条件") RelatedTrafficRequest request) {
        return trafficService.listRelatedTraffic(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.TRAFFIC_KEYWORD_STAT,
            description = "查询 ASIN 流量关键词统计")
    public TrafficKeywordStatVo getTrafficKeywordStats(
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "10 位 ASIN") String asin,
            @ToolParam(description = "月份，可选") String month) {
        return trafficService.getKeywordStats(marketplace, asin, month);
    }

    @Tool(name = SellerSpriteMcpToolNames.TRAFFIC_LISTING_STAT,
            description = "查询 ASIN 关联流量统计")
    public TrafficListingStatVo getTrafficListingStats(
            @ToolParam(description = "10 位主 ASIN") String asin,
            @ToolParam(description = "Amazon 市场编码") SellerSpriteMarketplace marketplace,
            @ToolParam(description = "关联 ASIN 列表") List<String> asinList) {
        return trafficService.getListingStats(asin, marketplace, asinList);
    }

    @Tool(name = SellerSpriteMcpToolNames.TRAFFIC_SOURCE,
            description = "查询关键词流量来源和流向")
    public TrafficSourceVo getTrafficSources(
            @Valid @ToolParam(description = "流量来源查询条件") TrafficSourceRequest request) {
        return trafficService.getTrafficSources(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_RESEARCH,
            description = "按类目、销量、销售额和竞争度查询市场列表")
    public MarketResearchVo researchMarkets(
            @Valid @ToolParam(description = "市场研究筛选条件") MarketResearchRequest request) {
        return marketService.researchMarkets(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_STATISTICS,
            description = "查询市场商品、品牌、卖家和销售统计")
    public MarketStatisticsVo getMarketStatistics(
            @Valid @ToolParam(description = "市场统计条件") MarketStatisticsRequest request) {
        return marketService.getMarketStatistics(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_GOODS,
            description = "查询市场商品集中度")
    public List<MarketGoodsConcentrationVo> getMarketGoodsConcentration(
            @Valid @ToolParam(description = "商品集中度条件") MarketGoodsConcentrationRequest request) {
        return marketService.getGoodsConcentration(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_BRAND,
            description = "查询市场品牌集中度")
    public List<MarketBrandConcentrationVo> getMarketBrandConcentration(
            @Valid @ToolParam(description = "品牌集中度条件") MarketBrandConcentrationRequest request) {
        return marketService.getBrandConcentration(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_SELLER_LOCATION,
            description = "查询市场卖家所属地分布")
    public List<MarketSellerLocationVo> getMarketSellerLocationDistribution(
            @Valid @ToolParam(description = "卖家所属地分布条件") MarketSellerLocationRequest request) {
        return marketService.getSellerLocationDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_SELLER,
            description = "查询市场卖家集中度")
    public List<MarketSellerConcentrationVo> getMarketSellerConcentration(
            @Valid @ToolParam(description = "卖家集中度条件") MarketSellerConcentrationRequest request) {
        return marketService.getSellerConcentration(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_SELLER_TYPE,
            description = "查询市场卖家类型分布")
    public List<MarketSellerTypeVo> getMarketSellerTypeDistribution(
            @Valid @ToolParam(description = "卖家类型分布条件") MarketSellerTypeRequest request) {
        return marketService.getSellerTypeDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_PERFORMANCE,
            description = "查询市场商品需求趋势")
    public MarketDemandTrendVo getMarketDemandTrend(
            @Valid @ToolParam(description = "商品需求趋势条件") MarketDemandTrendRequest request) {
        return marketService.getDemandTrend(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_SHELF_TIME,
            description = "查询市场商品上架时间分布")
    public List<MarketShelfTimeVo> getMarketShelfTimeDistribution(
            @Valid @ToolParam(description = "上架时间分布条件") MarketShelfTimeRequest request) {
        return marketService.getShelfTimeDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_SHELF_TREND,
            description = "查询市场商品上架趋势分布")
    public List<MarketShelfTrendVo> getMarketShelfTrendDistribution(
            @Valid @ToolParam(description = "上架趋势分布条件") MarketShelfTrendRequest request) {
        return marketService.getShelfTrendDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_RATINGS,
            description = "查询市场评分数量分布")
    public List<MarketRatingsDistributionVo> getMarketRatingsDistribution(
            @Valid @ToolParam(description = "评分数量分布条件") MarketRatingsDistributionRequest request) {
        return marketService.getRatingsDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_RATING,
            description = "查询市场评分值分布")
    public List<MarketRatingDistributionVo> getMarketRatingDistribution(
            @Valid @ToolParam(description = "评分值分布条件") MarketRatingDistributionRequest request) {
        return marketService.getRatingDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_PRICE,
            description = "查询市场价格分布")
    public List<MarketPriceDistributionVo> getMarketPriceDistribution(
            @Valid @ToolParam(description = "价格分布条件") MarketPriceDistributionRequest request) {
        return marketService.getPriceDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.MARKET_EBC,
            description = "查询市场 A+ 和视频分布")
    public List<MarketEbcDistributionVo> getMarketEbcDistribution(
            @Valid @ToolParam(description = "A+ 视频分布条件") MarketEbcDistributionRequest request) {
        return marketService.getEbcDistribution(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.REVIEW_LIST,
            description = "按 ASIN、星级和评论类型查询评论")
    public ReviewListVo listReviews(
            @Valid @ToolParam(description = "评论查询条件") ReviewListRequest request) {
        return reviewService.listReviews(request);
    }

    @Tool(name = SellerSpriteMcpToolNames.GLOBAL_BRAND_RANGE,
            description = "查询全球商标库可用数据范围")
    public List<TrademarkRangeVo> getTrademarkRange() {
        return trademarkService.getBrandRange();
    }

    @Tool(name = SellerSpriteMcpToolNames.GLOBAL_BRAND_DETAIL,
            description = "查询全球商标详情")
    public TrademarkDetailVo getTrademarkDetail(
            @ToolParam(description = "商标数据范围，例如 US") String office,
            @ToolParam(description = "商标 ID") String brandId) {
        return trademarkService.getBrandDetail(office, brandId);
    }

    @Tool(name = SellerSpriteMcpToolNames.GLOBAL_BRAND_LIST,
            description = "查询全球商标列表，图片查询使用 Base64")
    public TrademarkListVo listTrademarks(
            @Valid @ToolParam(description = "全球商标列表查询条件") McpTrademarkListRequest request) {
        return trademarkService.listBrands(request.toApiRequest());
    }

    @Tool(name = SellerSpriteMcpToolNames.GLOBAL_BRAND_STATS,
            description = "查询全球商标统计，图片查询使用 Base64")
    public TrademarkStatsVo getTrademarkStats(
            @Valid @ToolParam(description = "全球商标统计条件") McpTrademarkStatsRequest request) {
        return trademarkService.getBrandStats(request.toApiRequest());
    }

    @Tool(name = SellerSpriteMcpToolNames.OCR,
            description = "对远程图片 URL 或 Base64 图片执行 OCR")
    public OcrVo recognizeImageText(
            @Valid @ToolParam(description = "OCR 图片请求，MCP 不支持直接上传 MultipartFile") McpOcrRequest request) {
        return toolService.recognizeImageText(request.toApiRequest());
    }
}
