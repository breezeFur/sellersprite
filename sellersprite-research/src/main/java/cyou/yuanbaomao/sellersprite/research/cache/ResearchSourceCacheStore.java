package cyou.yuanbaomao.sellersprite.research.cache;

import java.util.Optional;
import tools.jackson.databind.JsonNode;

/** SellerSprite 外部响应缓存的存储边界。 */
public interface ResearchSourceCacheStore {

    Optional<CachedPayload> find(String cacheKey);

    void put(String cacheKey, CachedPayload payload, CachePolicy policy);

    void delete(String cacheKey);

    record CachedPayload(JsonNode payload, int recordCount) {
    }

    record CachePolicy(Long ttlMs) {

        public CachePolicy {
            if (ttlMs != null && ttlMs <= 0) {
                throw new IllegalArgumentException("缓存TTL必须大于0");
            }
        }

        public static CachePolicy permanent() {
            return new CachePolicy(null);
        }

        public static CachePolicy ttl(long ttlMs) {
            return new CachePolicy(ttlMs);
        }

        public boolean isPermanent() {
            return ttlMs == null;
        }
    }
}
