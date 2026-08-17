package cyou.yuanbaomao.sellersprite.research.report;

import java.util.List;

/** 最终报告固定的十二章，与证据目录顺序保持一致。 */
public record ResearchFinalReportSection(
        int order, String code, String datasetCode, String title) {

    public static final List<ResearchFinalReportSection> SECTIONS = List.of(
            new ResearchFinalReportSection(1, "products", "evidence.products", "US"),
            new ResearchFinalReportSection(2, "market-sales-trend", "evidence.market-sales-trend", "行业销售趋势"),
            new ResearchFinalReportSection(3, "market-demand-trend", "evidence.market-demand-trend", "行业需求及趋势"),
            new ResearchFinalReportSection(4, "segment-market", "evidence.segment-market", "细分市场现状"),
            new ResearchFinalReportSection(5, "segment-return", "evidence.segment-return", "细分市场退货率"),
            new ResearchFinalReportSection(6, "competitor-brands", "evidence.competitor-brands", "竞品品牌"),
            new ResearchFinalReportSection(7, "product-concentration", "evidence.product-concentration", "商品集中度"),
            new ResearchFinalReportSection(8, "reviews", "evidence.reviews", "评价"),
            new ResearchFinalReportSection(9, "voc", "evidence.voc", "VOC"),
            new ResearchFinalReportSection(10, "keywords", "evidence.keywords", "Keywords"),
            new ResearchFinalReportSection(11, "asin-sales-trend", "evidence.asin-sales-trend", "ASIN销售趋势"),
            new ResearchFinalReportSection(12, "asin-operation-trend", "evidence.asin-operation-trend", "ASIN运营趋势"));

    public static ResearchFinalReportSection requireByDatasetCode(String datasetCode) {
        return SECTIONS.stream()
                .filter(section -> section.datasetCode().equals(datasetCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知最终报告证据表: " + datasetCode));
    }
}
