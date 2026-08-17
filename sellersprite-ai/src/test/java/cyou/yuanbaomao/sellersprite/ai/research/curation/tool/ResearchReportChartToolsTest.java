package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import cyou.yuanbaomao.sellersprite.research.report.ResearchReportChart;
import cyou.yuanbaomao.sellersprite.research.report.ResearchReportChartPort;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResearchReportChartToolsTest {

    @Test
    void shouldReturnChapterMermaidMarkdownFromCurrentAnalysisJob() {
        AmazonSelectionToolContext toolContext = mock(AmazonSelectionToolContext.class);
        ResearchReportChartPort chartPort = mock(ResearchReportChartPort.class);
        ResearchReportChartTools tools = new ResearchReportChartTools(toolContext, chartPort);
        when(toolContext.getRequiredRawDataScope("run-1"))
                .thenReturn(new ResearchRawDataAccessScope("job-1", ResearchStageCode.FINAL_ANALYSIS));
        when(chartPort.buildCharts("job-1")).thenReturn(List.of(new ResearchReportChart(
                "market-sales-volume",
                "market-sales-trend",
                "行业销售趋势",
                "LINE",
                "行业月销量趋势",
                List.of("2026-01", "2026-02"),
                List.of(new ResearchReportChart.Series(
                        "销量", List.of(BigDecimal.valueOf(1200), BigDecimal.valueOf(1500)))),
                "件",
                "按证据表生成。")));

        String markdown = tools.generateResearchReportChart("run-1", "market-sales-trend");

        assertThat(markdown)
                .startsWith("```mermaid\nxychart-beta")
                .contains("title \"行业月销量趋势\"")
                .contains("line [1200, 1500]")
                .endsWith("```");
    }
}
