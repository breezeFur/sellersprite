package cyou.yuanbaomao.sellersprite.research.cache;

import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/** 使用项目现有 Spring Data Redis 客户端保存 SellerSprite 外部响应。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisResearchSourceCacheStore implements ResearchSourceCacheStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<CachedPayload> find(String cacheKey) {
        String serializedEntry;
        try {
            serializedEntry = redisTemplate.opsForValue().get(cacheKey);
        } catch (RuntimeException exception) {
            log.warn("读取SellerSprite Redis缓存失败，将重新回源 cacheKey={}, error={}",
                    cacheKey, exception.getMessage());
            return Optional.empty();
        }
        if (!StringUtils.hasText(serializedEntry)) {
            return Optional.empty();
        }

        try {
            RedisCacheEntry entry = objectMapper.readValue(serializedEntry, RedisCacheEntry.class);
            if (!Objects.equals(entry.responseSha256(), ResearchHashUtils.sha256(entry.responsePayload()))) {
                log.warn("SellerSprite Redis缓存完整性校验失败，将重新回源 cacheKey={}", cacheKey);
                deleteSafely(cacheKey);
                return Optional.empty();
            }
            return Optional.of(new CachedPayload(
                    objectMapper.readTree(entry.responsePayload()),
                    entry.recordCount()));
        } catch (Exception exception) {
            log.warn("解析SellerSprite Redis缓存失败，将重新回源 cacheKey={}, error={}",
                    cacheKey, exception.getMessage());
            deleteSafely(cacheKey);
            return Optional.empty();
        }
    }

    @Override
    public void put(String cacheKey, CachedPayload payload, CachePolicy policy) {
        try {
            String responsePayload = objectMapper.writeValueAsString(payload.payload());
            RedisCacheEntry entry = new RedisCacheEntry(
                    responsePayload,
                    ResearchHashUtils.sha256(responsePayload),
                    payload.recordCount(),
                    System.currentTimeMillis());
            String serializedEntry = objectMapper.writeValueAsString(entry);
            if (policy.isPermanent()) {
                redisTemplate.opsForValue().set(cacheKey, serializedEntry);
                return;
            }
            redisTemplate.opsForValue().set(
                    cacheKey,
                    serializedEntry,
                    Duration.ofMillis(policy.ttlMs()));
        } catch (Exception exception) {
            log.warn("保存SellerSprite Redis缓存失败，本次继续使用回源结果 cacheKey={}, error={}",
                    cacheKey, exception.getMessage());
        }
    }

    @Override
    public void delete(String cacheKey) {
        deleteSafely(cacheKey);
    }

    private void deleteSafely(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
        } catch (RuntimeException exception) {
            log.warn("删除无效SellerSprite Redis缓存失败 cacheKey={}, error={}",
                    cacheKey, exception.getMessage());
        }
    }

    private record RedisCacheEntry(
            String responsePayload,
            String responseSha256,
            int recordCount,
            long fetchedAt) {
    }
}
