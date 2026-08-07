package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachePolicy;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** 在任务数据集之外跨任务复用 SellerSprite 外部响应。 */
@Service
@RequiredArgsConstructor
public class ResearchSourceCacheService {

    private static final String CACHE_KEY_PREFIX = "sellersprite:research:source:v1:";
    private static final DateTimeFormatter SELLER_SPRITE_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMM");

    private final ResearchSourceCacheStore cacheStore;
    private final ObjectMapper objectMapper;
    private final ResearchProperties properties;

    public CachedPayload getOrLoad(
            SellerSpriteOperation operation,
            Object effectiveRequest,
            CachePolicy policy,
            Supplier<JsonNode> loader) {
        Objects.requireNonNull(operation, "operation 不能为空");
        Objects.requireNonNull(effectiveRequest, "effectiveRequest 不能为空");
        Objects.requireNonNull(policy, "policy 不能为空");
        Objects.requireNonNull(loader, "loader 不能为空");

        if (!properties.getSourceCache().isEnabled()) {
            return loadFromSource(loader);
        }

        String cacheKey = buildCacheKey(operation, effectiveRequest);
        CachedPayload cached = cacheStore.find(cacheKey).orElse(null);
        if (cached != null && cached.recordCount() > 0) {
            return cached;
        }
        if (cached != null) {
            cacheStore.delete(cacheKey);
        }
        return loadAndCache(cacheKey, policy, loader);
    }

    public CachePolicy categoryPolicy(String sellerSpriteMonth) {
        YearMonth requested = YearMonth.parse(sellerSpriteMonth, SELLER_SPRITE_MONTH_FORMATTER);
        if (requested.isBefore(YearMonth.now(ZoneOffset.UTC))) {
            return CachePolicy.permanent();
        }
        return CachePolicy.ttl(properties.getSourceCache().getCategoryTtlMs());
    }

    public CachePolicy asinPolicy() {
        return CachePolicy.ttl(properties.getSourceCache().getAsinTtlMs());
    }

    public CachePolicy productNodePolicy() {
        return CachePolicy.ttl(properties.getSourceCache().getProductNodeTtlMs());
    }

    private String buildCacheKey(SellerSpriteOperation operation, Object effectiveRequest) {
        String requestPayload = objectMapper.valueToTree(effectiveRequest).toString();
        return CACHE_KEY_PREFIX
                + operation.name().toLowerCase(Locale.ROOT)
                + ":"
                + ResearchHashUtils.sha256(requestPayload);
    }

    private CachedPayload loadAndCache(
            String cacheKey,
            CachePolicy policy,
            Supplier<JsonNode> loader) {
        CachedPayload loaded = loadFromSource(loader);
        if (loaded.recordCount() > 0) {
            cacheStore.put(cacheKey, loaded, policy);
        }
        return loaded;
    }

    private CachedPayload loadFromSource(Supplier<JsonNode> loader) {
        JsonNode loaded = Objects.requireNonNull(loader.get(), "SellerSprite 缓存回源响应不能为空");
        return new CachedPayload(loaded.deepCopy(), recordCount(loaded));
    }

    private int recordCount(JsonNode payload) {
        if (payload.isArray()) {
            return payload.size();
        }
        JsonNode items = payload.path("items");
        return items.isArray() ? items.size() : 1;
    }
}
