package com.yuanbaomao.sellersprite.research.provider;

import java.util.List;

import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;

/**
 * 市场调研数据采集统一入口。
 */
public interface ResearchDataProvider {

    /** 当前 Provider 对应的数据源模式。 */
    ResearchSourceMode sourceMode();

    /** 检查数据源和远端配额接口是否可用，并保留接口返回的完整数据。 */
    List<ResearchDataset> checkQuota(ResearchInput input);

    /** 采集市场和商品数据。 */
    List<ResearchDataset> collectMarketAndProducts(ResearchInput input);

    /** 采集关键词数据。 */
    List<ResearchDataset> collectKeywords(ResearchInput input);

    /** 采集种子商品评论数据。 */
    List<ResearchDataset> collectReviews(ResearchInput input);
}
