package cyou.yuanbaomao.sellersprite.research.evidence;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** 证据数据集、工作表与稳定列的唯一映射。 */
public final class ResearchEvidenceCatalog {

    /** 商品图片展示字段。 */
    public static final String IMAGE_FIELD = "图片";
    /** 商品原始图片链接字段。 */
    public static final String IMAGE_URL_FIELD = "图片链接";

    /** 阶段一固定七张市场初筛证据表。 */
    public static final List<Definition> SCREENING_DEFINITIONS = List.of(
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_US_EVIDENCE,
                    "evidence.products",
                    "US",
                    "排名", "ASIN", IMAGE_FIELD, IMAGE_URL_FIELD, "品牌", "标题", "类目",
                    "BSR", "BSR增长率", "月销量", "销量增长率", "月销售额($)", "价格($)",
                    "毛利率", "评分数", "月新增评分", "留评率", "评分", "变体数", "FBA费用($)",
                    "卖家数", "卖家名称", "卖家所属地", "配送方式", "上架时间", "包装重量",
                    "重量", "包装尺寸", "尺寸", "父体ASIN", "SKU", "A+", "视频"),
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_SALES_TREND_EVIDENCE,
                    "evidence.market-sales-trend",
                    "行业销售趋势",
                    "月份", "商品样本数", "样本总月销量", "样本总月销售额($)",
                    "单品月均销量", "单品月均销售额($)", "平均BSR", "平均价格($)",
                    "平均评分数", "平均评分", "品牌数", "卖家数", "新品数", "新品占比",
                    "平均毛利率", "平均卖家数"),
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_DEMAND_TREND_EVIDENCE,
                    "evidence.market-demand-trend",
                    "行业需求及趋势",
                    "来源类型", "关键词", "月份", "浏览量/搜索量", "购买量", "购买率", "商品数",
                    "SellerSprite退货率", "类目平均退货率", "搜索购买比", "类目平均搜索购买比"),
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_SEGMENT_MARKET_EVIDENCE,
                    "evidence.segment-market",
                    "细分市场现状",
                    "排名", "细分市场", "类目路径", "商品总数", "样本商品数", "月销量", "月销售额($)",
                    "单品月均销量", "单品月均销售额($)", "平均价格($)", "平均BSR", "平均评分数",
                    "平均评分", "平均毛利率", "平均卖家数", "品牌数", "卖家数", "A+占比",
                    "Amazon自营占比", "FBA占比", "FBM占比", "主要卖家所属地", "主要卖家地区占比",
                    "搜索购买比", "退货率", "近1月新品占比", "近3月新品占比", "近6月新品占比",
                    "近12月新品占比", "头部3商品集中度", "头部10商品集中度"),
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_SEGMENT_RETURN_EVIDENCE,
                    "evidence.segment-return",
                    "细分市场退货率",
                    "细分市场", "类目路径", "SellerSprite退货率", "类目平均退货率", "退货原因", "退货量"),
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_BRAND_EVIDENCE,
                    "evidence.competitor-brands",
                    "竞品品牌",
                    "排名", "品牌", "代表ASIN", "样本商品数", "月销量", "月销售额($)",
                    "平均价格($)", "平均评分", "平均评分数", "平均变体数", "主要卖家所属地",
                    "FBA商品占比", "样本销量份额", "产品线线索"),
            definition(
                    EvidenceStage.SCREENING,
                    ResearchPhase.PREPARE_CONCENTRATION_EVIDENCE,
                    "evidence.product-concentration",
                    "商品集中度",
                    "排名", "集中维度", "对象/区间", "ASIN", "品牌/卖家", "商品数", "月销量",
                    "月销售额($)", "占比", "平均评分", "平均价格($)"));

    /** 阶段二固定五张评论、关键词与ASIN趋势深挖证据表。 */
    public static final List<Definition> DEEP_DIVE_DEFINITIONS = List.of(
            definition(
                    EvidenceStage.DEEP_DIVE,
                    ResearchPhase.PREPARE_REVIEW_EVIDENCE,
                    "evidence.reviews",
                    "评价",
                    "ASIN", "作者", "标题", "内容", "评论时间", "星级", "VP评论", "Vine评论",
                    "型号/SKU", "赞同数", "图片", "视频"),
            definition(
                    EvidenceStage.DEEP_DIVE,
                    ResearchPhase.PREPARE_VOC_EVIDENCE,
                    "evidence.voc",
                    "VOC",
                    "ASIN", "评论样本数", "平均星级", "正向样本数", "负向样本数", "代表正向评价",
                    "代表负向评价", "用户画像", "使用场景", "购买动机"),
            definition(
                    EvidenceStage.DEEP_DIVE,
                    ResearchPhase.PREPARE_KEYWORD_EVIDENCE,
                    "evidence.keywords",
                    "Keywords",
                    "来源类型", "关联ASIN", "关键词", "中文翻译", "月份", "月搜索量", "点击量",
                    "曝光量", "购买量", "购买率", "商品数", "供需比", "PPC建议竞价", "PPC最低价",
                    "PPC最高价", "自然排名", "广告排名", "标题密度", "搜索量增长率"),
            definition(
                    EvidenceStage.DEEP_DIVE,
                    ResearchPhase.PREPARE_ASIN_SALES_TREND_EVIDENCE,
                    "evidence.asin-sales-trend",
                    "ASIN销售趋势",
                    "ASIN", "品牌", "标题", "月份", "父体销量", "子体销量",
                    "父体销售额($)", "子体销售额($)", "标价($)", "平均价格($)"),
            definition(
                    EvidenceStage.DEEP_DIVE,
                    ResearchPhase.PREPARE_ASIN_OPERATION_TREND_EVIDENCE,
                    "evidence.asin-operation-trend",
                    "ASIN运营趋势",
                    "ASIN", "数据ASIN", "父体ASIN", "品牌", "标题", "类目", "指标", "时间", "数值"));

    /** 按稳定顺序组合的完整十二张证据表。 */
    public static final List<Definition> DEFINITIONS = Stream.concat(
                    SCREENING_DEFINITIONS.stream(), DEEP_DIVE_DEFINITIONS.stream())
            .toList();

    private ResearchEvidenceCatalog() {
    }

    public static List<Definition> definitions(EvidenceStage stage) {
        return switch (Objects.requireNonNull(stage, "证据阶段不能为空")) {
            case SCREENING -> SCREENING_DEFINITIONS;
            case DEEP_DIVE -> DEEP_DIVE_DEFINITIONS;
        };
    }

    public static Definition requireByPhase(ResearchPhase phase) {
        return DEFINITIONS.stream()
                .filter(definition -> definition.phase() == phase)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("阶段不是证据整理节点: " + phase));
    }

    public static Definition requireByDatasetCode(String datasetCode) {
        return DEFINITIONS.stream()
                .filter(definition -> definition.datasetCode().equals(datasetCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知证据数据集: " + datasetCode));
    }

    private static Definition definition(
            EvidenceStage stage,
            ResearchPhase phase,
            String datasetCode,
            String sheetName,
            String... businessColumns) {
        return new Definition(stage, phase, datasetCode, sheetName, List.of(businessColumns));
    }

    public record Definition(
            EvidenceStage stage,
            ResearchPhase phase,
            String datasetCode,
            String sheetName,
            List<String> columns) {
    }
}
