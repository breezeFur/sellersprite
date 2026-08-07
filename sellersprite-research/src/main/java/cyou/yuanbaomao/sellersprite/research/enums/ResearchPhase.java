package cyou.yuanbaomao.sellersprite.research.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.List;

/**
 * 固定市场调研工作流阶段。
 */
@Getter
@RequiredArgsConstructor
public enum ResearchPhase {

    VALIDATE(ResearchGraphCode.COLLECTION, "collection.validate", "校验任务参数", 1),
    CHECK_QUOTA(ResearchGraphCode.COLLECTION, "collection.checkQuota", "检查数据源配置", 3),
    COLLECT_PRODUCTS(ResearchGraphCode.COLLECTION, "collection.collectProducts", "采集商品池", 8),
    COLLECT_MARKET_SALES_TREND(
            ResearchGraphCode.COLLECTION,
            "collection.collectMarketSalesTrend",
            "采集市场销售趋势",
            14),
    COLLECT_KEYWORD_DEMAND_TREND(
            ResearchGraphCode.COLLECTION,
            "collection.collectKeywordDemandTrend",
            "采集关键词需求趋势",
            19),
    COLLECT_SEGMENT_OPPORTUNITY(
            ResearchGraphCode.COLLECTION,
            "collection.collectSegmentOpportunity",
            "采集细分市场机会",
            24),
    COLLECT_REVIEWS(ResearchGraphCode.COLLECTION, "collection.collectReviews", "采集评论", 29),
    COLLECT_ASIN_INTELLIGENCE(
            ResearchGraphCode.COLLECTION,
            "collection.collectAsinIntelligence",
            "采集ASIN经营情报",
            32),
    COLLECT_KEYWORD_INTELLIGENCE(
            ResearchGraphCode.COLLECTION,
            "collection.collectKeywordIntelligence",
            "采集关键词情报",
            34),
    VALIDATE_RAW_DATA(
            ResearchGraphCode.COLLECTION,
            "collection.validateRawData",
            "校验原始数据集",
            36),
    RENDER_RAW_WORKBOOK(
            ResearchGraphCode.COLLECTION,
            "collection.renderRawWorkbook",
            "生成原始数据Excel",
            38),
    PUBLISH_RAW_WORKBOOK(
            ResearchGraphCode.COLLECTION,
            "collection.publishRawWorkbook",
            "发布原始数据Excel",
            40),

    PREPARE_US_EVIDENCE(
            ResearchGraphCode.EVIDENCE, "evidence.prepareUs", "整理US商品证据", 44),
    PREPARE_SALES_TREND_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareSalesTrend",
            "整理行业销售趋势证据",
            48),
    PREPARE_DEMAND_TREND_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareDemandTrend",
            "整理行业需求趋势证据",
            52),
    PREPARE_SEGMENT_MARKET_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareSegmentMarket",
            "整理细分市场证据",
            56),
    PREPARE_SEGMENT_RETURN_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareSegmentReturn",
            "整理细分市场退货证据",
            60),
    PREPARE_BRAND_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareBrand",
            "整理竞品品牌证据",
            64),
    PREPARE_CONCENTRATION_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareConcentration",
            "整理商品集中度证据",
            68),
    PREPARE_REVIEW_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareReview",
            "整理评价证据",
            72),
    PREPARE_VOC_EVIDENCE(
            ResearchGraphCode.EVIDENCE, "evidence.prepareVoc", "整理VOC证据", 76),
    PREPARE_KEYWORD_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareKeyword",
            "整理关键词证据",
            80),
    PREPARE_ASIN_SALES_TREND_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareAsinSalesTrend",
            "整理ASIN销售趋势证据",
            81),
    PREPARE_ASIN_OPERATION_TREND_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.prepareAsinOperationTrend",
            "整理ASIN运营趋势证据",
            82),
    VALIDATE_EVIDENCE(
            ResearchGraphCode.EVIDENCE,
            "evidence.validateDatasets",
            "校验证据数据集",
            83),
    RENDER_EVIDENCE_WORKBOOK(
            ResearchGraphCode.EVIDENCE,
            "evidence.renderWorkbook",
            "生成证据Excel",
            85),
    PUBLISH_EVIDENCE_WORKBOOK(
            ResearchGraphCode.EVIDENCE,
            "evidence.publishWorkbook",
            "发布证据Excel",
            87),

    RUN_INITIAL_ANALYSIS(
            ResearchGraphCode.REPORT,
            "report.runInitialAnalysis",
            "生成AI分析报告",
            100);

    private final ResearchGraphCode graphCode;
    private final String nodeCode;
    private final String displayName;
    private final int progress;

    public int getStartProgress() {
        return ordinal() == 0 ? 0 : values()[ordinal() - 1].progress;
    }

    public static List<ResearchPhase> phases(ResearchGraphCode graphCode) {
        return Arrays.stream(values())
                .filter(phase -> phase.graphCode == graphCode)
                .toList();
    }
}
