package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig.MarketDistributionConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** 应用启动后为活跃远端任务补齐历史市场销售趋势缓存。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResearchMarketTrendCacheWarmup {

    private static final int WARMUP_MONTH_COUNT = 24;
    private static final long REMOTE_REQUEST_INTERVAL_MS = 5_000L;
    private static final DateTimeFormatter SELLER_SPRITE_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMM");

    private final MarketResearchJobDao jobDao;
    private final ResearchInputService inputService;
    private final ResearchSourceCacheService sourceCacheService;
    private final MarketService marketService;
    private final ObjectMapper objectMapper;
    private final ResearchProperties properties;

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean completed = new AtomicBoolean();
    private volatile long lastRemoteRequestAt;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        warmUp();
    }

    public void warmUp() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        try {
            if (!isWarmupRequired()) {
                return;
            }
            Set<WarmupTarget> targets = loadTargets();
            AtomicInteger checkedCount = new AtomicInteger();
            AtomicInteger loadedCount = new AtomicInteger();
            log.info("开始检查市场销售趋势缓存，targetCount={}，monthCount={}",
                    targets.size(), WARMUP_MONTH_COUNT);
            for (WarmupTarget target : targets) {
                if (!warmUpTarget(target, checkedCount, loadedCount)) {
                    return;
                }
            }
            log.info("市场销售趋势缓存检查完成，targetCount={}，checkedCount={}，loadedCount={}",
                    targets.size(), checkedCount.get(), loadedCount.get());
        } catch (RuntimeException exception) {
            log.warn("市场销售趋势启动预热失败，本次跳过并继续启动", exception);
        } finally {
            completed.set(true);
        }
    }

    public boolean isReadyForDispatch() {
        return !isWarmupRequired() || completed.get();
    }

    private boolean isWarmupRequired() {
        return properties.getSourceMode() == ResearchSourceMode.REMOTE
                && properties.getSourceCache().isEnabled();
    }

    private Set<WarmupTarget> loadTargets() {
        Set<WarmupTarget> targets = new LinkedHashSet<>();
        for (MarketResearchJob job : jobDao.listMarketTrendCacheWarmupCandidates()) {
            try {
                targets.add(toTarget(job));
            } catch (RuntimeException exception) {
                log.warn("跳过无效的市场销售趋势预热任务，jobId={}", job.getJobId(), exception);
            }
        }
        return targets;
    }

    private WarmupTarget toTarget(MarketResearchJob job) {
        ResearchInput input = inputService.from(job);
        if (!StringUtils.hasText(input.getNodeIdPath())) {
            throw new IllegalArgumentException("类目节点路径不能为空");
        }
        SellerSpriteMarketplace marketplace =
                Objects.requireNonNull(
                        SellerSpriteMarketplace.fromTransportValue(input.getMarketplace()),
                        "市场编码不能为空");
        YearMonth requestedEndMonth = YearMonth.parse(input.getMonth());
        YearMonth lastCompleteMonth = YearMonth.now(ZoneOffset.UTC).minusMonths(1);
        YearMonth endMonth = requestedEndMonth.isAfter(lastCompleteMonth)
                ? lastCompleteMonth
                : requestedEndMonth;
        MarketDistributionConfig distribution = input.getCollectionConfig()
                .getCollectSegmentOpportunity()
                .getDistribution();
        return new WarmupTarget(
                marketplace,
                input.getNodeIdPath().trim(),
                endMonth,
                distribution.getTopN(),
                distribution.getNewProduct());
    }

    private boolean warmUpTarget(
            WarmupTarget target,
            AtomicInteger checkedCount,
            AtomicInteger loadedCount) {
        for (int offset = WARMUP_MONTH_COUNT - 1; offset >= 0; offset--) {
            YearMonth month = target.endMonth().minusMonths(offset);
            MarketStatisticsRequest request = target.requestFor(month);
            AtomicBoolean loaded = new AtomicBoolean();
            try {
                sourceCacheService.getOrLoad(
                        SellerSpriteOperation.MARKET_STATISTICS,
                        request,
                        sourceCacheService.categoryPolicy(request.getMonth()),
                        loadFromRemote(request, loaded));
                checkedCount.incrementAndGet();
                if (loaded.get()) {
                    loadedCount.incrementAndGet();
                }
            } catch (RuntimeException exception) {
                log.warn(
                        "补齐市场销售趋势缓存失败，本轮预热提前结束，marketplace={}，nodeIdPath={}，month={}",
                        target.marketplace().getCode(),
                        target.nodeIdPath(),
                        month,
                        exception);
                return false;
            }
        }
        return true;
    }

    private Supplier<JsonNode> loadFromRemote(
            MarketStatisticsRequest request,
            AtomicBoolean loaded) {
        return () -> {
            waitForRequestSlot();
            loaded.set(true);
            try {
                return objectMapper.valueToTree(Objects.requireNonNull(
                        marketService.getMarketStatistics(request),
                        "SellerSprite 市场统计响应不能为空"));
            } finally {
                lastRemoteRequestAt = System.currentTimeMillis();
            }
        };
    }

    private void waitForRequestSlot() {
        long waitMs = REMOTE_REQUEST_INTERVAL_MS
                - (System.currentTimeMillis() - lastRemoteRequestAt);
        if (waitMs <= 0) {
            return;
        }
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("市场销售趋势缓存预热线程已中断", exception);
        }
    }

    private record WarmupTarget(
            SellerSpriteMarketplace marketplace,
            String nodeIdPath,
            YearMonth endMonth,
            Integer topN,
            Integer newProduct) {

        private MarketStatisticsRequest requestFor(YearMonth month) {
            MarketStatisticsRequest request = new MarketStatisticsRequest();
            request.setMarketplace(marketplace);
            request.setMonth(month.format(SELLER_SPRITE_MONTH_FORMATTER));
            request.setTopN(topN);
            request.setNewProduct(newProduct);
            request.setNodeIdPath(nodeIdPath);
            return request;
        }
    }
}
