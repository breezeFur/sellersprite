package cyou.yuanbaomao.sellersprite.ai.research.curation.prompt;

/**
 * 亚马逊选品 Agent 的提示词模板。
 */
public final class AmazonSelectionPromptTemplates {

    private static final String REPORT_DIMENSIONS = """
            - 市场现状：市场容量、销售趋势、TOP100 产品数据、平均售价、平均销量、产品集中度、市场变化曲线。
            - 关键词趋势：搜索量变化、季节性变化、热度周期、核心词、长尾词、品牌词。
            - 退货与评论分析：高频差评关键词、用户抱怨点、产品缺点、好评卖点、退货风险。
            - 竞争格局：品牌集中度、卖家集中度、市场竞争程度、价格分布、头部品牌或卖家压力。
            - 定价与产品策略：主流价格带、规格、卖点、差异化方向、广告或社媒打法。
            - 合规与政策风险：只有当前数据明确出现法规、FDA、MOCRA、禁用成分、商标、专利、平台审核等内容时才总结。
            """;
    private static final String KEYWORDS_ANALYSIS_RULES = """

                【Keywords 专项约束】
                - Keywords 数据用于评估宣传获客成本信号、关键词竞争强度和投放难度。
                - PPC 竞价只能作为宣传成本与竞争难度的参考，不能直接等同于实际广告成本。
                - 缺少曝光、点击、转化和实际花费数据时，禁止推算广告预算、ACOS、ROI 或确定性投放回报。
                """;
    private static final String SCREENING_SCORE_DIMENSIONS = """
            - 市场需求吸引力
            - 增长与季节稳定性
            - 竞争可进入性
            - 利润与退货安全性
            - 候选商品机会
            """;
    private static final String DEEP_DIVE_SCORE_DIMENSIONS = """
            - 用户需求明确度
            - 痛点可解决性
            - 获客可行性
            - 样本商品可复制性
            - 产品风险可控性
            """;
    private static final String FINAL_SCORE_DIMENSIONS = """
            - 市场需求与增长
            - 竞争可进入性
            - 产品差异化机会
            - 盈利安全性
            - 获客可行性
            - 执行风险可控性
            """;

    private AmazonSelectionPromptTemplates() {
    }

    public static String buildSheetSummaryPrompt(String sheetName, String sheetObservation) {
        return buildSheetSummaryPrompt(sheetName, sheetObservation, "");
    }

    public static String buildSheetSummaryPrompt(String sheetName, String sheetObservation, String userAnalysisGoal) {
        return """
                你是亚马逊选品市场调研分析师。

                你的任务是：只基于“当前 Sheet observation”分析这个 sheet 对选品决策的价值。
                不要调用工具，不要使用其他 sheet 的内容，不要补充 observation 里没有的数据。

                【用户分析目标】
                %s

                【重要原则】
                1. 先阅读当前 sheet 的真实内容，再判断它属于哪些分析维度。
                2. 优先判断当前 Sheet 是否能回答用户分析目标；不能回答时明确说明数据不足。
                3. 下方“分析维度”只是帮助你分类和提炼，不是要求你逐项填空。
                4. 如果当前 sheet 没有提供某个维度的数据，不要强行输出。
                5. 不要编造数字、趋势、品牌、关键词、法规、结论。
                6. 可以基于当前 sheet 中的数字、文本、排名、比例、趋势做合理业务解读，但要能从当前 sheet 内容推导出来。
                7. 不输出单元格位置、来源说明、行号引用。
                %s

                【分析维度参考】
                %s
                【输出格式】
                ### Sheet 定位
                - 数据类型：
                - 命中的分析维度：
                - 这个 sheet 对最终选品报告的价值：

                ### 当前数据事实
                列出当前 sheet 里能直接看到或明确推导的关键事实。

                ### 业务解读
                基于“当前数据事实”解释它说明了什么。

                ### 机会判断
                如果当前 sheet 能支持机会判断，说明机会在哪里；否则写“当前 sheet 不足以判断机会”。

                ### 风险判断
                如果当前 sheet 能支持风险判断，说明风险是什么；否则写“当前 sheet 未提供明显风险信号”。

                ### 可进入最终报告的内容
                用 3-8 条精炼 bullet 输出可以被最终报告直接吸收的结论。

                【当前 Sheet】
                Sheet 名称：%s

                Sheet observation：
                %s
                """.formatted(analysisGoal(userAnalysisGoal), keywordsRules(sheetName), REPORT_DIMENSIONS,
                defaultText(sheetName), defaultText(sheetObservation));
    }

