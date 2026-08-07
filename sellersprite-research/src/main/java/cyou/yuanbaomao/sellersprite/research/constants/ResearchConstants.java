package cyou.yuanbaomao.sellersprite.research.constants;

/**
 * 市场调研模块稳定常量。
 */
public final class ResearchConstants {

    /** 任务接口使用的业务月份格式，示例：2026-07。 */
    public static final String RESEARCH_MONTH_PATTERN = "^\\d{4}-(0[1-9]|1[0-2])$";
    /** SellerSprite 类目节点路径格式，节点之间使用冒号分隔。 */
    public static final String NODE_ID_PATH_PATTERN = "^\\d+(?::\\d+)*$";
    public static final String MARKETPLACE_US = "US";
    public static final String TEMPLATE_CODE = "market-research-v1";
    public static final String WORKFLOW_VERSION = "market-research-v6-cache-insights";
    public static final String DATASET_SCHEMA_VERSION = "1.0";
    /** 阶段一外部接口原始响应工作簿。 */
    public static final String ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK = "STAGE1_RAW_WORKBOOK";
    /** 阶段一市场初筛证据工作簿。 */
    public static final String ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK = "STAGE1_EVIDENCE_WORKBOOK";
    /** 阶段二评论、关键词与ASIN趋势原始响应工作簿。 */
    public static final String ARTIFACT_TYPE_STAGE2_RAW_WORKBOOK = "STAGE2_RAW_WORKBOOK";
    /** 阶段二评价、VOC、Keywords 与ASIN趋势证据工作簿。 */
    public static final String ARTIFACT_TYPE_STAGE2_EVIDENCE_WORKBOOK = "STAGE2_EVIDENCE_WORKBOOK";
    /** 最终市场调研 Markdown 报告。 */
    public static final String ARTIFACT_TYPE_AI_ANALYSIS_REPORT = "AI_ANALYSIS_REPORT";
    /** 尚未阶段化的调用默认写入阶段一原始工作簿类型。 */
    @Deprecated
    public static final String ARTIFACT_TYPE_RAW_DATA_WORKBOOK = ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK;
    /** 尚未阶段化的调用默认写入阶段一证据工作簿类型。 */
    @Deprecated
    public static final String ARTIFACT_TYPE_EVIDENCE_WORKBOOK = ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK;
    public static final String MARKDOWN_MEDIA_TYPE = "text/markdown;charset=UTF-8";
    public static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String ERROR_CODE_EXECUTION_FAILED = "MR_EXECUTION_FAILED";
    public static final String ERROR_CODE_VALIDATION_FAILED = "MR_VALIDATION_FAILED";
    public static final String ERROR_CODE_CANCELLED = "MR_CANCELLED";
    public static final int MAX_SEED_ASINS = 20;
    public static final String PRODUCTS_DATASET_CODE = "products";
    /** 历史月份类目统计响应的数据集编码前缀，后缀格式为 yyyy-MM。 */
    public static final String HISTORICAL_MARKET_STATISTICS_DATASET_CODE_PREFIX =
            "market.statistics.history.";
    public static final String MARKET_SALES_TREND_DATASET_CODE = "market.sales-trend";
    public static final String MARKET_DEMAND_TREND_DATASET_CODE = "market.demand-trend";
    public static final String KEYWORD_TREND_DATASET_CODE = "keywords.trend";
    public static final String MARKET_RESEARCH_DATASET_CODE = "market.research";
    public static final String MARKET_STATISTICS_DATASET_CODE = "market.statistics";
    public static final String KEYWORDS_DATASET_CODE = "keywords";
    public static final String REVIEWS_DATASET_CODE_PREFIX = "reviews.";
    public static final String TRAFFIC_KEYWORDS_DATASET_CODE_PREFIX = "traffic-keywords.";
    /** 人工选中ASIN的销量趋势数据集编码前缀。 */
    public static final String ASIN_SALES_TREND_DATASET_CODE_PREFIX = "asin-sales-trend.";
    /** 人工选中ASIN的Keepa经营趋势数据集编码前缀。 */
    public static final String ASIN_KEEPA_TREND_DATASET_CODE_PREFIX = "asin-keepa-trend.";
    private ResearchConstants() {
    }
}
