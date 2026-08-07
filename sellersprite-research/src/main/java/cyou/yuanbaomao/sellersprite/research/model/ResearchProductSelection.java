package cyou.yuanbaomao.sellersprite.research.model;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import java.util.List;

/** 已持久化的商品选择关卡输入，供父 Graph 路由和阶段二覆盖采集 ASIN。 */
public record ResearchProductSelection(
        ResearchSelectionDecision decision, List<String> selectedAsins) {
}
