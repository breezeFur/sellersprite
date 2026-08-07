package cyou.yuanbaomao.sellersprite.research.provider;

import java.util.List;

import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;

/**
 * 市场调研数据采集统一入口。
 */
public interface ResearchDataProvider {

    /** 当前 Provider 对应的数据源模式。 */
    ResearchSourceMode sourceMode();

    /** 检查数据源和远端配额接口是否可用，并保留接口返回的完整数据。 */
    List<ResearchDataset> checkQuota(ResearchInput input);

    /** 采集商品池和可选 ASIN 补充数据。 */
    List<ResearchDataset> collectProducts(ResearchInput input);

    /** 采集按月类目聚合市场销售趋势。 */
    List<ResearchDataset> collectMarketSalesTrend(ResearchInput input);

    /** 采集类目需求趋势和可选核心关键词趋势。 */
    List<ResearchDataset> collectKeywordDemandTrend(ResearchInput input);

    /** 采集 SellerSprite 细分市场及分布代理数据。 */
    List<ResearchDataset> collectSegmentOpportunity(ResearchInput input);

    /** 采集种子商品评论数据。 */
    List<ResearchDataset> collectReviews(ResearchInput input);

    /** 采集人工选中 ASIN 的销量与经营趋势。 */
    List<ResearchDataset> collectAsinIntelligence(ResearchInput input);

    /** 采集关键词研究、挖词和竞品反查词数据。 */
    List<ResearchDataset> collectKeywordIntelligence(ResearchInput input);
}