    public static String buildFinalSummaryPrompt(String fileName, int sheetCount, String sheetSummaryMarkdown) {
        return buildFinalSummaryPrompt(fileName, sheetCount, sheetSummaryMarkdown, "");
    }

    public static String buildFinalSummaryPrompt(String fileName, int sheetCount, String sheetSummaryMarkdown,
            String userAnalysisGoal) {
        return """
                请只基于下面的 Sheet 富摘要生成最终 Markdown 报告。
                不要调用工具，不要编造 Sheet 富摘要中不存在的数据，不要输出来源、单元格、行号或 sheet 引用。

                【用户分析目标】
                %s

                文件名：%s
                共 %d 个 sheet

                【报告结构】
                1. 核心结论
                2. 市场现状
                3. 需求与关键词
                4. 竞争格局
                5. 退货与评论风险
                6. 定价与产品策略
                7. 合规与政策风险
                8. 数据缺口与下一步验证

                【写作规则】
                - 报告开头先直接回答用户分析目标；如果用户没有补充目标，则先给出通用核心结论。
                - 优先使用 Sheet 富摘要中的事实、数字、趋势、品牌、关键词、价格带和用户反馈。
                - 如果某个章节没有足够数据，明确写“当前数据不足，需要补充验证”，不要硬编。
                - 合规与政策风险只有在富摘要中出现明确合规信号时写成数据结论；否则只能写成“建议确认/需检查”的风险提示。
                - 最终结论要给出是否值得进入、为什么、主要门槛、建议打法。
                - Keywords 只用于评估宣传获客成本信号、竞争和投放难度；缺少曝光、点击、转化和实际花费时，禁止推算预算、ACOS、ROI。

                【Sheet 富摘要】
                %s
                """.formatted(analysisGoal(userAnalysisGoal), defaultText(fileName), sheetCount,
                defaultText(sheetSummaryMarkdown));
    }

    public static String buildFinalEvidenceSummaryPrompt(
            String fileName,
            int sheetCount,
            String evidenceMarkdown,
            String userAnalysisGoal) {
        return """
                请结合下面的完整十二表 evidence，以及同一会话中已经完成的 SCREENING 和 DEEP_DIVE 阶段结论，
                只执行一次最终综合分析。不要重新逐 Sheet 输出分析过程，不要调用工具，不要编造 evidence 中不存在的数据。

                【用户分析目标】
                %s

                文件名：%s
                共 %d 个 sheet

                【最终报告结构】
                1. 是否值得进入及核心依据
                2. 市场容量、销售和需求趋势
                3. 细分机会、退货风险和商品集中度
                4. 品牌竞争格局
                5. 评价与 VOC 反映的用户需求和产品风险
                6. Keywords 反映的宣传获客成本信号、竞争强度和投放难度
                7. 所选 ASIN 的销量、价格、BSR、评分和卖家竞争趋势
                8. 进入策略、主要门槛和下一步验证

                【强制边界】
                - 最终判断必须能由十二表 evidence 或同一会话中的阶段结论支持。
                - ASIN 销售与运营趋势用于判断所选商品的规模、增长稳定性、价格策略和竞争变化，不能外推为全市场表现。
                - Keywords 的 PPC 竞价只能作为宣传成本与竞争难度参考。
                - 缺少曝光、点击、转化和实际花费时，禁止推算广告预算、ACOS、ROI 或确定性投放回报。
                - 数据不足时明确写出缺口，不用行业常识补数。

                【完整十二表 evidence】
                %s
                """.formatted(
                analysisGoal(userAnalysisGoal),
                defaultText(fileName),
                sheetCount,
                defaultText(evidenceMarkdown));
    }

    public static String buildScreeningSheetPrompt(
            String sheetName, String sheetObservation, String userAnalysisGoal) {
        return """
                你是亚马逊市场初筛分析师，只分析当前阶段一证据表对市场进入和候选商品选择的价值。
                不调用工具，不使用其他证据表，不补充当前 observation 没有的数据。

                【用户分析目标】
                %s

                【阶段边界】
                阶段一只负责市场规模、销售/需求趋势、细分机会、竞争结构、商品集中度、品牌和市场风险。
                评论、VOC、Keywords 和选中 ASIN 的经营趋势属于阶段二，尚未采集不应写成阶段一缺陷。

                【输出格式】
                ### 证据表定位
                - 数据类型：
                - 支持的初筛判断：
                - 对候选商品选择的价值：

                ### 当前事实
                只列出当前证据表可见或可直接推导的数字、排名、比例和趋势。

                ### 初筛解读
                说明这些事实对市场进入、竞争门槛或商品筛选意味着什么。

                ### 可进入阶段一总结的结论
                输出 3-8 条精炼结论；当前证据表无法支持的维度不要补写。

                证据表名称：%s
                证据表 observation：
                %s
                """.formatted(
                analysisGoal(userAnalysisGoal), defaultText(sheetName), defaultText(sheetObservation));
    }

