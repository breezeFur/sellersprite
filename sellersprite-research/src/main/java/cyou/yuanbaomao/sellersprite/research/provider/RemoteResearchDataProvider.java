package cyou.yuanbaomao.sellersprite.research.provider;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.util.StringUtils;

import cyou.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.SellerSpritePageVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordMinerRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchTrendRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
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
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchItemVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import cyou.yuanbaomao.sellersprite.api.review.model.vo.ReviewListItemVo;
import cyou.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.KeywordDemandTrendCollectionConfig;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.KeywordIntelligenceCollectionConfig;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.MarketDistributionConfig;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.MarketPagination;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.MarketSalesTrendCollectionConfig;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.ProductPagination;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.ReviewCollectionConfig;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.ReviewPagination;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSourceCacheService;
import cyou.yuanbaomao.sellersprite.research.support.ResearchMonthUtils;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 通过项目现有强类型 SellerSprite Service 采集远端数据的 Provider。
 */
public class RemoteResearchDataProvider implements ResearchDataProvider {

    private static final String QUOTA_DATASET_CODE = "quota.visits";
    private static final String PRODUCT_DATASET_CODE = "products";
    private static final String KEYWORD_DATASET_CODE = "keywords";
    private static final String MARKET_RESEARCH_DATASET_CODE = "market.research";
    private static final String MARKET_STATISTICS_DATASET_CODE = "market.statistics";
    private static final String MARKET_DEMAND_TREND_DATASET_CODE = "market.demand-trend";
    private static final String MARKET_SALES_TREND_DATASET_CODE = "market.sales-trend";
    private static final String MARKET_SHELF_TIME_DATASET_CODE = "market.shelf-time";
    private static final String MARKET_SHELF_TREND_DATASET_CODE = "market.shelf-trend";
    private static final String MARKET_PRICE_DATASET_CODE = "market.price";
    private static final String MARKET_RATINGS_DATASET_CODE = "market.ratings";
    private static final String MARKET_RATING_DATASET_CODE = "market.rating";
    private static final String MARKET_EBC_DATASET_CODE = "market.ebc";
    private static final String MARKET_GOODS_DATASET_CODE = "market.goods-concentration";
    private static final String MARKET_BRAND_DATASET_CODE = "market.brand-concentration";
    private static final String MARKET_SELLER_DATASET_CODE = "market.seller-concentration";
    private static final String MARKET_SELLER_LOCATION_DATASET_CODE = "market.seller-location";
    private static final String MARKET_SELLER_TYPE_DATASET_CODE = "market.seller-type";
    private static final String ASIN_DATASET_CODE_PREFIX = "asins.";
    private static final String KEYWORD_TREND_DATASET_CODE = "keywords.trend";
    private static final String KEYWORD_MINER_DATASET_CODE = "keywords.miner";
    private static final String TRAFFIC_KEYWORD_DATASET_CODE_PREFIX = "traffic-keywords.";
    private static final String REVIEW_DATASET_CODE_PREFIX = "reviews.";
    private static final DateTimeFormatter SELLER_SPRITE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final AsinService asinService;
    private final ProductService productService;
    private final KeywordService keywordService;
    private final MarketService marketService;
    private final ReviewService reviewService;
    private final TrafficService trafficService;
    private final ResearchSourceCacheService sourceCacheService;
    private final Validator validator;

