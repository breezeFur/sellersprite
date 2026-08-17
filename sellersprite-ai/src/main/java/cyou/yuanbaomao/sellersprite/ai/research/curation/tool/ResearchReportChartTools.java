package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import cyou.yuanbaomao.sellersprite.research.report.ResearchReportChartPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** 为最终报告生成可直接插入正文的 Mermaid Markdown 图表。 */
@Component
@RequiredArgsConstructor
public class ResearchReportChartTools {

    private final AmazonSelectionToolContext toolContext;
    private final ResearchReportChartPort reportChartPort;

    @Tool(
            name = "generateResearchReportChart",
            description = "根据已持久化证据生成指定报告章节的 Mermaid Markdown；返回内容必须原样插入对应章节")
    public String generateResearchReportChart(
            @ToolParam(description = "提示词中提供的分析运行 ID") String analysisRunId,
            @ToolParam(description = "章节代码：market-sales-trend、market-demand-trend 或 keywords")
                    String sectionCode) {
        ResearchRawDataAccessScope scope = toolContext.getRequiredRawDataScope(analysisRunId);
        return reportChartPort.buildCharts(scope.jobId()).stream()
                .filter(chart -> chart.sectionCode().equals(sectionCode))
                .map(chart -> chart.mermaidMarkdown().stripTrailing())
                .findFirst()
                .orElse("");
    }
}