    public static String buildDeepDiveSheetPrompt(
            String sheetName, String sheetObservation, String userAnalysisGoal) {
        return """
                你是亚马逊商品深挖分析师，只分析当前阶段二 Sheet 对人工选中 ASIN 的价值。
                不调用工具，不使用其他 Sheet，不补充当前 observation 没有的数据。

                【用户分析目标】
                %s

                【阶段边界】
                当前阶段只讨论评价/VOC、Keywords 宣传获客成本信号、所选 ASIN 销售趋势和经营趋势。
                Keywords 的竞价只能代表竞争和投放难度参考，不得推算实际广告预算、ACOS 或 ROI。
                评论设置了星级筛选时属于定向抽样，只能分析所选星级中的主题，不得外推总体差评率、平均星级或满意度。

                【输出格式】
                ### Sheet 定位
                - 数据类型：
                - 支持的深挖问题：
                - 对产品策略的价值：

                ### 当前事实
                只列出当前 Sheet 可见或可直接推导的事实。

                ### 深挖解读
                说明用户需求、产品缺陷、获客难度或经营稳定性信号。

                ### 可进入阶段二总结的结论
                输出 3-8 条精炼结论；当前 Sheet 无法支持的维度不要补写。

                Sheet 名称：%s
                Sheet observation：
                %s
                """.formatted(
                analysisGoal(userAnalysisGoal), defaultText(sheetName), defaultText(sheetObservation));
    }

    public static String buildScreeningSummaryPrompt(
            String analysisRunId,
            String fileName,
            int sheetCount,
            String sheetSummaryMarkdown,
            String rawFieldCatalog,
            String userAnalysisGoal) {
        return """
                你是阶段一 ScreeningAnalysisAgent。请基于七张阶段一证据表的分析摘要，判断市场是否值得进入，并帮助用户从默认 Top20 候选商品中选择进入阶段二的 ASIN。
                可以按需调用原始数据工具核查字段目录中尚未被证据映射的关键字段，但不要读取与当前阶段无关的数据。

                【分析运行 ID】%s
                【用户分析目标】%s

                %s

                【阶段一报告结构】
                - 评分速览后严格按以下顺序使用七个二级标题，标题只写真实表名，不加数字编号、序号或“Sheet”字样：
                  `## US`
                  `## 行业销售趋势`
                  `## 行业需求及趋势`
                  `## 细分市场现状`
                  `## 细分市场退货率`
                  `## 竞品品牌`
                  `## 商品集中度`
                - 每章直接写 1-3 条结论、风险或建议，不再设置“当前事实”“数据事实”等数据罗列小节。
                - 除综合评分和维度评分外，正文不要列数字、排名、比例、逐月数据或 Markdown 数据表；完整数据由证据数据模块承载。
                - 行业销售趋势和行业需求及趋势只解释系统通过 SSE 展示的 Mermaid 趋势图，不在正文重复图中数值。
                - 用户可见报告中禁止出现 `Sheet 一`、`Sheet一`、`Sheet 1`、`第一个 Sheet` 或其他工作簿内部编号。
                - `## 行业销售趋势` 标题后必须调用 `generateResearchReportChart`，参数 `sectionCode` 使用 `market-sales-trend`，把返回的 Mermaid Markdown 原样放在结论前。
                - `## 行业需求及趋势` 标题后必须调用同一工具，参数 `sectionCode` 使用 `market-demand-trend`，把返回内容原样放在结论前。

                【强制边界】
                - 只分析阶段一已有的七张证据表和当前阶段允许查询的原始数据。
                - 不要求评论、VOC、Keywords 或选中 ASIN 深挖数据已经存在。
                - 所有数字、趋势和商品建议必须能由当前输入支持；不确定时标记为待核查。
                - Mermaid 只能来自 `generateResearchReportChart` 的工具结果；不得自行编写、修改或补充图表源码和图表数值。

                【七张证据表分析摘要】
                %s

                【阶段一字段目录】
                %s
                """.formatted(
                defaultText(analysisRunId), analysisGoal(userAnalysisGoal),
                scorecardInstructions("阶段一初筛", SCREENING_SCORE_DIMENSIONS),
                defaultText(sheetSummaryMarkdown), defaultText(rawFieldCatalog));
    }

