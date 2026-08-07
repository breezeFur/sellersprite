package cyou.yuanbaomao.sellersprite.research.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 父市场调研 Graph 内固定的三个子图。 */
@Getter
@RequiredArgsConstructor
public enum ResearchGraphCode {

    COLLECTION("collection", "采集数据"),
    EVIDENCE("evidence", "整理证据"),
    REPORT("report", "生成分析报告");

    private final String code;
    private final String displayName;
}
