package cyou.yuanbaomao.sellersprite.research.report;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownPdfRendererTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldRenderChineseMarkdownTableAsReadablePdf() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.setPdfFontPath(findWindowsCjkFont());
        MarkdownPdfRenderer renderer = new MarkdownPdfRenderer(properties);
        Path target = outputPath();

        renderer.render("""
                ## 阶段一初筛评分速览
                **综合评分：70/100｜推进建议：有条件推进｜置信度：中**

                | 维度 | 评分 | 关键依据 |
                | --- | --- | --- |
                | 市场需求吸引力 | 75 | 搜索与购买需求保持稳定，样本销量具备验证价值。 |
                | 增长与季节稳定性 | 65 | 近期增长温和，但仍需覆盖完整年度验证季节性。 |
                | 竞争可进入性 | 60 | 头部集中度中等，新品仍有进入窗口。 |
                | 利润与退货安全性 | 70 | 毛利空间可接受，退货原因需要继续核查。 |
                | 候选商品机会 | 80 | 多个候选商品具备差异化功能和价格带机会。 |

                > 评分仅代表当前证据下的决策支持度，不是成功率、利润率、ROI 或收益承诺。
                """, target);

        byte[] pdf = Files.readAllBytes(target);
        assertThat(new String(pdf, 0, 8, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
        assertThat(pdf).contains((byte) '%');
        assertThat(Files.size(target)).isGreaterThan(1_000L);
        try (PDDocument document = PDDocument.load(target.toFile())) {
            assertThat(new PDFTextStripper().getText(document))
                    .contains("阶段一初筛评分速览", "有条件推进", "候选商品机会");
        }
    }

    @Test
    void shouldEmbedReportChartFromStructuredSpecification() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.setPdfFontPath(findWindowsCjkFont());
        MarkdownPdfRenderer renderer = new MarkdownPdfRenderer(properties);
        Path target = temporaryDirectory.resolve("report-with-chart.pdf");
        ResearchReportChart chart = new ResearchReportChart(
                "market-sales-volume", "market-sales-trend", "行业销售趋势", "LINE",
                "行业月销量趋势", List.of("2026-01", "2026-02"),
                List.of(new ResearchReportChart.Series(
                        "销量", List.of(BigDecimal.valueOf(1200), BigDecimal.valueOf(1600)))),
                "件", "按证据表生成。");

        renderer.render("""
                ## 2. 行业销售趋势

                ```mermaid
                xychart-beta
                    line [1200, 1600]
                ```

                销量正在增长。
                """, List.of(chart), target);

        assertThat(Files.size(target)).isGreaterThan(5_000L);
        try (PDDocument document = PDDocument.load(target.toFile())) {
            assertThat(new PDFTextStripper().getText(document))
                    .contains("行业销售趋势", "按证据表生成")
                    .doesNotContain("xychart-beta", "line [1200, 1600]");
        }
    }

    @Test
    void shouldEmbedLongKeywordChartAsTallHorizontalImage() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.setPdfFontPath(findWindowsCjkFont());
        MarkdownPdfRenderer renderer = new MarkdownPdfRenderer(properties);
        Path target = keywordOutputPath();
        List<String> keywords = java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(index -> "long countertop nugget ice maker keyword " + index)
                .toList();
        ResearchReportChart chart = new ResearchReportChart(
                "competitor-keyword-frequency", "keywords", "Keywords",
                ResearchReportChart.TYPE_HORIZONTAL_BAR,
                "所选竞品 ASIN 高频关键词", keywords,
                List.of(new ResearchReportChart.Series(
                        "覆盖竞品 ASIN 数", keywords.stream().map(ignored -> BigDecimal.ONE).toList())),
                "个 ASIN", "仅统计所选竞品的规范化关键词。");

        renderer.render("""
                ## 10. Keywords

                ```mermaid
                xychart-beta horizontal
                    bar [1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
                ```

                当前关键词覆盖频率相同。
                """, List.of(chart), target);

        assertThat(Files.size(target)).isGreaterThan(10_000L);
        try (PDDocument document = PDDocument.load(target.toFile())) {
            assertThat(new PDFTextStripper().getText(document))
                    .contains("Keywords", "当前关键词覆盖频率相同", "仅统计所选竞品的规范化关键词")
                    .doesNotContain("xychart-beta");
            List<PDImageXObject> images = new ArrayList<>();
            for (var name : document.getPage(0).getResources().getXObjectNames()) {
                if (document.getPage(0).getResources().getXObject(name) instanceof PDImageXObject image) {
                    images.add(image);
                }
            }
            assertThat(images).anySatisfy(image -> {
                assertThat(image.getWidth()).isEqualTo(960);
                assertThat(image.getHeight()).isEqualTo(604);
            });
        }
    }

    private String findWindowsCjkFont() {
        Path font = Path.of("C:/Windows/Fonts/simhei.ttf");
        return Files.isRegularFile(font) ? font.toString() : null;
    }

    private Path outputPath() throws Exception {
        String configuredOutput = System.getProperty("pdf.test.output");
        if (configuredOutput == null || configuredOutput.isBlank()) {
            return temporaryDirectory.resolve("market-research.pdf");
        }
        Path output = Path.of(configuredOutput).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        return output;
    }

    private Path keywordOutputPath() throws Exception {
        String configuredOutput = System.getProperty("pdf.keyword.test.output");
        if (configuredOutput == null || configuredOutput.isBlank()) {
            return temporaryDirectory.resolve("keyword-horizontal-chart.pdf");
        }
        Path output = Path.of(configuredOutput).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        return output;
    }
}
