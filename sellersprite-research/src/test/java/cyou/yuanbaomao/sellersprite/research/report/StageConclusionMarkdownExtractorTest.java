package cyou.yuanbaomao.sellersprite.research.report;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StageConclusionMarkdownExtractorTest {

    private final StageConclusionMarkdownExtractor extractor =
            new StageConclusionMarkdownExtractor();

    @Test
    void shouldExtractOnlyScorecardSectionThroughDisclaimer() {
        String markdown = """
                ## 阶段一初筛评分速览
                **综合评分：70/100｜推进建议：有条件推进｜置信度：中**

                | 维度 | 评分 | 关键依据 |
                | --- | --- | --- |
                | 市场需求吸引力 | 75 | 搜索需求稳定 |

                > 评分仅代表当前证据下的决策支持度，不是成功率、利润率、ROI 或收益承诺。

                ## 市场进入结论
                建议先验证供应链。
                """;

        assertThat(extractor.extract(markdown, "阶段一初筛评分速览"))
                .isEqualTo("""
                        ## 阶段一初筛评分速览
                        **综合评分：70/100｜推进建议：有条件推进｜置信度：中**

                        | 维度 | 评分 | 关键依据 |
                        | --- | --- | --- |
                        | 市场需求吸引力 | 75 | 搜索需求稳定 |

                        > 评分仅代表当前证据下的决策支持度，不是成功率、利润率、ROI 或收益承诺。
                        """.strip());
    }

    @Test
    void shouldKeepLegacyMarkdownWhenScorecardHeadingIsAbsent() {
        String legacyMarkdown = "## 阶段结论\n\n历史报告没有评分速览。";

        assertThat(extractor.extract(legacyMarkdown, "阶段二深挖评分速览"))
                .isEqualTo(legacyMarkdown);
    }
}
