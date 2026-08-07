package cyou.yuanbaomao.sellersprite.research.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;

import lombok.Data;

/**
 * 市场调研工作流配置。
 */
@Data
@ConfigurationProperties(prefix = ResearchProperties.PREFIX)
public class ResearchProperties {

    /** 市场调研配置前缀。 */
    public static final String PREFIX = "sellersprite.research";

    /** 默认使用本地 Mock，确保开发和自动化测试不依赖远端网络。 */
    private ResearchSourceMode sourceMode = ResearchSourceMode.MOCK;

    /** 是否启用以数据库任务表为事实源的Graph Dispatcher。 */
    private boolean dispatcherEnabled = true;

    /** 单次轮询最多抢占的任务数。 */
    private int dispatchBatchSize = 20;

    /** Dispatcher 数据库轮询间隔，单位毫秒。 */
    private long pollIntervalMs = 2_000L;

    /** 新任务最大自动执行次数。 */
    private int maxAttempts = 3;

    /** 单次执行租约时长，单位毫秒。 */
    private long leaseDurationMs = 60_000L;

    /** 执行期间心跳间隔，单位毫秒。 */
    private long heartbeatIntervalMs = 15_000L;

    /** 自动重试首次退避时长，单位毫秒。 */
    private long retryBaseDelayMs = 5_000L;

    /** 自动重试最大退避时长，单位毫秒。 */
    private long retryMaxDelayMs = 300_000L;

    /** Remote模式下执行详情、趋势和流量扩展的最大去重种子ASIN数。 */
    private int remoteEnrichmentAsinLimit = 5;

    /** 跨任务 SellerSprite 外部响应缓存配置。 */
    private SourceCache sourceCache = new SourceCache();

    /** 市场调研持久化事件流配置。 */
    private EventStream eventStream = new EventStream();

    /**
     * 是否允许 Graph Saver 自动创建 checkpoint 表。
     * 生产环境保持关闭并由 SQL 迁移管理结构，测试环境可显式开启。
     */
    private boolean checkpointInitializeSchema = false;

    /** 版本化 Mock fixture 的 Spring Resource 地址。 */
    private String mockFixtureLocation = "classpath:research/mock/v1/market-research.json";

    /** 生成报告的本地输出目录。 */
    private String outputDirectory = "./data/market-research";

    /** 主动推送型SSE的回放批次、心跳与连接限制。 */
    @Data
    public static class EventStream {

        /** 单条 SSE 连接服务端最大存活时间，单位毫秒。 */
        private long timeoutMs = 30 * 60 * 1_000L;

        /** 无业务事件时发送非持久化心跳的间隔，单位毫秒。 */
        private long heartbeatIntervalMs = 15_000L;

        /** 单次从数据库回放的最大事件数量。 */
        private int replayBatchSize = 500;

        /** 实时事件短窗口聚合时长，单位毫秒；只等待内存事件，不查询数据库。 */
        private long liveBatchWindowMs = 20L;

        /** 单条连接尚未写出的最大事件数；溢出时断开并由Last-Event-ID重放恢复。 */
        private int outboundQueueCapacity = 1024;
    }

    /** SellerSprite Redis 外部响应缓存配置。 */
    @Data
    public static class SourceCache {

        /** 是否启用 SellerSprite 外部响应缓存；关闭时直接回源且不访问 Redis。 */
        private boolean enabled = true;

        /** 当前自然月类目响应有效期，单位毫秒。 */
        private long categoryTtlMs = 24 * 60 * 60 * 1_000L;

        /** 产品类目树与类目搜索响应有效期，单位毫秒。 */
        private long productNodeTtlMs = 7 * 24 * 60 * 60 * 1_000L;

        /** ASIN 销量与经营趋势响应有效期，单位毫秒。 */
        private long asinTtlMs = 24 * 60 * 60 * 1_000L;
    }
}
