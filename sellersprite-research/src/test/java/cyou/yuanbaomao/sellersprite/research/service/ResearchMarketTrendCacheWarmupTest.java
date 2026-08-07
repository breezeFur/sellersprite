package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ResearchMarketTrendCacheWarmupTest {

    private final MarketResearchJobDao jobDao = mock(MarketResearchJobDao.class);
    private final ResearchSourceCacheService sourceCacheService = mock(ResearchSourceCacheService.class);
    private final MarketService marketService = mock(MarketService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResearchProperties properties = new ResearchProperties();

    private ResearchMarketTrendCacheWarmup warmup;

    @BeforeEach
    void setUp() {
        properties.setSourceMode(ResearchSourceMode.REMOTE);
        warmup = new ResearchMarketTrendCacheWarmup(
                jobDao,
                new ResearchInputService(objectMapper),
                sourceCacheService,
                marketService,
                objectMapper,
                properties);
    }

    @Test
    void shouldCheckTwentyFourMonthsAndLoadOnlyMissingMonth() throws Exception {
        MarketResearchJob job = activeRemoteJob();
        when(jobDao.listMarketTrendCacheWarmupCandidates()).thenReturn(List.of(job));
        when(sourceCacheService.categoryPolicy(any())).thenReturn(CachePolicy.permanent());
        when(marketService.getMarketStatistics(any())).thenReturn(new MarketStatisticsVo());
        CachedPayload hit = new CachedPayload(
                objectMapper.createObjectNode().put("products", 20),
                1);
        when(sourceCacheService.getOrLoad(
                        eq(SellerSpriteOperation.MARKET_STATISTICS),
                        any(MarketStatisticsRequest.class),
                        any(CachePolicy.class),
                        any()))
                .thenAnswer(invocation -> {
                    MarketStatisticsRequest request = invocation.getArgument(1);
                    if (!"202509".equals(request.getMonth())) {
                        return hit;
                    }
                    @SuppressWarnings("unchecked")
                    Supplier<JsonNode> loader = invocation.getArgument(3, Supplier.class);
                    return new CachedPayload(loader.get(), 1);
                });

        assertThat(warmup.isReadyForDispatch()).isFalse();
        warmup.warmUp();

        assertThat(warmup.isReadyForDispatch()).isTrue();
        verify(marketService, times(1)).getMarketStatistics(any(MarketStatisticsRequest.class));
        ArgumentCaptor<MarketStatisticsRequest> requestCaptor =
                ArgumentCaptor.forClass(MarketStatisticsRequest.class);
        verify(sourceCacheService, times(24)).getOrLoad(
                eq(SellerSpriteOperation.MARKET_STATISTICS),
                requestCaptor.capture(),
                any(CachePolicy.class),
                any());
        assertThat(requestCaptor.getAllValues())
                .extracting(MarketStatisticsRequest::getMonth)
                .containsExactly("202407", "202408", "202409", "202410", "202411", "202412",
                        "202501", "202502", "202503", "202504", "202505", "202506",
                        "202507", "202508", "202509", "202510", "202511", "202512",
                        "202601", "202602", "202603", "202604", "202605", "202606");
        assertThat(requestCaptor.getAllValues())
                .allSatisfy(request -> {
                    assertThat(request.getMarketplace().getCode()).isEqualTo("US");
                    assertThat(request.getNodeIdPath()).isEqualTo("165796011:239225011:322268011");
                    assertThat(request.getTopN()).isEqualTo(100);
                    assertThat(request.getNewProduct()).isEqualTo(6);
                });
    }

    @Test
    void shouldSkipWarmupWhenCacheIsDisabled() {
        properties.getSourceCache().setEnabled(false);

        assertThat(warmup.isReadyForDispatch()).isTrue();
        warmup.warmUp();

        verifyNoInteractions(jobDao, sourceCacheService, marketService);
    }

    @Test
    void shouldReleaseDispatcherWhenRemoteWarmupFails() throws Exception {
        when(jobDao.listMarketTrendCacheWarmupCandidates()).thenReturn(List.of(activeRemoteJob()));
        when(sourceCacheService.categoryPolicy(any())).thenReturn(CachePolicy.permanent());
        when(sourceCacheService.getOrLoad(
                        eq(SellerSpriteOperation.MARKET_STATISTICS),
                        any(MarketStatisticsRequest.class),
                        any(CachePolicy.class),
                        any()))
                .thenThrow(new IllegalStateException("rate limited"));

        warmup.warmUp();

        assertThat(warmup.isReadyForDispatch()).isTrue();
        verify(marketService, never()).getMarketStatistics(any());
    }

    private MarketResearchJob activeRemoteJob() throws Exception {
        CollectionGraphConfig config = new CollectionGraphConfig();
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId("job-warmup-001");
        job.setMarketplace("US");
        job.setNodeIdPath("165796011:239225011:322268011");
        job.setResearchMonth("2026-06");
        job.setCollectionConfig(objectMapper.writeValueAsString(config));
        return job;
    }
}
