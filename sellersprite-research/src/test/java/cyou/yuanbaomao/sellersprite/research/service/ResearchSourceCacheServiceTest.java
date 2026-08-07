package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

class ResearchSourceCacheServiceTest {

    private final ResearchSourceCacheStore cacheStore = mock(ResearchSourceCacheStore.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResearchProperties properties = new ResearchProperties();
    private final ResearchSourceCacheService service = new ResearchSourceCacheService(
            cacheStore, objectMapper, properties);

    @Test
    void shouldLoadAndCacheResponseOnCacheMiss() {
        when(cacheStore.find(anyString())).thenReturn(Optional.empty());
        CachePolicy policy = CachePolicy.ttl(60_000L);

        CachedPayload result = service.getOrLoad(
                SellerSpriteOperation.MARKET_STATISTICS,
                Map.of("marketplace", "US", "month", "202507"),
                policy,
                () -> objectMapper.createObjectNode().put("products", 100));

        assertThat(result.payload().path("products").asInt()).isEqualTo(100);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(cacheStore).put(keyCaptor.capture(), org.mockito.ArgumentMatchers.eq(result),
                org.mockito.ArgumentMatchers.eq(policy));
        assertThat(keyCaptor.getValue())
                .startsWith("sellersprite:research:source:v1:market_statistics:");
    }

    @Test
    void shouldReturnCachedResponseWithoutCallingSource() {
        CachedPayload cached = new CachedPayload(
                objectMapper.createObjectNode().put("products", 88),
                1);
        when(cacheStore.find(anyString())).thenReturn(Optional.of(cached));
        AtomicInteger loads = new AtomicInteger();

        CachedPayload result = service.getOrLoad(
                SellerSpriteOperation.MARKET_STATISTICS,
                Map.of("month", "202507"),
                CachePolicy.ttl(60_000L),
                () -> {
                    loads.incrementAndGet();
                    return objectMapper.createObjectNode();
                });

        assertThat(result).isSameAs(cached);
        assertThat(loads).hasValue(0);
        verify(cacheStore, never()).put(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotCacheEmptySourceResponse() {
        when(cacheStore.find(anyString())).thenReturn(Optional.empty());

        CachedPayload result = service.getOrLoad(
                SellerSpriteOperation.MARKET_PERFORMANCE,
                Map.of("month", "202507"),
                CachePolicy.permanent(),
                () -> objectMapper.createObjectNode().putArray("items"));

        assertThat(result.recordCount()).isZero();
        verify(cacheStore, never()).put(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldEvictEmptyCachedResponseAndReloadSource() {
        CachedPayload empty = new CachedPayload(
                objectMapper.createObjectNode().putArray("items"),
                0);
        when(cacheStore.find(anyString())).thenReturn(Optional.of(empty));

        CachedPayload result = service.getOrLoad(
                SellerSpriteOperation.MARKET_PERFORMANCE,
                Map.of("month", "202507"),
                CachePolicy.permanent(),
                () -> objectMapper.createObjectNode()
                        .put("asinCount", 100)
                        .putArray("items")
                        .addObject()
                        .put("month", "202507"));

        assertThat(result.recordCount()).isEqualTo(1);
        verify(cacheStore).delete(anyString());
        verify(cacheStore).put(
                anyString(),
                org.mockito.ArgumentMatchers.eq(result),
                org.mockito.ArgumentMatchers.eq(CachePolicy.permanent()));
    }

    @Test
    void shouldBypassRedisWhenCacheIsDisabled() {
        properties.getSourceCache().setEnabled(false);

        CachedPayload result = service.getOrLoad(
                SellerSpriteOperation.PRODUCT_NODE,
                Map.of("marketplace", "US"),
                CachePolicy.ttl(60_000L),
                () -> objectMapper.createArrayNode().add("node-1"));

        assertThat(result.recordCount()).isEqualTo(1);
        verifyNoInteractions(cacheStore);
    }

    @Test
    void shouldKeepHistoricalCategoryMonthsPermanentlyAndExpireCurrentMonth() {
        String currentMonth = YearMonth.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        assertThat(service.categoryPolicy("202001").isPermanent()).isTrue();
        assertThat(service.categoryPolicy(currentMonth).ttlMs())
                .isEqualTo(properties.getSourceCache().getCategoryTtlMs());
        assertThat(service.productNodePolicy().ttlMs())
                .isEqualTo(properties.getSourceCache().getProductNodeTtlMs());
        assertThat(service.asinPolicy().ttlMs())
                .isEqualTo(properties.getSourceCache().getAsinTtlMs());
    }
}
