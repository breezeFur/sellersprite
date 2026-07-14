package com.yuanbaomao.sellersprite.research.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 固定市场调研工作流阶段。
 */
@Getter
@RequiredArgsConstructor
public enum ResearchPhase {

    VALIDATE("校验任务参数", 5),
    CHECK_QUOTA("检查数据源配置", 10),
    COLLECT_MARKET_AND_PRODUCTS("采集市场与商品", 35),
    COLLECT_KEYWORDS("采集关键词", 55),
    COLLECT_REVIEWS("采集评论", 70),
    PREPARE_DATA("整理报告数据", 80),
    RENDER_EXCEL("生成Excel", 90),
    VALIDATE_AND_PUBLISH("校验并发布", 100);

    private final String displayName;
    private final int progress;

    public int getStartProgress() {
        return ordinal() == 0 ? 0 : values()[ordinal() - 1].progress;
    }
}
