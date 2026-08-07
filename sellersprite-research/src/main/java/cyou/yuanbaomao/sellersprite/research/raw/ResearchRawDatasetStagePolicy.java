package cyou.yuanbaomao.sellersprite.research.raw;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import java.util.LinkedHashSet;
import java.util.Set;

/** 定义每个业务阶段可以读取的原始采集节点。 */
public final class ResearchRawDatasetStagePolicy {

    private static final String COLLECTION_NODE_PREFIX = "collection.";
    private static final Set<String> SCREENING_NODE_CODES = Set.of(
            ResearchPhase.COLLECT_PRODUCTS.getNodeCode(),
            ResearchPhase.COLLECT_MARKET_SALES_TREND.getNodeCode(),
            ResearchPhase.COLLECT_KEYWORD_DEMAND_TREND.getNodeCode(),
            ResearchPhase.COLLECT_SEGMENT_OPPORTUNITY.getNodeCode());
    private static final Set<String> DEEP_DIVE_NODE_CODES = Set.of(
            ResearchPhase.COLLECT_ASIN_INTELLIGENCE.getNodeCode(),
            ResearchPhase.COLLECT_REVIEWS.getNodeCode(),
            ResearchPhase.COLLECT_KEYWORD_INTELLIGENCE.getNodeCode());
    private static final Set<String> FINAL_ANALYSIS_NODE_CODES = finalAnalysisNodeCodes();

    private ResearchRawDatasetStagePolicy() {
    }

    public static Set<String> allowedNodeCodes(ResearchStageCode stageCode) {
        return switch (stageCode) {
            case SCREENING -> SCREENING_NODE_CODES;
            case DEEP_DIVE -> DEEP_DIVE_NODE_CODES;
            case FINAL_ANALYSIS -> FINAL_ANALYSIS_NODE_CODES;
        };
    }

    public static boolean allows(ResearchStageCode stageCode, String nodeCode) {
        return nodeCode != null && allowedNodeCodes(stageCode).contains(nodeCode);
    }

    public static boolean isCollectionNode(String nodeCode) {
        return nodeCode != null && nodeCode.startsWith(COLLECTION_NODE_PREFIX);
    }

    private static Set<String> finalAnalysisNodeCodes() {
        Set<String> nodeCodes = new LinkedHashSet<>(SCREENING_NODE_CODES);
        nodeCodes.addAll(DEEP_DIVE_NODE_CODES);
        return Set.copyOf(nodeCodes);
    }
}
