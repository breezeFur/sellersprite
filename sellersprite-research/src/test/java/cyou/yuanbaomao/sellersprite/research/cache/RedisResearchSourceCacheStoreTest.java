package cyou.yuanbaomao.sellersprite.research.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class RedisResearchSourceCacheStoreTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisResearchSourceCacheStore store =
            new RedisResearchSourceCacheStore(redisTemplate, objectMapper);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReadIntactCachedResponse() throws Exception {
        String cacheKey = "cache:key";
        String responsePayload = "{\"products\":88}";
        ObjectNode entry = objectMapper.createObjectNode()
                .put("responsePayload", responsePayload)
                .put("responseSha256", ResearchHashUtils.sha256(responsePayload))
                .put("recordCount", 1)
                .put("fetchedAt", 123L);
        when(valueOperations.get(cacheKey)).thenReturn(objectMapper.writeValueAsString(entry));

        Optional<CachedPayload> result = store.find(cacheKey);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().payload().path("products").asInt()).isEqualTo(88);
        assertThat(result.orElseThrow().recordCount()).isEqualTo(1);
    }

    @Test
    void shouldDeleteCorruptedCachedResponse() throws Exception {
        String cacheKey = "cache:key";
        ObjectNode entry = objectMapper.createObjectNode()
                .put("responsePayload", "{\"products\":88}")
                .put("responseSha256", "invalid")
                .put("recordCount", 1)
                .put("fetchedAt", 123L);
        when(valueOperations.get(cacheKey)).thenReturn(objectMapper.writeValueAsString(entry));

        assertThat(store.find(cacheKey)).isEmpty();
        verify(redisTemplate).delete(cacheKey);
    }

    @Test
    void shouldWriteCachedResponseWithTtl() throws Exception {
        String cacheKey = "cache:key";
        CachedPayload payload = new CachedPayload(
                objectMapper.createObjectNode().put("products", 100),
                1);

        store.put(cacheKey, payload, CachePolicy.ttl(60_000L));

        ArgumentCaptor<String> entryCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(cacheKey), entryCaptor.capture(), eq(Duration.ofSeconds(60)));
        assertThat(objectMapper.readTree(entryCaptor.getValue()).path("recordCount").asInt()).isEqualTo(1);
    }

    @Test
    void shouldWritePermanentCachedResponseWithoutTtl() {
        String cacheKey = "cache:key";
        CachedPayload payload = new CachedPayload(objectMapper.createArrayNode().add("node-1"), 1);

        store.put(cacheKey, payload, CachePolicy.permanent());

        verify(valueOperations).set(eq(cacheKey), anyString());
    }

    @Test
    void shouldTreatRedisFailureAsCacheMiss() {
        when(valueOperations.get("cache:key")).thenThrow(new IllegalStateException("redis unavailable"));

        assertThat(store.find("cache:key")).isEmpty();
        verify(redisTemplate, never()).delete("cache:key");
    }
}
