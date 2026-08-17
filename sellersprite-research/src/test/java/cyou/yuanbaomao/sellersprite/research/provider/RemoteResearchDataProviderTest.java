package cyou.yuanbaomao.sellersprite.research.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import cyou.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordMinerRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchTrendRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerItemVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchItemVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchTrendVo;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketDemandTrendVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchItemVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo;
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
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSourceCacheService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

class RemoteResearchDataProviderTest {

    private AccountService accountService;
    private AsinService asinService;
    private ProductService productService;
    private KeywordService keywordService;
    private MarketService marketService;
    private ReviewService reviewService;
    private TrafficService trafficService;
    private ResearchSourceCacheService sourceCacheService;
    private RemoteResearchDataProvider provider;
    private ResearchInput input;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        asinService = mock(AsinService.class);
        productService = mock(ProductService.class);
        keywordService = mock(KeywordService.class);
        marketService = mock(MarketService.class);
        reviewService = mock(ReviewService.class);
        trafficService = mock(TrafficService.class);
        sourceCacheService = mock(ResearchSourceCacheService.class);
        configureInMemorySourceCache();
        provider = newProvider();
        CollectionGraphConfig collectionConfig = new CollectionGraphConfig();
        collectionConfig.getCollectProducts().setEnrichmentAsinLimit(1);
        collectionConfig.getCollectKeywordIntelligence().setTrafficAsinLimit(1);
        input = ResearchInput.builder()
                .jobId("job-remote-001")
                .marketplace("us")
                .nodeIdPath("172282:281407")
                .month("2026-07")
                .keyword("facial cleansing device")
                .seedAsins(List.of(" B0TEST0001 ", "B0TEST0001", "B0TEST0002"))
                .collectionConfig(collectionConfig)
                .build();
    }

    @Test
    void shouldCheckRemoteQuotaEndpoint() {
        tools.jackson.databind.node.ObjectNode details = new ObjectMapper().createObjectNode();
        details.put("status", "available");
        when(accountService.getVisits()).thenReturn(new VisitsVo(details));

        List<ResearchDataset> datasets = provider.checkQuota(input);

        verify(accountService).getVisits();
        assertThat(datasets).singleElement().satisfies(dataset -> {
            assertThat(dataset.getDatasetCode()).isEqualTo("quota.visits");
            assertThat(dataset.getOperation()).isEqualTo("ACCOUNT_VISITS");
            assertThat(dataset.getRecordCount()).isEqualTo(1);
            assertThat(dataset.getPayload().at("/details/status").asText()).isEqualTo("available");
        });
    }

    @Test
    void shouldCollectMarketAndLimitedAsinDatasetsWithGlobalSelectionContext() {
        CollectionGraphConfig config = input.getCollectionConfig();
        config.getCollectProducts().getProductResearch().setMinUnits(33);
        config.getCollectKeywordDemandTrend().setTopN(77);
        config.getCollectSegmentOpportunity().getMarketResearch().setMinGoodsCount(12);
        config.getCollectSegmentOpportunity().getDistribution().setTopN(88);
        config.getCollectSegmentOpportunity().getDistribution().setNewProduct(3);
        config.getCollectSegmentOpportunity().getDistribution().setAsins(List.of("B0CONFIG001"));
        ProductSummaryVo first = product("172282:281407");
        ProductResearchVo products = new ProductResearchVo();
        products.setItems(List.of(first));
        when(productService.researchProducts(any(ProductResearchRequest.class))).thenReturn(products);
        stubMarketResponses();
        when(asinService.getAsinDetail(SellerSpriteMarketplace.US, "B0TEST0001"))
                .thenReturn(new AsinDetailVo());
        when(asinService.getSalesTrend(SellerSpriteMarketplace.US, "B0TEST0001"))
                .thenReturn(new AsinSalesTrendVo());

        List<ResearchDataset> productDatasets = provider.collectProducts(input);
        List<ResearchDataset> salesTrendDatasets =
                provider.collectMarketSalesTrend(input);
        List<ResearchDataset> demandDatasets = provider.collectKeywordDemandTrend(input);
        List<ResearchDataset> segmentDatasets = provider.collectSegmentOpportunity(input);
        List<ResearchDataset> datasets = java.util.stream.Stream.of(
                        productDatasets, salesTrendDatasets, demandDatasets, segmentDatasets)
                .flatMap(List::stream)
                .toList();

        ArgumentCaptor<ProductResearchRequest> productRequest = ArgumentCaptor.forClass(ProductResearchRequest.class);
        verify(productService).researchProducts(productRequest.capture());
        assertThat(productRequest.getAllValues())
                .extracting(ProductResearchRequest::getMonth)
                .containsExactly("202607");
        assertThat(productRequest.getAllValues()).allSatisfy(request -> {
            assertThat(request.getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
            assertThat(request.getKeyword()).isEqualTo("facial cleansing device");
            assertThat(request.getNodeIdPaths()).containsExactly("172282:281407");
            assertThat(request.getSize()).isEqualTo(100);
            assertThat(request.getMinUnits()).isEqualTo(33);
        });

        ArgumentCaptor<MarketResearchRequest> marketRequest = ArgumentCaptor.forClass(MarketResearchRequest.class);
        verify(marketService).researchMarkets(marketRequest.capture());
        assertThat(marketRequest.getValue().getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
        assertThat(marketRequest.getValue().getMonth()).isEqualTo("202607");
        assertThat(marketRequest.getValue().getNodeIdPath()).isEqualTo("172282:281407");
        assertThat(marketRequest.getValue().getDepartmentKeyword()).isEqualTo("facial cleansing device");
        assertThat(marketRequest.getValue().getMinGoodsCount()).isEqualTo(12);

        ArgumentCaptor<MarketStatisticsRequest> statisticsRequest =
                ArgumentCaptor.forClass(MarketStatisticsRequest.class);
        verify(marketService, times(12)).getMarketStatistics(statisticsRequest.capture());
        assertThat(statisticsRequest.getAllValues())
                .extracting(MarketStatisticsRequest::getMonth)
                .containsExactly(
                        "202508", "202509", "202510", "202511", "202512", "202601",
                        "202602", "202603", "202604", "202605", "202606", "202607");
        assertThat(statisticsRequest.getAllValues()).allSatisfy(request -> {
            assertThat(request.getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
            assertThat(request.getNodeIdPath()).isEqualTo("172282:281407");
            assertThat(request.getTopN()).isEqualTo(88);
            assertThat(request.getNewProduct()).isEqualTo(3);
        });

        verify(marketService).getDemandTrend(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())
                && request.getTopN() == 77));
        verify(marketService).getShelfTimeDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getShelfTrendDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getPriceDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getGoodsConcentration(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())
                && request.getAsins().equals(List.of("B0CONFIG001"))));
        verify(marketService).getBrandConcentration(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getSellerConcentration(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getSellerLocationDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getSellerTypeDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getRatingsDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getRatingDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));
        verify(marketService).getEbcDistribution(argThat(request -> hasGlobalMarketContext(
                request.getMarketplace(), request.getMonth(), request.getNodeIdPath())));

        verify(asinService).getAsinDetail(SellerSpriteMarketplace.US, "B0TEST0001");
        verify(asinService).getSalesTrend(SellerSpriteMarketplace.US, "B0TEST0001");

        assertThat(datasets)
                .extracting(ResearchDataset::getDatasetCode)
                .contains(
                        "products",
                        "market.sales-trend",
                        "market.statistics.history.2026-02",
                        "market.statistics.history.2026-03",
                        "market.statistics.history.2026-04",
                        "market.statistics.history.2026-05",
                        "market.statistics.history.2026-06",
                        "market.research",
                        "market.statistics",
                        "market.demand-trend",
                        "market.shelf-time",
                        "market.shelf-trend",
                        "market.price",
                        "market.goods-concentration",
                        "market.brand-concentration",
                        "market.seller-concentration",
                        "market.seller-location",
                        "market.seller-type",
                        "market.ratings",
                        "market.rating",
                        "market.ebc",
                        "asins.B0TEST0001.detail",
                        "asins.B0TEST0001.sales-trend");
        assertThat(datasets)
                .filteredOn(dataset -> "market.sales-trend".equals(dataset.getDatasetCode()))
                .singleElement()
                .satisfies(dataset -> {
                    assertThat(dataset.getRecordCount()).isEqualTo(12);
                    assertThat(dataset.getPayload().get(0).get("month").asText()).isEqualTo("2025-08");
                    assertThat(dataset.getPayload().get(11).get("month").asText()).isEqualTo("2026-07");
                });
        assertThat(datasets)
                .filteredOn(dataset -> dataset.getDatasetCode().startsWith("market.statistics.history."))
                .hasSize(11)
                .allSatisfy(dataset -> {
                    assertThat(dataset.getRecordCount()).isEqualTo(1);
                    assertThat(dataset.getOperation()).isEqualTo("MARKET_STATISTICS");
                });
        verify(reviewService, never()).listReviews(any());
        verify(keywordService, never()).researchKeywords(any());
        verify(keywordService, never()).mineKeywords(any());
        verify(trafficService, never()).reverseKeywords(any());
    }

    @Test
    void shouldPageProductsAndMarketsUntilConfiguredTargets() {
        input.setSeedAsins(List.of());
        CollectionGraphConfig config = input.getCollectionConfig();
        config.getCollectProducts().getPagination().setStartPage(2);
        config.getCollectProducts().getPagination().setPageSize(2);
        config.getCollectProducts().getPagination().setTargetCount(5);
        config.getCollectSegmentOpportunity().getPagination().setStartPage(3);
        config.getCollectSegmentOpportunity().getPagination().setPageSize(2);
        config.getCollectSegmentOpportunity().getPagination().setTargetCount(3);
        when(productService.researchProducts(any(ProductResearchRequest.class))).thenAnswer(invocation -> {
            ProductResearchRequest request = invocation.getArgument(0);
            ProductResearchVo response = new ProductResearchVo();
            response.setPage(request.getPage());
            response.setSize(request.getSize());
            response.setHasNextPage(true);
            response.setItems(List.of(product("172282:281407"), product("172282:281407")));
            return response;
        });
        stubMarketResponses();
        when(marketService.researchMarkets(any(MarketResearchRequest.class))).thenAnswer(invocation -> {
            MarketResearchRequest request = invocation.getArgument(0);
            MarketResearchVo response = new MarketResearchVo();
            response.setPage(request.getPage());
            response.setSize(request.getSize());
            response.setHasNextPage(true);
            response.setItems(List.of(new MarketResearchItemVo(), new MarketResearchItemVo()));
            return response;
        });

        List<ResearchDataset> products = provider.collectProducts(input);
        List<ResearchDataset> markets = provider.collectSegmentOpportunity(input);

        ArgumentCaptor<ProductResearchRequest> productCaptor =
                ArgumentCaptor.forClass(ProductResearchRequest.class);
        verify(productService, times(3)).researchProducts(productCaptor.capture());
        assertThat(productCaptor.getAllValues())
                .extracting(ProductResearchRequest::getPage)
                .containsExactly(2, 3, 4);
        assertThat(productCaptor.getAllValues())
                .extracting(ProductResearchRequest::getSize)
                .containsOnly(2);
        assertThat(products)
                .filteredOn(dataset -> "products".equals(dataset.getDatasetCode()))
                .singleElement()
                .satisfies(dataset -> assertThat(dataset.getRecordCount()).isEqualTo(5));

        ArgumentCaptor<MarketResearchRequest> marketCaptor =
                ArgumentCaptor.forClass(MarketResearchRequest.class);
        verify(marketService, times(2)).researchMarkets(marketCaptor.capture());
        assertThat(marketCaptor.getAllValues())
                .extracting(MarketResearchRequest::getPage)
                .containsExactly(3, 4);
        assertThat(marketCaptor.getAllValues())
                .extracting(MarketResearchRequest::getSize)
                .containsOnly(2);
        assertThat(markets)
                .filteredOn(dataset -> "market.research".equals(dataset.getDatasetCode()))
                .singleElement()
                .satisfies(dataset -> assertThat(dataset.getRecordCount()).isEqualTo(3));
    }

    @Test
    void shouldCollectKeywordIntelligenceFromConfiguredDtoCopiesAndSelectedAsins() {
        CollectionGraphConfig.KeywordIntelligenceCollectionConfig config =
                input.getCollectionConfig().getCollectKeywordIntelligence();
        config.getKeywordResearch().setMinSearches(1234);
        config.getKeywordResearch().setPage(2);
        config.getKeywordResearch().setSize(10);
        config.getKeywordMiner().setMinSearch(4321);
        config.getKeywordMiner().setPage(3);
        config.getKeywordMiner().setSize(20);
        config.getTrafficKeyword().setBadges(List.of("AC"));
        config.getTrafficKeyword().setPage(4);
        config.getTrafficKeyword().setSize(30);
        config.getTrafficKeyword().setAsin("B0CONFIG01");
        KeywordResearchVo research = new KeywordResearchVo();
        research.setItems(List.of(new KeywordResearchItemVo()));
        KeywordMinerVo miner = new KeywordMinerVo();
        miner.setItems(List.of(new KeywordMinerItemVo()));
        TrafficKeywordVo traffic = new TrafficKeywordVo();
        traffic.setItems(List.of(new TrafficKeywordVo.ItemsVo()));
        when(keywordService.researchKeywords(any(KeywordResearchRequest.class))).thenReturn(research);
        when(keywordService.mineKeywords(any(KeywordMinerRequest.class))).thenReturn(miner);
        when(trafficService.reverseKeywords(any(TrafficKeywordRequest.class))).thenReturn(traffic);

        List<ResearchDataset> datasets = provider.collectKeywordIntelligence(input);

        ArgumentCaptor<KeywordResearchRequest> researchRequest =
                ArgumentCaptor.forClass(KeywordResearchRequest.class);
        verify(keywordService).researchKeywords(researchRequest.capture());
        assertThat(researchRequest.getValue().getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
        assertThat(researchRequest.getValue().getMonth()).isEqualTo("202607");
        assertThat(researchRequest.getValue().getKeywords()).isEqualTo("facial cleansing device");
        assertThat(researchRequest.getValue().getPage()).isEqualTo(2);
        assertThat(researchRequest.getValue().getSize()).isEqualTo(10);
        assertThat(researchRequest.getValue().getMinSearches()).isEqualTo(1234);

        ArgumentCaptor<KeywordMinerRequest> minerRequest = ArgumentCaptor.forClass(KeywordMinerRequest.class);
        verify(keywordService).mineKeywords(minerRequest.capture());
        assertThat(minerRequest.getValue().getHistoryDate()).isEqualTo("202607");
        assertThat(minerRequest.getValue().getKeyword()).isEqualTo("facial cleansing device");
        assertThat(minerRequest.getValue().getPage()).isEqualTo(3);
        assertThat(minerRequest.getValue().getSize()).isEqualTo(20);
        assertThat(minerRequest.getValue().getMinSearch()).isEqualTo(4321);

        ArgumentCaptor<TrafficKeywordRequest> trafficRequest = ArgumentCaptor.forClass(TrafficKeywordRequest.class);
        verify(trafficService).reverseKeywords(trafficRequest.capture());
        assertThat(trafficRequest.getValue().getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
        assertThat(trafficRequest.getValue().getMonth()).isEqualTo("202607");
        assertThat(trafficRequest.getValue().getAsin()).isEqualTo("B0TEST0001");
        assertThat(trafficRequest.getValue().getKeyword()).isEqualTo("facial cleansing device");
        assertThat(trafficRequest.getValue().getPage()).isEqualTo(4);
        assertThat(trafficRequest.getValue().getSize()).isEqualTo(30);
        assertThat(trafficRequest.getValue().getBadges()).containsExactly("AC");
        assertThat(config.getTrafficKeyword().getAsin()).isEqualTo("B0CONFIG01");
        assertThat(datasets)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly(
                        "keywords",
                        "keywords.miner",
                        "traffic-keywords.B0TEST0001");
        verify(keywordService, never()).getKeywordResearchTrends(any());
        verify(reviewService, never()).listReviews(any());
        verify(productService, never()).researchProducts(any());
        verifyNoInteractions(accountService, asinService, marketService);
    }

    @Test
    void shouldCollectBaseKeywordsAndSkipKeywordSpecificCallsWhenKeywordIsEmpty() {
        input.setKeyword(null);
        input.setSeedAsins(List.of());
        KeywordResearchVo research = new KeywordResearchVo();
        research.setItems(List.of(new KeywordResearchItemVo()));
        when(keywordService.researchKeywords(any(KeywordResearchRequest.class))).thenReturn(research);

        List<ResearchDataset> datasets = provider.collectKeywordIntelligence(input);

        ArgumentCaptor<KeywordResearchRequest> request = ArgumentCaptor.forClass(KeywordResearchRequest.class);
        verify(keywordService).researchKeywords(request.capture());
        assertThat(request.getValue().getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
        assertThat(request.getValue().getMonth()).isEqualTo("202607");
        assertThat(request.getValue().getKeywords()).isNull();
        verify(keywordService, never()).getKeywordResearchTrends(any());
        verify(keywordService, never()).mineKeywords(any());
        assertThat(datasets).extracting(ResearchDataset::getDatasetCode).containsExactly("keywords");
    }

    @Test
    void shouldDelegateReviewCollectionOncePerDistinctAsinWithoutEnrichmentLimit() {
        ReviewListVo response = new ReviewListVo();
        response.setItems(List.of(new ReviewListItemVo()));
        when(reviewService.listReviews(any(ReviewListRequest.class))).thenReturn(response);

        List<ResearchDataset> datasets = provider.collectReviews(input);

        ArgumentCaptor<ReviewListRequest> requestCaptor = ArgumentCaptor.forClass(ReviewListRequest.class);
        verify(reviewService, times(2)).listReviews(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .extracting(ReviewListRequest::getAsin)
                .containsExactly("B0TEST0001", "B0TEST0002");
        assertThat(datasets)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("reviews.B0TEST0001", "reviews.B0TEST0002");
    }

    @Test
    void shouldCollectOneHundredReviewsPerAsinWithConfiguredFilters() {
        input.setSeedAsins(List.of("B0TEST0001"));
        CollectionGraphConfig.ReviewCollectionConfig config =
                input.getCollectionConfig().getCollectReviews();
        config.setStarList(List.of("five_star"));
        config.setTypeList(List.of("verified_purchase"));
        config.getPagination().setStartPage(2);
        config.getPagination().setPageSize(10);
        config.getPagination().setTargetCountPerAsin(100);
        when(reviewService.listReviews(any(ReviewListRequest.class))).thenAnswer(invocation -> {
            ReviewListRequest request = invocation.getArgument(0);
            ReviewListVo response = new ReviewListVo();
            response.setPage(request.getPage());
            response.setSize(request.getSize());
            response.setHasNextPage(true);
            response.setItems(IntStream.range(0, 10)
                    .mapToObj(ignored -> new ReviewListItemVo())
                    .toList());
            return response;
        });

        List<ResearchDataset> datasets = provider.collectReviews(input);

        ArgumentCaptor<ReviewListRequest> captor = ArgumentCaptor.forClass(ReviewListRequest.class);
        verify(reviewService, times(10)).listReviews(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ReviewListRequest::getPage)
                .containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        assertThat(captor.getAllValues()).allSatisfy(request -> {
            assertThat(request.getSize()).isEqualTo(10);
            assertThat(request.getStarList()).containsExactly("five_star");
            assertThat(request.getTypeList()).containsExactly("verified_purchase");
        });
        assertThat(datasets).singleElement().satisfies(dataset -> {
            assertThat(dataset.getRecordCount()).isEqualTo(100);
            assertThat(dataset.getPayload().path("items")).hasSize(100);
        });
    }

    @Test
    void shouldCollectTwelveMonthsFromMarketStatistics() {
        CollectionGraphConfig config = input.getCollectionConfig();
        config.getCollectMarketSalesTrend().setMonthCount(12);
        MarketStatisticsVo statistics = new MarketStatisticsVo();
        statistics.setProducts(1);
        statistics.setAvgUnits(100);
        when(marketService.getMarketStatistics(any(MarketStatisticsRequest.class))).thenReturn(statistics);

        List<ResearchDataset> datasets = provider.collectMarketSalesTrend(input);

        ArgumentCaptor<MarketStatisticsRequest> captor =
                ArgumentCaptor.forClass(MarketStatisticsRequest.class);
        verify(marketService, times(12)).getMarketStatistics(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(MarketStatisticsRequest::getMonth)
                .containsExactly(
                        "202508", "202509", "202510", "202511", "202512", "202601",
                        "202602", "202603", "202604", "202605", "202606", "202607");
        assertThat(datasets)
                .filteredOn(dataset -> "market.sales-trend".equals(dataset.getDatasetCode()))
                .singleElement()
                .satisfies(dataset -> {
                    assertThat(dataset.getRecordCount()).isEqualTo(12);
                    assertThat(dataset.getPayload()).hasSize(12);
                    assertThat(dataset.getPayload().get(11).path("month").asText()).isEqualTo("2026-07");
                });
        assertThat(datasets)
                .filteredOn(dataset -> dataset.getDatasetCode().startsWith("market.statistics.history."))
                .hasSize(11);
    }

    @Test
    void shouldCollectConfiguredNumberOfMarketStatisticsMonths() {
        CollectionGraphConfig config = input.getCollectionConfig();
        config.getCollectMarketSalesTrend().setMonthCount(3);
        MarketStatisticsVo response = new MarketStatisticsVo();
        response.setProducts(1);
        response.setAvgUnits(50);
        when(marketService.getMarketStatistics(any(MarketStatisticsRequest.class))).thenReturn(response);

        List<ResearchDataset> datasets = provider.collectMarketSalesTrend(input);

        ArgumentCaptor<MarketStatisticsRequest> captor =
                ArgumentCaptor.forClass(MarketStatisticsRequest.class);
        verify(marketService, times(3)).getMarketStatistics(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(MarketStatisticsRequest::getMonth)
                .containsExactly("202605", "202606", "202607");
        assertThat(datasets)
                .filteredOn(dataset -> "market.sales-trend".equals(dataset.getDatasetCode()))
                .singleElement()
                .satisfies(dataset -> assertThat(dataset.getPayload().get(2).path("products").asInt())
                        .isEqualTo(1));
    }

    @Test
    void shouldRejectNullRemoteResponse() {
        when(keywordService.researchKeywords(any(KeywordResearchRequest.class))).thenReturn(null);

        assertThatThrownBy(() -> provider.collectKeywordIntelligence(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KEYWORD_RESEARCH");
    }

    @Test
    void shouldPropagateDistributionFailure() {
        ProductResearchVo products = new ProductResearchVo();
        products.setItems(List.of(product("172282:281407")));
        when(productService.researchProducts(any(ProductResearchRequest.class))).thenReturn(products);
        stubMarketResponses();
        when(marketService.getEbcDistribution(any())).thenThrow(new IllegalStateException("未授权"));
        input.setSeedAsins(List.of());

        assertThatThrownBy(() -> provider.collectSegmentOpportunity(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("未授权");
    }

    private RemoteResearchDataProvider newProvider() {
        return new RemoteResearchDataProvider(
                new ObjectMapper(),
                accountService,
                asinService,
                productService,
                keywordService,
                marketService,
                reviewService,
                trafficService,
                sourceCacheService,
                Validation.buildDefaultValidatorFactory().getValidator());
    }

    private void configureInMemorySourceCache() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, CachedPayload> cache = new HashMap<>();
        when(sourceCacheService.categoryPolicy(anyString())).thenReturn(CachePolicy.permanent());
        when(sourceCacheService.asinPolicy()).thenReturn(CachePolicy.permanent());
        when(sourceCacheService.getOrLoad(any(), any(), any(), any())).thenAnswer(invocation -> {
            SellerSpriteOperation operation = invocation.getArgument(0);
            Object request = invocation.getArgument(1);
            String cacheKey = operation.name() + ":" + objectMapper.valueToTree(request);
            CachedPayload cached = cache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            Supplier<JsonNode> loader = invocation.getArgument(3);
            JsonNode payload = loader.get();
            int recordCount = payload.isArray()
                    ? payload.size()
                    : payload.path("items").isArray() ? payload.path("items").size() : 1;
            CachedPayload loaded = new CachedPayload(payload.deepCopy(), recordCount);
            cache.put(cacheKey, loaded);
            return loaded;
        });
    }

    private ProductSummaryVo product(String nodeIdPath) {
        ProductSummaryVo product = new ProductSummaryVo();
        product.setNodeIdPath(nodeIdPath);
        return product;
    }

    private boolean hasGlobalMarketContext(
            SellerSpriteMarketplace marketplace,
            String month,
            String nodeIdPath) {
        return marketplace == SellerSpriteMarketplace.US
                && "202607".equals(month)
                && "172282:281407".equals(nodeIdPath);
    }

    private void stubMarketResponses() {
        MarketResearchItemVo market = new MarketResearchItemVo();
        market.setNodeIdPath("response:path:is:not:used");
        MarketResearchVo markets = new MarketResearchVo();
        markets.setItems(List.of(market));
        when(marketService.researchMarkets(any(MarketResearchRequest.class))).thenReturn(markets);
        when(marketService.getMarketStatistics(any())).thenReturn(new MarketStatisticsVo());
        when(marketService.getDemandTrend(any())).thenReturn(new MarketDemandTrendVo());
        when(marketService.getShelfTimeDistribution(any())).thenReturn(List.of());
        when(marketService.getShelfTrendDistribution(any())).thenReturn(List.of());
        when(marketService.getPriceDistribution(any())).thenReturn(List.of());
        when(marketService.getGoodsConcentration(any())).thenReturn(List.of());
        when(marketService.getBrandConcentration(any())).thenReturn(List.of());
        when(marketService.getSellerConcentration(any())).thenReturn(List.of());
        when(marketService.getSellerLocationDistribution(any())).thenReturn(List.of());
        when(marketService.getSellerTypeDistribution(any())).thenReturn(List.of());
        when(marketService.getRatingsDistribution(any())).thenReturn(List.of());
        when(marketService.getRatingDistribution(any())).thenReturn(List.of());
        when(marketService.getEbcDistribution(any())).thenReturn(List.of());
    }
}