    public RemoteResearchDataProvider(
            ObjectMapper objectMapper,
            AccountService accountService,
            AsinService asinService,
            ProductService productService,
            KeywordService keywordService,
            MarketService marketService,
            ReviewService reviewService,
            TrafficService trafficService,
            ResearchSourceCacheService sourceCacheService,
            Validator validator) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper 不能为空");
        this.accountService = Objects.requireNonNull(accountService, "accountService 不能为空");
        this.asinService = Objects.requireNonNull(asinService, "asinService 不能为空");
        this.productService = Objects.requireNonNull(productService, "productService 不能为空");
        this.keywordService = Objects.requireNonNull(keywordService, "keywordService 不能为空");
        this.marketService = Objects.requireNonNull(marketService, "marketService 不能为空");
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService 不能为空");
        this.trafficService = Objects.requireNonNull(trafficService, "trafficService 不能为空");
        this.sourceCacheService = Objects.requireNonNull(sourceCacheService, "sourceCacheService 不能为空");
        this.validator = Objects.requireNonNull(validator, "validator 不能为空");
    }

    @Override
    public ResearchSourceMode sourceMode() {
        return ResearchSourceMode.REMOTE;
    }

    @Override
    public List<ResearchDataset> checkQuota(ResearchInput input) {
        resolveMarketplace(input);
        VisitsVo visits = accountService.getVisits();
        if (visits == null || visits.getDetails() == null || visits.getDetails().isNull()) {
            throw new IllegalStateException("SellerSprite 配额接口返回空响应");
        }
        return List.of(toDataset(
                QUOTA_DATASET_CODE,
                SellerSpriteOperation.ACCOUNT_VISITS,
                visits,
                1));
    }

    @Override
    public List<ResearchDataset> collectProducts(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        CollectionGraphConfig config = collectionConfig(input);
        ProductResearchVo products = collectProductPages(
                input,
                marketplace,
                config.getCollectProducts().getProductResearch(),
                config.getCollectProducts().getPagination(),
                resolveSellerSpriteMonth(input));
        List<ResearchDataset> datasets = new ArrayList<>();
        datasets.add(toDataset(
                PRODUCT_DATASET_CODE,
                SellerSpriteOperation.PRODUCT_RESEARCH,
                products,
                itemCount(products == null ? null : products.getItems())));
        limitedSeedAsins(input, config.getCollectProducts().getEnrichmentAsinLimit())
                .forEach(asin -> collectAsinDatasets(datasets, marketplace, asin));
        return List.copyOf(datasets);
    }

    @Override
    public List<ResearchDataset> collectMarketSalesTrend(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        CollectionGraphConfig config = collectionConfig(input);
        MarketSalesTrendCollectionConfig trendConfig = config.getCollectMarketSalesTrend();
        MarketDistributionConfig distribution = config.getCollectSegmentOpportunity().getDistribution();
        return collectSalesTrendDatasets(
                input,
                marketplace,
                distribution,
                trendConfig.getMonthCount());
    }

    @Override
    public List<ResearchDataset> collectKeywordDemandTrend(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        String nodeIdPath = resolveNodeIdPath(input);
        String sellerSpriteMonth = resolveSellerSpriteMonth(input);
        KeywordDemandTrendCollectionConfig config = collectionConfig(input).getCollectKeywordDemandTrend();
        List<ResearchDataset> datasets = new ArrayList<>();

        MarketDemandTrendRequest demandTrendRequest = new MarketDemandTrendRequest();
        configureMarketRequest(
                demandTrendRequest,
                marketplace,
                nodeIdPath,
                sellerSpriteMonth,
                config.getTopN());
        datasets.add(cachedDataset(
                MARKET_DEMAND_TREND_DATASET_CODE,
                SellerSpriteOperation.MARKET_PERFORMANCE,
                demandTrendRequest,
                sourceCacheService.categoryPolicy(sellerSpriteMonth),
                () -> marketService.getDemandTrend(demandTrendRequest)));

        String keyword = normalizedKeyword(input);
        if (StringUtils.hasText(keyword)) {
            KeywordResearchTrendRequest trendRequest = new KeywordResearchTrendRequest();
            trendRequest.setMarketplace(marketplace);
            trendRequest.setKeyword(keyword);
            validateRequest(trendRequest);
            List<?> trends = keywordService.getKeywordResearchTrends(trendRequest);
            datasets.add(toDataset(
                    KEYWORD_TREND_DATASET_CODE,
                    SellerSpriteOperation.KEYWORD_RESEARCH_TRENDS,
                    trends,
                    itemCount(trends)));
        }
        return List.copyOf(datasets);
    }

    @Override
    public List<ResearchDataset> collectSegmentOpportunity(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        String nodeIdPath = resolveNodeIdPath(input);
        String sellerSpriteMonth = resolveSellerSpriteMonth(input);
        CollectionGraphConfig.SegmentOpportunityCollectionConfig config =
                collectionConfig(input).getCollectSegmentOpportunity();
        List<ResearchDataset> datasets = new ArrayList<>();

        MarketResearchVo markets = collectMarketPages(
                input,
                marketplace,
                config.getMarketResearch(),
                config.getPagination(),
                sellerSpriteMonth);
        datasets.add(toDataset(
                MARKET_RESEARCH_DATASET_CODE,
                SellerSpriteOperation.MARKET_RESEARCH,
                markets,
                itemCount(markets == null ? null : markets.getItems())));
        collectSegmentDatasets(
                datasets,
                marketplace,
                nodeIdPath,
                sellerSpriteMonth,
                config.getDistribution());
        return List.copyOf(datasets);
    }

    @Override
    public List<ResearchDataset> collectKeywordIntelligence(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        String sellerSpriteMonth = resolveSellerSpriteMonth(input);
        String keyword = normalizedKeyword(input);
        KeywordIntelligenceCollectionConfig config =
                collectionConfig(input).getCollectKeywordIntelligence();
        KeywordResearchRequest request = copyRequest(
                config.getKeywordResearch(), KeywordResearchRequest.class);
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setKeywords(keyword);
        validateRequest(request);

        KeywordResearchVo response = keywordService.researchKeywords(request);
        List<ResearchDataset> datasets = new ArrayList<>();
        datasets.add(toDataset(
                KEYWORD_DATASET_CODE,
                SellerSpriteOperation.KEYWORD_RESEARCH,
                response,
                itemCount(response == null ? null : response.getItems())));

        if (StringUtils.hasText(keyword)) {
            KeywordMinerRequest minerRequest = copyRequest(
                    config.getKeywordMiner(), KeywordMinerRequest.class);
            minerRequest.setMarketplace(marketplace);
            minerRequest.setHistoryDate(sellerSpriteMonth);
            minerRequest.setKeyword(keyword);
            validateRequest(minerRequest);
            KeywordMinerVo minedKeywords = keywordService.mineKeywords(minerRequest);
            datasets.add(toDataset(
                    KEYWORD_MINER_DATASET_CODE,
                    SellerSpriteOperation.KEYWORD_MINER,
                    minedKeywords,
                    itemCount(minedKeywords == null ? null : minedKeywords.getItems())));
        }

        limitedSeedAsins(input, config.getTrafficAsinLimit()).forEach(asin -> {
            TrafficKeywordRequest trafficRequest = copyRequest(
                    config.getTrafficKeyword(), TrafficKeywordRequest.class);
            trafficRequest.setMarketplace(marketplace);
            trafficRequest.setAsin(asin);
            trafficRequest.setMonth(sellerSpriteMonth);
            trafficRequest.setKeyword(keyword);
            validateRequest(trafficRequest);
            TrafficKeywordVo trafficKeywords = trafficService.reverseKeywords(trafficRequest);
            datasets.add(toDataset(
                    TRAFFIC_KEYWORD_DATASET_CODE_PREFIX + asin,
                    SellerSpriteOperation.TRAFFIC_KEYWORD,
                    trafficKeywords,
                    itemCount(trafficKeywords == null ? null : trafficKeywords.getItems())));
        });
        return List.copyOf(datasets);
    }

    @Override
    public List<ResearchDataset> collectReviews(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        ReviewCollectionConfig config = collectionConfig(input).getCollectReviews();
        return normalizedSeedAsins(input).stream()
                .map(asin -> collectReviews(marketplace, asin, config))
                .toList();
    }

    @Override
    public List<ResearchDataset> collectAsinIntelligence(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        CollectionGraphConfig.AsinIntelligenceCollectionConfig config =
                collectionConfig(input).getCollectAsinIntelligence();
        List<ResearchDataset> datasets = new ArrayList<>();
        for (String asin : normalizedSeedAsins(input)) {
            AsinSalesTrendRequest salesRequest = new AsinSalesTrendRequest();
            salesRequest.setMarketplace(marketplace);
            salesRequest.setAsin(asin);
            validateRequest(salesRequest);
            datasets.add(cachedDataset(
                    ResearchConstants.ASIN_SALES_TREND_DATASET_CODE_PREFIX + asin,
                    SellerSpriteOperation.ASIN_SALES_TREND,
                    salesRequest,
                    sourceCacheService.asinPolicy(),
                    () -> asinService.getSalesTrend(salesRequest.getMarketplace(), salesRequest.getAsin())));

            KeepaTrendRequest keepaRequest = copyRequest(config.getKeepaTrend(), KeepaTrendRequest.class);
            keepaRequest.setMarketplace(marketplace);
            keepaRequest.setAsin(asin);
            validateRequest(keepaRequest);
            datasets.add(cachedDataset(
                    ResearchConstants.ASIN_KEEPA_TREND_DATASET_CODE_PREFIX + asin,
                    SellerSpriteOperation.ASIN_KEEPA_TREND,
                    keepaRequest,
                    sourceCacheService.asinPolicy(),
                    () -> asinService.getKeepaTrend(keepaRequest)));
        }
        return List.copyOf(datasets);
    }

    private ProductResearchVo collectProductPages(
            ResearchInput input,
            SellerSpriteMarketplace marketplace,
            ProductResearchRequest configuredRequest,
            ProductPagination pagination,
            String month) {
        int page = pagination.getStartPage();
        int pageSize = pagination.getPageSize();
        int targetCount = pagination.getTargetCount();
        List<ProductSummaryVo> items = new ArrayList<>(targetCount);
        ProductResearchVo aggregate = null;
        while (items.size() < targetCount) {
            ProductResearchRequest request = copyRequest(configuredRequest, ProductResearchRequest.class);
            configureProductRequest(request, input, marketplace, month, page, pageSize);
            validateRequest(request);
            ProductResearchVo response = requireResponse(
                    productService.researchProducts(request), SellerSpriteOperation.PRODUCT_RESEARCH);
            if (aggregate == null) {
                aggregate = copyRequest(response, ProductResearchVo.class);
            }
            appendUpToTarget(items, response.getItems(), targetCount);
            if (isLastPage(response, page, pageSize)) {
                break;
            }
            page++;
        }
        aggregate.setPage(pagination.getStartPage());
        aggregate.setSize(pageSize);
        aggregate.setItems(List.copyOf(items));
        return aggregate;
    }

    private MarketResearchVo collectMarketPages(
            ResearchInput input,
            SellerSpriteMarketplace marketplace,
            MarketResearchRequest configuredRequest,
            MarketPagination pagination,
            String month) {
        String nodeIdPath = resolveNodeIdPath(input);
        MarketResearchPageCacheKey cacheKey = new MarketResearchPageCacheKey(
                marketplace,
                month,
                nodeIdPath,
                normalizedKeyword(input),
                configuredRequest,
                pagination);
        CachedPayload cached = cachedPayload(
                SellerSpriteOperation.MARKET_RESEARCH,
                cacheKey,
                sourceCacheService.categoryPolicy(month),
                () -> collectMarketPagesRemotely(
                        marketplace,
                        configuredRequest,
                        pagination,
                        month,
                        nodeIdPath,
                        normalizedKeyword(input)));
        return objectMapper.convertValue(cached.payload(), MarketResearchVo.class);
    }

    private MarketResearchVo collectMarketPagesRemotely(
            SellerSpriteMarketplace marketplace,
            MarketResearchRequest configuredRequest,
            MarketPagination pagination,
            String month,
            String nodeIdPath,
            String keyword) {
        int page = pagination.getStartPage();
        int pageSize = pagination.getPageSize();
        int targetCount = pagination.getTargetCount();
        List<MarketResearchItemVo> items = new ArrayList<>(targetCount);
        MarketResearchVo aggregate = null;
        while (items.size() < targetCount) {
            MarketResearchRequest request = copyRequest(configuredRequest, MarketResearchRequest.class);
            request.setMarketplace(marketplace);
            request.setMonth(month);
            request.setNodeIdPath(nodeIdPath);
            request.setDepartmentKeyword(keyword);
            request.setPage(page);
            request.setSize(pageSize);
            validateRequest(request);
            MarketResearchVo response = requireResponse(
                    marketService.researchMarkets(request), SellerSpriteOperation.MARKET_RESEARCH);
            if (aggregate == null) {
                aggregate = copyRequest(response, MarketResearchVo.class);
            }
            appendUpToTarget(items, response.getItems(), targetCount);
            if (isLastPage(response, page, pageSize)) {
                break;
            }
            page++;
        }
        aggregate.setPage(pagination.getStartPage());
        aggregate.setSize(pageSize);
        aggregate.setItems(List.copyOf(items));
        return aggregate;
    }

    private void configureProductRequest(
            ProductResearchRequest request,
            ResearchInput input,
            SellerSpriteMarketplace marketplace,
            String month,
            int page,
            int pageSize) {
        request.setMarketplace(marketplace);
        request.setMonth(month);
        request.setKeyword(normalizedKeyword(input));
        request.setNodeIdPaths(List.of(resolveNodeIdPath(input)));
        request.setPage(page);
        request.setSize(pageSize);
    }

    private <T> void appendUpToTarget(List<T> target, List<T> pageItems, int targetCount) {
        if (pageItems == null || pageItems.isEmpty()) {
            return;
        }
        int remaining = targetCount - target.size();
        target.addAll(pageItems.subList(0, Math.min(remaining, pageItems.size())));
    }

    private boolean isLastPage(SellerSpritePageVo<?> response, int requestedPage, int requestedSize) {
        List<?> items = response.getItems();
        if (items == null || items.isEmpty() || Boolean.FALSE.equals(response.getHasNextPage())) {
            return true;
        }
        if (Boolean.TRUE.equals(response.getHasNextPage())) {
            return false;
        }
        if (response.getPages() != null && requestedPage >= response.getPages()) {
            return true;
        }
        if (response.getTotal() != null
                && (long) requestedPage * requestedSize >= response.getTotal()) {
            return true;
        }
        return items.size() < requestedSize;
    }

    private void collectSegmentDatasets(
            List<ResearchDataset> datasets,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            MarketDistributionConfig config) {
        CachePolicy cachePolicy = sourceCacheService.categoryPolicy(sellerSpriteMonth);
        MarketStatisticsRequest statisticsRequest = new MarketStatisticsRequest();
        configureMarketRequest(statisticsRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_STATISTICS_DATASET_CODE,
                SellerSpriteOperation.MARKET_STATISTICS,
                statisticsRequest,
                cachePolicy,
                () -> marketService.getMarketStatistics(statisticsRequest)));

        MarketShelfTimeRequest shelfTimeRequest = new MarketShelfTimeRequest();
        configureMarketRequest(shelfTimeRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_SHELF_TIME_DATASET_CODE,
                SellerSpriteOperation.MARKET_SHELF_TIME,
                shelfTimeRequest,
                cachePolicy,
                () -> marketService.getShelfTimeDistribution(shelfTimeRequest)));

        MarketShelfTrendRequest shelfTrendRequest = new MarketShelfTrendRequest();
        configureMarketRequest(shelfTrendRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_SHELF_TREND_DATASET_CODE,
                SellerSpriteOperation.MARKET_SHELF_TREND,
                shelfTrendRequest,
                cachePolicy,
                () -> marketService.getShelfTrendDistribution(shelfTrendRequest)));

        MarketPriceDistributionRequest priceRequest = new MarketPriceDistributionRequest();
        configureMarketRequest(priceRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_PRICE_DATASET_CODE,
                SellerSpriteOperation.MARKET_PRICE,
                priceRequest,
                cachePolicy,
                () -> marketService.getPriceDistribution(priceRequest)));

        MarketGoodsConcentrationRequest goodsRequest = new MarketGoodsConcentrationRequest();
        configureMarketRequest(goodsRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        goodsRequest.setAsins(normalizedAsins(config.getAsins()));
        validateRequest(goodsRequest);
        datasets.add(cachedDataset(
                MARKET_GOODS_DATASET_CODE,
                SellerSpriteOperation.MARKET_GOODS,
                goodsRequest,
                cachePolicy,
                () -> marketService.getGoodsConcentration(goodsRequest)));

        MarketBrandConcentrationRequest brandRequest = new MarketBrandConcentrationRequest();
        configureMarketRequest(brandRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_BRAND_DATASET_CODE,
                SellerSpriteOperation.MARKET_BRAND,
                brandRequest,
                cachePolicy,
                () -> marketService.getBrandConcentration(brandRequest)));

        MarketSellerConcentrationRequest sellerRequest = new MarketSellerConcentrationRequest();
        configureMarketRequest(sellerRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_SELLER_DATASET_CODE,
                SellerSpriteOperation.MARKET_SELLER,
                sellerRequest,
                cachePolicy,
                () -> marketService.getSellerConcentration(sellerRequest)));

        MarketSellerLocationRequest sellerLocationRequest = new MarketSellerLocationRequest();
        configureMarketRequest(sellerLocationRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_SELLER_LOCATION_DATASET_CODE,
                SellerSpriteOperation.MARKET_SELLER_LOCATION,
                sellerLocationRequest,
                cachePolicy,
                () -> marketService.getSellerLocationDistribution(sellerLocationRequest)));

        MarketSellerTypeRequest sellerTypeRequest = new MarketSellerTypeRequest();
        configureMarketRequest(sellerTypeRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_SELLER_TYPE_DATASET_CODE,
                SellerSpriteOperation.MARKET_SELLER_TYPE,
                sellerTypeRequest,
                cachePolicy,
                () -> marketService.getSellerTypeDistribution(sellerTypeRequest)));

        MarketRatingsDistributionRequest ratingsRequest = new MarketRatingsDistributionRequest();
        configureMarketRequest(ratingsRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_RATINGS_DATASET_CODE,
                SellerSpriteOperation.MARKET_RATINGS,
                ratingsRequest,
                cachePolicy,
                () -> marketService.getRatingsDistribution(ratingsRequest)));

        MarketRatingDistributionRequest ratingRequest = new MarketRatingDistributionRequest();
        configureMarketRequest(ratingRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_RATING_DATASET_CODE,
                SellerSpriteOperation.MARKET_RATING,
                ratingRequest,
                cachePolicy,
                () -> marketService.getRatingDistribution(ratingRequest)));

        MarketEbcDistributionRequest ebcRequest = new MarketEbcDistributionRequest();
        configureMarketRequest(ebcRequest, marketplace, nodeIdPath, sellerSpriteMonth,
                config.getTopN(), config.getNewProduct());
        datasets.add(cachedDataset(
                MARKET_EBC_DATASET_CODE,
                SellerSpriteOperation.MARKET_EBC,
                ebcRequest,
                cachePolicy,
                () -> marketService.getEbcDistribution(ebcRequest)));
    }

    private List<ResearchDataset> collectSalesTrendDatasets(
            ResearchInput input,
            SellerSpriteMarketplace marketplace,
            MarketDistributionConfig config,
            int monthCount) {
        YearMonth researchMonth = YearMonth.parse(input.getMonth());
        List<MarketSalesTrendPoint> points = new ArrayList<>(monthCount);
        List<ResearchDataset> datasets = new ArrayList<>(monthCount + 1);
        for (int offset = monthCount - 1; offset >= 0; offset--) {
            YearMonth month = researchMonth.minusMonths(offset);
            String sellerSpriteMonth = month.format(SELLER_SPRITE_MONTH_FORMATTER);
            MarketStatisticsRequest request = new MarketStatisticsRequest();
            configureMarketRequest(
                    request,
                    marketplace,
                    resolveNodeIdPath(input),
                    sellerSpriteMonth,
                    config.getTopN(),
                    config.getNewProduct());
            CachedPayload response = cachedPayload(
                    SellerSpriteOperation.MARKET_STATISTICS,
                    request,
                    sourceCacheService.categoryPolicy(sellerSpriteMonth),
                    () -> marketService.getMarketStatistics(request));
            points.add(toSalesTrendPoint(month, response.payload()));
            if (offset > 0) {
                datasets.add(new ResearchDataset(
                        ResearchConstants.HISTORICAL_MARKET_STATISTICS_DATASET_CODE_PREFIX + month,
                        SellerSpriteOperation.MARKET_STATISTICS.name(),
                        response.payload().deepCopy(),
                        response.recordCount()));
            }
        }
        datasets.add(toDataset(
                MARKET_SALES_TREND_DATASET_CODE,
                SellerSpriteOperation.MARKET_STATISTICS,
                points,
                points.size()));
        return List.copyOf(datasets);
    }

    private MarketSalesTrendPoint toSalesTrendPoint(YearMonth month, JsonNode statistics) {
        int products = statistics.path("products").asInt(0);
        long averageUnits = statistics.path("avgUnits").asLong(0L);
        BigDecimal averageRevenue = decimalValue(statistics, "avgRevenue");
        return new MarketSalesTrendPoint(
                month.toString(),
                products,
                Math.multiplyExact(averageUnits, products),
                averageRevenue.multiply(BigDecimal.valueOf(products)),
                averageUnits,
                averageRevenue,
                statistics.path("avgBsr").asDouble(0D),
                decimalValue(statistics, "avgPrice"),
                statistics.path("avgRatings").asLong(0L),
                decimalValue(statistics, "avgRating"),
                statistics.path("brands").asInt(0),
                statistics.path("sellers").asInt(0),
                statistics.path("newProducts").asInt(0),
                decimalValue(statistics, "newProductProportion"),
                decimalValue(statistics, "avgProfit"),
                decimalValue(statistics, "avgSellers"));
    }

    private void collectAsinDatasets(
            List<ResearchDataset> datasets,
            SellerSpriteMarketplace marketplace,
            String asin) {
        AsinDetailRequest detailRequest = new AsinDetailRequest();
        detailRequest.setMarketplace(marketplace);
        detailRequest.setAsin(asin);
        validateRequest(detailRequest);
        datasets.add(cachedDataset(
                ASIN_DATASET_CODE_PREFIX + asin + ".detail",
                SellerSpriteOperation.ASIN_DETAIL,
                detailRequest,
                sourceCacheService.asinPolicy(),
                () -> asinService.getAsinDetail(detailRequest.getMarketplace(), detailRequest.getAsin())));

        AsinSalesTrendRequest salesTrendRequest = new AsinSalesTrendRequest();
        salesTrendRequest.setMarketplace(marketplace);
        salesTrendRequest.setAsin(asin);
        validateRequest(salesTrendRequest);
        datasets.add(cachedDataset(
                ASIN_DATASET_CODE_PREFIX + asin + ".sales-trend",
                SellerSpriteOperation.ASIN_SALES_TREND,
                salesTrendRequest,
                sourceCacheService.asinPolicy(),
                () -> asinService.getSalesTrend(
                        salesTrendRequest.getMarketplace(), salesTrendRequest.getAsin())));
    }

    private void configureMarketRequest(
            MarketStatisticsRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketDemandTrendRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketShelfTimeRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketShelfTrendRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketPriceDistributionRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketGoodsConcentrationRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
    }

    private void configureMarketRequest(
            MarketBrandConcentrationRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketSellerConcentrationRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketSellerLocationRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketSellerTypeRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketRatingsDistributionRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketRatingDistributionRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private void configureMarketRequest(
            MarketEbcDistributionRequest request,
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            String sellerSpriteMonth,
            Integer topN,
            Integer newProduct) {
        request.setMarketplace(marketplace);
        request.setMonth(sellerSpriteMonth);
        request.setTopN(topN);
        request.setNewProduct(newProduct);
        request.setNodeIdPath(nodeIdPath);
        validateRequest(request);
    }

    private String resolveNodeIdPath(ResearchInput input) {
        if (input == null || !StringUtils.hasText(input.getNodeIdPath())) {
            throw new IllegalArgumentException("市场调研输入的 nodeIdPath 不能为空");
        }
        return input.getNodeIdPath().trim();
    }

    private String resolveSellerSpriteMonth(ResearchInput input) {
        if (input == null) {
            throw new IllegalArgumentException("市场调研输入不能为空");
        }
        return ResearchMonthUtils.toSellerSpriteMonth(input.getMonth());
    }

    private String normalizedKeyword(ResearchInput input) {
        return input == null || !StringUtils.hasText(input.getKeyword()) ? null : input.getKeyword().trim();
    }

    private List<String> normalizedSeedAsins(ResearchInput input) {
        return normalizedAsins(input.getSeedAsins());
    }

    private List<String> normalizedAsins(List<String> asins) {
        if (asins == null || asins.isEmpty()) {
            return List.of();
        }
        return asins.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<String> limitedSeedAsins(ResearchInput input, Integer limit) {
        if (limit == null || limit <= 0) {
            return List.of();
        }
        return normalizedSeedAsins(input).stream()
                .limit(limit)
                .toList();
    }

    private ResearchDataset collectReviews(
            SellerSpriteMarketplace marketplace,
            String asin,
            ReviewCollectionConfig config) {
        ReviewPagination pagination = config.getPagination();
        int page = pagination.getStartPage();
        int pageSize = pagination.getPageSize();
        int targetCount = pagination.getTargetCountPerAsin();
        List<ReviewListItemVo> items = new ArrayList<>(targetCount);
        ReviewListVo aggregate = null;
        while (items.size() < targetCount) {
            ReviewListRequest request = new ReviewListRequest();
            request.setMarketplace(marketplace);
            request.setAsin(asin);
            request.setStarList(config.getStarList());
            request.setTypeList(config.getTypeList());
            request.setPage(page);
            request.setSize(pageSize);
            validateRequest(request);
            ReviewListVo response = requireResponse(
                    reviewService.listReviews(request), SellerSpriteOperation.REVIEW_LIST);
            if (aggregate == null) {
                aggregate = copyRequest(response, ReviewListVo.class);
            }
            appendUpToTarget(items, response.getItems(), targetCount);
            if (isLastPage(response, page, pageSize)) {
                break;
            }
            page++;
        }
        aggregate.setPage(pagination.getStartPage());
        aggregate.setSize(pageSize);
        aggregate.setItems(List.copyOf(items));
        return toDataset(
                REVIEW_DATASET_CODE_PREFIX + asin,
                SellerSpriteOperation.REVIEW_LIST,
                aggregate,
                items.size());
    }

    private CollectionGraphConfig collectionConfig(ResearchInput input) {
        if (input == null || input.getCollectionConfig() == null) {
            throw new IllegalArgumentException("市场调研输入的 collectionConfig 不能为空");
        }
        return input.getCollectionConfig();
    }

    private <T> T copyRequest(T source, Class<T> type) {
        return objectMapper.convertValue(Objects.requireNonNull(source, type.getSimpleName() + " 不能为空"), type);
    }

    private <T> T requireResponse(T response, SellerSpriteOperation operation) {
        if (response == null) {
            throw new IllegalStateException("SellerSprite 返回空响应: " + operation.name());
        }
        return response;
    }

    private <T> void validateRequest(T request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private ResearchDataset toDataset(
            String datasetCode,
            SellerSpriteOperation operation,
            Object response,
            int recordCount) {
        if (response == null) {
            throw new IllegalStateException("SellerSprite 返回空响应: " + operation.name());
        }
        return new ResearchDataset(
                datasetCode,
                operation.name(),
                objectMapper.valueToTree(response),
                recordCount);
    }

    private ResearchDataset cachedDataset(
            String datasetCode,
            SellerSpriteOperation operation,
            Object effectiveRequest,
            CachePolicy policy,
            Supplier<?> loader) {
        CachedPayload payload = cachedPayload(operation, effectiveRequest, policy, loader);
        return new ResearchDataset(
                datasetCode,
                operation.name(),
                payload.payload().deepCopy(),
                payload.recordCount());
    }

    private CachedPayload cachedPayload(
            SellerSpriteOperation operation,
            Object effectiveRequest,
            CachePolicy policy,
            Supplier<?> loader) {
        return sourceCacheService.getOrLoad(
                operation,
                effectiveRequest,
                policy,
                () -> objectMapper.valueToTree(requireResponse(loader.get(), operation)));
    }

    private BigDecimal decimalValue(JsonNode source, String field) {
        JsonNode value = source.path(field);
        if (value.isNumber()) {
            return value.decimalValue();
        }
        if (value.isTextual() && StringUtils.hasText(value.asText())) {
            try {
                return new BigDecimal(value.asText());
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private int itemCount(List<?> items) {
        return items == null ? 0 : items.size();
    }

    private record MarketSalesTrendPoint(
            String month,
            int products,
            long sampledUnits,
            BigDecimal sampledRevenue,
            long averageUnits,
            BigDecimal averageRevenue,
            double averageBsr,
            BigDecimal averagePrice,
            long averageRatings,
            BigDecimal averageRating,
            int brands,
            int sellers,
            int newProducts,
            BigDecimal newProductProportion,
            BigDecimal averageProfit,
            BigDecimal averageSellers) {
    }

    private record MarketResearchPageCacheKey(
            SellerSpriteMarketplace marketplace,
            String month,
            String nodeIdPath,
            String keyword,
            MarketResearchRequest request,
            MarketPagination pagination) {
    }

    private SellerSpriteMarketplace resolveMarketplace(ResearchInput input) {
        if (input == null
                || !StringUtils.hasText(input.getJobId())
                || !StringUtils.hasText(input.getMarketplace())) {
            throw new IllegalArgumentException("市场调研输入的 jobId 和 marketplace 不能为空");
        }
        try {
            return SellerSpriteMarketplace.valueOf(input.getMarketplace().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("不支持的 SellerSprite 市场编码: " + input.getMarketplace(), exception);
        }
    }
}
