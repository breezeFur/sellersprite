package cyou.yuanbaomao.sellersprite.ai.research;

import java.util.List;

/** 八个固定市场调研维度及其专用 Agent 分析目标。 */
enum ResearchDimensionAgent {

    MARKET_OVERVIEW(
            "MARKET_OVERVIEW",
            "市场概况",
            "分析市场规模、TOP100商品表现、价格区间、销量与销售额趋势、新品趋势、季节性证据和生命周期。",
            List.of("market.", "products", "asins.")),
    USER_DEMAND(
            "USER_DEMAND",
            "用户需求分析",
            "分析搜索需求、增长方向、评论VOC、评分分布、用户痛点、使用场景和退货风险；人口属性没有证据时必须说明。",
            List.of("keywords", "reviews.", "market.ratings", "market.rating", "market.ebc", "products")),
    COMPETITION(
            "COMPETITION",
            "竞争分析",
            "分析头部品牌与商品、品牌和卖家集中度、价格带、竞争壁垒、市场结构及可验证的竞品特征。",
            List.of("products", "market.goods", "market.brand", "market.seller", "market.price", "asins.")),
    KEYWORDS(
            "KEYWORDS",
            "关键词分析",
            "分析核心词、长尾词、趋势、竞争程度、竞品流量词和广告机会；不得生成证据中不存在的搜索量或排名。",
            List.of("keywords", "traffic-keywords.", "products")),
    PRODUCT_OPPORTUNITY(
            "PRODUCT_OPPORTUNITY",
            "产品机会分析",
            "综合增长、竞争、新品比例、关键词缺口、评论痛点和竞品特征，识别细分机会与差异化方向。",
            List.of("market.", "products", "keywords", "traffic-keywords.", "reviews.", "asins.")),
    POLICY_COMPLIANCE(
            "POLICY_COMPLIANCE",
            "政策法规",
            "仅依据现有商品和类目信息识别潜在合规问题；没有Amazon、FDA、海关或认证权威证据时必须明确标记数据不足，不得给出确定合规结论。",
            List.of("products", "asins.")),
    PRODUCT_RECOMMENDATION(
            "PRODUCT_RECOMMENDATION",
            "产品建议",
            "给出有证据支持的定价、定位、标题关键词、卖点和图片规划建议；缺少Listing或图片证据时必须说明限制。",
            List.of("market.price", "products", "keywords", "traffic-keywords.", "reviews.", "asins.")),
    GO_NO_GO(
            "GO_NO_GO",
            "是否进入市场",
            "综合全部原始证据和前序维度，判断进入、谨慎进入或不进入，解释市场、增长、竞争、利润和差异化方面的依据与缺口。",
            List.of(""));

    private final String code;
    private final String displayName;
    private final String analysisGoal;
    private final List<String> datasetPrefixes;

    ResearchDimensionAgent(
            String code,
            String displayName,
            String analysisGoal,
            List<String> datasetPrefixes) {
        this.code = code;
        this.displayName = displayName;
        this.analysisGoal = analysisGoal;
        this.datasetPrefixes = datasetPrefixes;
    }

    String code() {
        return code;
    }

    String displayName() {
        return displayName;
    }

    String analysisGoal() {
        return analysisGoal;
    }

    boolean accepts(String datasetCode) {
        if (datasetCode == null) {
            return this == GO_NO_GO;
        }
        return datasetPrefixes.stream().anyMatch(datasetCode::startsWith);
    }
}