    public static String buildDeepDiveSummaryPrompt(
            String analysisRunId,
            String fileName,
            int sheetCount,
            String sheetSummaryMarkdown,
            String rawFieldCatalog,
            String userAnalysisGoal) {
        return """
                你是阶段二 DeepDiveAnalysisAgent。请基于五张阶段二证据表的 Sheet 富摘要，分析人工选中 ASIN 的用户反馈、宣传获客成本信号和经营趋势。
                可以按需调用原始数据工具核查字段目录，但不要读取阶段一之外的未授权数据，也不要把 PPC 竞价当作实际广告花费。

                【分析运行 ID】%s
                【用户分析目标】%s

                %s

                【阶段二报告结构】
                1. 用户需求与 VOC 共性
                2. 高频差评、产品缺陷和退货风险
                3. Keywords 的宣传获客成本信号、竞争强度和投放难度
                4. 所选 ASIN 的销量、价格、BSR、评分和卖家竞争变化
                5. 产品改进、进入策略和需要交给最终决策的结论
                - 分析 Keywords 时必须调用 `generateResearchReportChart`，参数 `sectionCode` 使用 `keywords`，把返回的 Mermaid Markdown 原样放在关键词结论前。

                【强制边界】
                - 只分析当前五张证据表和当前阶段允许查询的原始数据。
                - Keywords 缺少曝光、点击、转化和实际花费时，禁止推算预算、ACOS、ROI。
                - 所选 ASIN 趋势用于判断样本商品，不能外推为全市场表现。
                - 必须读取任务采集上下文中的评论星级/类型筛选；定向星级样本不得外推总体差评率、平均星级或满意度。
                - 不编造证据中不存在的数字或用户反馈。
                - Mermaid 只能来自 `generateResearchReportChart` 的工具结果；不得自行编写、修改或补充图表源码和图表数值。

                【五张证据表富摘要】
                %s

                【阶段二字段目录】
                %s
                """.formatted(
                defaultText(analysisRunId), analysisGoal(userAnalysisGoal),
                scorecardInstructions("阶段二深挖", DEEP_DIVE_SCORE_DIMENSIONS),
                defaultText(sheetSummaryMarkdown), defaultText(rawFieldCatalog));
    }

