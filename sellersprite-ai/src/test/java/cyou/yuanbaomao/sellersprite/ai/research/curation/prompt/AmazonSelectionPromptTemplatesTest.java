package cyou.yuanbaomao.sellersprite.ai.research.curation.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AmazonSelectionPromptTemplatesTest {

    @Test
    void shouldConstrainKeywordsAnalysisToSupportedAdvertisingSignals() {
        String prompt = AmazonSelectionPromptTemplates.buildSheetSummaryPrompt(
                "Keywords", "PPC竞价 1.25，搜索量 1000", "判断市场进入难度");

        assertThat(prompt)
                .contains("宣传获客成本信号")
                .contains("关键词竞争强度")
                .contains("投放难度")
                .contains("禁止推算广告预算、ACOS、ROI")
                .contains("缺少曝光、点击、转化和实际花费数据");
    }

    @Test
    void shouldKeepKeywordsConstraintInFinalSummaryPrompt() {
        String prompt = AmazonSelectionPromptTemplates.buildFinalSummaryPrompt(
                "market-research-job-1.xlsx", 12, "Keywords摘要", "判断市场进入难度");

        assertThat(prompt)
                .contains("Keywords 只用于评估宣传获客成本信号、竞争和投放难度")
                .contains("禁止推算预算、ACOS、ROI");
    }

    @Test
    void shouldBuildOnePassFinalEvidencePromptWithPreviousStageContext() {
        String prompt = AmazonSelectionPromptTemplates.buildFinalEvidenceSummaryPrompt(
                "market-research-job-1.xlsx",
                12,
                "Sheet: US\nRow 1: ASIN",
                "判断市场是否值得进入");

        assertThat(prompt)
                .contains("已经完成的 SCREENING 和 DEEP_DIVE 阶段结论")
                .contains("只执行一次最终综合分析")
                .contains("不要重新逐 Sheet 输出分析过程")
                .contains("完整十二表 evidence")
                .contains("所选 ASIN 的销量、价格、BSR、评分和卖家竞争趋势")
                .contains("禁止推算广告预算、ACOS、ROI");
    }

    @Test
    void shouldKeepScreeningPromptInsideStageOneBoundary() {
        String prompt = AmazonSelectionPromptTemplates.buildScreeningSummaryPrompt(
                "run-1",
                "stage1.xlsx",
                7,
                "七张表摘要",
                "items[].customSignal：[尚未引用]",
                "判断市场是否值得进入");

        assertThat(prompt)
                .contains("ScreeningAnalysisAgent")
                .contains("Top20 候选商品")
                .contains("不要求评论、VOC、Keywords 或选中 ASIN 深挖数据已经存在")
                .contains("items[].customSignal")
                .contains("## US", "## 行业销售趋势", "## 商品集中度")
                .contains("正文不要列数字、排名、比例、逐月数据或 Markdown 数据表")
                .contains("generateResearchReportChart", "market-sales-trend", "market-demand-trend")
                .doesNotContain("### Sheet 定位")
                .doesNotContain("数据缺口与下一步验证");
    }

    @Test
    void shouldFocusDeepDivePromptOnSelectedAsinsAndAdvertisingSignals() {
        String prompt = AmazonSelectionPromptTemplates.buildDeepDiveSummaryPrompt(
                "run-2",
                "stage2.xlsx",
                5,
                "五张表摘要",
                "items[].content：[证据已引用]",
                "判断产品风险");

        assertThat(prompt)
                .contains("DeepDiveAnalysisAgent")
                .contains("人工选中 ASIN")
                .contains("宣传获客成本信号")
                .contains("禁止推算预算、ACOS、ROI")
                .contains("generateResearchReportChart", "keywords")
                .contains("所选 ASIN 趋势用于判断样本商品，不能外推为全市场表现")
                .contains("定向星级样本不得外推总体差评率、平均星级或满意度");
    }

    @Test
    void shouldAllowFinalAgentToInspectRawFieldsWithoutRepeatingSheetAnalysis() {
        String prompt = AmazonSelectionPromptTemplates.buildFinalDecisionPrompt(
                "run-3",
                "final.xlsx",
                12,
                "十二张表证据",
                "items[].signal：[尚未引用]",
                "给出最终决策");

        assertThat(prompt)
                .contains("FinalDecisionAgent")
                .contains("不重新逐 Sheet 输出分析过程")
                .contains("只读原始数据工具")
                .contains("items[].signal")
                .contains("不能把定向样本外推为评论总体")
                .contains("## 1. US", "## 10. Keywords", "## 12. ASIN运营趋势")
                .contains("章节评分")
                .contains("不要输出数据清单或 Markdown 数据表")
                .contains("竞品反查词")
                .contains("行业销售趋势必须围绕销量图")
                .contains("generateResearchReportChart")
                .contains("market-sales-trend", "market-demand-trend", "keywords")
                .contains("把工具返回的 Mermaid Markdown 原样放在 `### 核心结论` 前")
                .contains("不得自行编写、修改或补充图表源码和图表数值")
                .contains("同一会话中已经完成的阶段一和阶段二结论");
    }

    @Test
    void shouldRequireStageSpecificScorecardsBeforeAllThreeAnalysisReports() {
        String screeningPrompt = AmazonSelectionPromptTemplates.buildScreeningSummaryPrompt(
                "run-1", "stage1.xlsx", 7, "七张表摘要", "阶段一字段", "判断市场是否值得进入");
        String deepDivePrompt = AmazonSelectionPromptTemplates.buildDeepDiveSummaryPrompt(
                "run-2", "stage2.xlsx", 5, "五张表摘要", "阶段二字段", "判断产品是否可行");
        String finalPrompt = AmazonSelectionPromptTemplates.buildFinalDecisionPrompt(
                "run-3", "final.xlsx", 12, "十二张表证据", "完整字段", "给出最终决策");

        assertScorecardContract(screeningPrompt,
                "市场需求吸引力", "增长与季节稳定性", "竞争可进入性", "利润与退货安全性", "候选商品机会");
        assertThat(screeningPrompt).contains("## 阶段一初筛评分速览");
        assertThat(screeningPrompt.indexOf("## 阶段一初筛评分速览"))
                .isLessThan(screeningPrompt.indexOf("【阶段一报告结构】"));

        assertScorecardContract(deepDivePrompt,
                "用户需求明确度", "痛点可解决性", "获客可行性", "样本商品可复制性", "产品风险可控性");
        assertThat(deepDivePrompt).contains("## 阶段二深挖评分速览");
        assertThat(deepDivePrompt.indexOf("## 阶段二深挖评分速览"))
                .isLessThan(deepDivePrompt.indexOf("【阶段二报告结构】"));

        assertScorecardContract(finalPrompt,
                "市场需求与增长", "竞争可进入性", "产品差异化机会", "盈利安全性", "获客可行性", "执行风险可控性");
        assertThat(finalPrompt).contains("## 最终决策评分速览");
        assertThat(finalPrompt.indexOf("## 最终决策评分速览"))
                .isLessThan(finalPrompt.indexOf("【最终报告结构】"));
    }

    private void assertScorecardContract(String prompt, String... dimensions) {
        assertThat(prompt)
                .contains("第一个非空内容必须是二级标题")
                .contains("维度 | 评分 | 关键依据")
                .contains("只能取 5 的倍数")
                .contains("分数越高表示越支持继续推进")
                .contains("推进建议：{值得推进/有条件推进/谨慎验证/暂不推进/补充证据}")
                .contains("置信度：{高/中/低}")
                .contains("评分写“证据不足”")
                .contains("维度总数的 80%")
                .contains("不是成功率、利润率、ROI 或收益承诺");
        assertThat(prompt).contains(dimensions);
    }
}
