package cyou.yuanbaomao.sellersprite.research.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ResearchReportChartServiceTest {

    private final ResearchDatasetService datasetService = org.mockito.Mockito.mock(ResearchDatasetService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResearchStageInputService stageInputService = org.mockito.Mockito.mock(ResearchStageInputService.class);
    private final ResearchReportChartService service = new ResearchReportChartService(datasetService, stageInputService);

    @Test
    void shouldBuildSalesDemandAndDeduplicatedCompetitorKeywordCharts() throws Exception {
        when(datasetService.readEvidenceDatasets("job-1")).thenReturn(List.of(
                dataset("evidence.market-sales-trend", """
                        {"items":[
                          {"月份":"2026-01","样本总月销量":"1,200"},
                          {"月份":"2026-02","样本总月销量":1500}
                        ]}
                        """),
                dataset("evidence.market-demand-trend", """
                        {"items":[
                          {"月份":"2026-01","浏览量/搜索量":3000},
                          {"月份":"2026-01","浏览量/搜索量":500},
                          {"月份":"2026-02","浏览量/搜索量":4000}
                        ]}
                        """),
                dataset("evidence.keywords", """
                        {"items":[
                          {"来源类型":"竞品反查词","关联ASIN":"B001","关键词":"Travel  Mug"},
                          {"来源类型":"竞品反查词","关联ASIN":"B001","关键词":"travel mug"},
                          {"来源类型":"竞品反查词","关联ASIN":"B002","关键词":"TRAVEL MUG!"},
                          {"来源类型":"竞品反查词","关联ASIN":"B001","关键词":"steel cup"},
                          {"来源类型":"关键词研究","关联ASIN":"","关键词":"travel mug"}
                        ]}
                        """)));

        List<ResearchReportChart> charts = service.buildCharts("job-1");

        assertThat(charts).extracting(ResearchReportChart::chartCode)
                .containsExactly("market-sales-volume", "market-demand-volume", "competitor-keyword-frequency");
        assertThat(charts.get(0).series().getFirst().values())
                .containsExactly(new BigDecimal("1200"), new BigDecimal("1500"));
        assertThat(charts.get(1).series().getFirst().values())
                .containsExactly(new BigDecimal("3500"), new BigDecimal("4000"));
        ResearchReportChart keywords = charts.get(2);
        assertThat(keywords.type()).isEqualTo(ResearchReportChart.TYPE_HORIZONTAL_BAR);
        assertThat(keywords.categories()).containsExactly("travel mug", "steel cup");
        assertThat(keywords.series().getFirst().values())
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ONE);
        assertThat(charts.getFirst().mermaidMarkdown())
                .startsWith("```mermaid\nxychart-beta")
                .contains("title \"行业月销量趋势\"")
                .contains("x-axis [\"2026-01\", \"2026-02\"]")
                .contains("y-axis \"件\" 0 --> 1500")
                .contains("line [1200, 1500]");
        assertThat(keywords.mermaidMarkdown())
                .startsWith("```mermaid\nxychart-beta horizontal")
                .contains("bar [2, 1]");
    }

    @Test
    void shouldLimitKeywordChartAndExplainMissingCrossAsinKeywords() throws Exception {
        String keywordItems = IntStream.rangeClosed(1, 12)
                .mapToObj(index -> """
                        {"来源类型":"竞品反查词","关联ASIN":"%s","关键词":"long competitor keyword %02d"}
                        """.formatted(index % 2 == 0 ? "B001" : "B002", index).trim())
                .collect(Collectors.joining(","));
        when(datasetService.readEvidenceDatasets("job-2")).thenReturn(List.of(
                dataset("evidence.keywords", "{\"items\":[" + keywordItems + "]}")));

        ResearchReportChart keywords = service.buildCharts("job-2").getFirst();

        assertThat(keywords.categories()).hasSize(10);
        assertThat(keywords.title()).contains("未形成跨 ASIN 高频共词");
        assertThat(keywords.methodology()).contains("未形成覆盖两个及以上 ASIN 的高频共词");
        assertThat(keywords.mermaidMarkdown()).contains("xychart-beta horizontal");
    }

    private ResearchDataset dataset(String code, String payload) throws Exception {
        return new ResearchDataset(code, code, objectMapper.readTree(payload), 1);
    }
}