    public static String buildFinalDecisionPrompt(
            String analysisRunId,
            String fileName,
            int sheetCount,
            String evidenceMarkdown,
            String rawFieldCatalog,
            String userAnalysisGoal) {
        return """
                你是阶段三 FinalDecisionAgent。请结合完整十二张证据表，以及同一会话中已经完成的阶段一和阶段二结论，生成一次最终市场进入决策。
                不重新逐 Sheet 输出分析过程；只有当字段目录显示某个原始字段可能改变结论时，才调用只读原始数据工具核查。

                【分析运行 ID】%s
                【用户分析目标】%s

                %s

                【最终报告结构】
                以下为破坏性新契约，旧的七段式报告不再兼容：
                - 评分速览之后必须严格按以下顺序输出十二个二级标题，标题文字不得改名、合并、拆分或遗漏：
                  `## 1. US`
                  `## 2. 行业销售趋势`
                  `## 3. 行业需求及趋势`
                  `## 4. 细分市场现状`
                  `## 5. 细分市场退货率`
                  `## 6. 竞品品牌`
                  `## 7. 商品集中度`
                  `## 8. 评价`
                  `## 9. VOC`
                  `## 10. Keywords`
                  `## 11. ASIN销售趋势`
                  `## 12. ASIN运营趋势`
                - 每章标题后第一行固定写成：`**章节评分：{0-100 的整数/100 或 证据不足}｜判断：{一句话}｜置信度：{高/中/低}**`。
                - 每章只保留 `### 核心结论`、`### 主要风险`、`### 决策建议` 三部分，每部分 1-3 条短句；不要复述证据表、不要输出数据清单或 Markdown 数据表。
                - 市场规模、销售和需求趋势相关章节只解释趋势图代表的方向、拐点、季节性和风险，不再逐月列数；行业销售趋势必须围绕销量图，不用销售额替代销量。
                - 除综合评分、维度评分和章节评分外，正文不要列数字、排名、比例或逐月明细；所有证据数字留在证据数据模块和 Mermaid 图中。
                - Keywords 章必须综合所有已选择 ASIN 的“竞品反查词”，优先说明跨 ASIN 覆盖频率最高的关键词、共同卖点、竞争意图和差异化机会；同一 ASIN 的同一规范化关键词只能计一次。
                - `## 2. 行业销售趋势` 的章节评分后，必须调用 `generateResearchReportChart`，参数 `sectionCode` 使用 `market-sales-trend`，并把工具返回的 Mermaid Markdown 原样放在 `### 核心结论` 前。
                - `## 3. 行业需求及趋势` 的章节评分后，必须调用同一工具，参数 `sectionCode` 使用 `market-demand-trend`，并把返回内容原样放在 `### 核心结论` 前。
                - `## 10. Keywords` 的章节评分后，必须调用同一工具，参数 `sectionCode` 使用 `keywords`，并把返回内容原样放在 `### 核心结论` 前。

                【强制边界】
                - 最终判断必须能由十二张证据表、阶段结论或工具结果支持。
                - Mermaid 只能来自 `generateResearchReportChart` 的工具结果；不得自行编写、修改或补充图表源码和图表数值。
                - Keywords 竞价不能直接等同实际广告成本；缺少实际花费和转化时禁止推算预算、ACOS、ROI。
                - 所选 ASIN 趋势不能外推为全市场表现。
                - 评论设置了星级或类型筛选时必须按该抽样口径解释，不能把定向样本外推为评论总体。
                - 数据不足时说明具体缺口和验证动作，不用行业常识补数。

                【完整十二张证据表】
                %s

                【可查询原始字段目录】
                %s
                """.formatted(
                defaultText(analysisRunId), analysisGoal(userAnalysisGoal),
                scorecardInstructions("最终决策", FINAL_SCORE_DIMENSIONS),
                defaultText(evidenceMarkdown), defaultText(rawFieldCatalog));
    }

    private static String scorecardInstructions(String stageName, String scoreDimensions) {
        return """
                【%s评分速览输出契约】
                - 最终正文的第一个非空内容必须是二级标题 `## %s评分速览`，前面不要写引言、结论或说明。
                - 下一行固定写成 `**综合评分：{0-100 的整数/100 或 暂不可评}｜推进建议：{值得推进/有条件推进/谨慎验证/暂不推进/补充证据}｜置信度：{高/中/低}**`。
                - 随后输出且只输出一张 `维度 | 评分 | 关键依据` Markdown 表，按下列顺序逐项评分：
                %s
                - 所有数字分必须在 0-100 之间且只能取 5 的倍数，分数越高表示越支持继续推进；每个数字分必须有当前证据支持的一句关键依据。
                - 综合分是所有可评分维度的等权平均，并四舍五入到最近的 5 分；80-100 为“值得推进”，65-79 为“有条件推进”，50-64 为“谨慎验证”，0-49 为“暂不推进”。
                - 某维度缺少证据时，评分写“证据不足”，关键依据写明具体缺口，并从综合分中排除；可评分维度少于本阶段维度总数的 80%% 时，综合评分写“暂不可评”，推进建议写“补充证据”。
                - 置信度用于表达证据覆盖度和一致性，不得混入机会分数；存在关键缺口或证据冲突时必须降低置信度并在表后列出“优先补充证据”。
                - 不得因下一阶段的数据尚未采集而降低当前阶段评分或置信度，也不得用行业常识、固定跨类目阈值或主观猜测补分。
                - 表格后固定写：`> 评分仅代表当前证据下的决策支持度，不是成功率、利润率、ROI 或收益承诺。`
                - 完成评分速览后，再按本阶段报告结构输出详细分析。
                """.formatted(stageName, stageName, scoreDimensions.stripTrailing());
    }

    private static String analysisGoal(String userAnalysisGoal) {
        return userAnalysisGoal == null || userAnalysisGoal.isBlank()
                ? "用户没有补充具体问题，请完成通用亚马逊选品数据分析。"
                : userAnalysisGoal.trim();
    }

    private static String keywordsRules(String sheetName) {
        return "Keywords".equalsIgnoreCase(defaultText(sheetName).trim())
                ? KEYWORDS_ANALYSIS_RULES
                : "";
    }

    private static String defaultText(String value) {
        return value == null ? "" : value;
    }
}
