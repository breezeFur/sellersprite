package cyou.yuanbaomao.sellersprite.research.report;

import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 将模型输出的 Markdown 渲染为可下载的中文 PDF。 */
@Component
public class MarkdownPdfRenderer {

    private static final String FONT_FAMILY = "Research CJK";
    private static final String BUNDLED_FONT_RESOURCE = "/fonts/NotoSansSC-VF.ttf";
    private static final String HTML_BASE_URI = "file:///";
    private static final List<Extension> MARKDOWN_EXTENSIONS = List.of(
            TablesExtension.create(), StrikethroughExtension.create());
    private static final Pattern MERMAID_BLOCK = Pattern.compile(
            "(?s)```mermaid\\s*.*?```\\s*");
    private final Path configuredFontPath;

    public MarkdownPdfRenderer(ResearchProperties properties) {
        this.configuredFontPath = resolveConfiguredFontPath(properties.getPdfFontPath());
    }

    public void render(String markdown, Path target) throws Exception {
        render(markdown, List.of(), target);
    }

    public void render(String markdown, List<ResearchReportChart> charts, Path target) throws Exception {
        if (!StringUtils.hasText(markdown)) {
            throw new IllegalArgumentException("AI分析报告内容不能为空");
        }
        Path normalizedTarget = target.toAbsolutePath().normalize();
        Files.createDirectories(normalizedTarget.getParent());
        Parser parser = Parser.builder().extensions(MARKDOWN_EXTENSIONS).build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(MARKDOWN_EXTENSIONS)
                .escapeHtml(true)
                .build();
        String pdfMarkdown = charts.isEmpty() ? markdown : MERMAID_BLOCK.matcher(markdown).replaceAll("");
        String body = renderer.render(parser.parse(pdfMarkdown));
        String html = document(insertCharts(body, charts));
        try (OutputStream outputStream = Files.newOutputStream(normalizedTarget)) {
            PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
            pdfBuilder.useFastMode();
            registerFont(pdfBuilder);
            pdfBuilder
                    .withHtmlContent(html, HTML_BASE_URI)
                    .toStream(outputStream)
                    .run();
        }
    }

    private String insertCharts(String body, List<ResearchReportChart> charts) {
        ResearchReportChartImageRenderer chartRenderer = new ResearchReportChartImageRenderer();
        String enriched = body;
        for (ResearchReportChart chart : charts) {
            String heading = "<h2>" + sectionOrder(chart.sectionCode()) + ". "
                    + chart.sectionTitle() + "</h2>";
            String figure = "<figure class=\"report-chart\"><img alt=\"" + chart.title()
                    + "\" src=\"" + chartRenderer.renderDataUri(chart) + "\"/>"
                    + "<figcaption>" + chart.methodology() + "</figcaption></figure>";
            enriched = enriched.replace(heading, heading + figure);
        }
        return enriched;
    }

    private int sectionOrder(String sectionCode) {
        return ResearchFinalReportSection.SECTIONS.stream()
                .filter(section -> section.code().equals(sectionCode))
                .map(ResearchFinalReportSection::order)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知最终报告章节: " + sectionCode));
    }

    private String document(String body) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <style>
                    @page {
                      size: A4;
                      margin: 18mm 16mm 18mm;
                      @bottom-right {
                        content: counter(page);
                        color: #64748b;
                        font-size: 9pt;
                      }
                    }
                    body {
                      color: #1e293b;
                      font-family: 'Research CJK';
                      font-size: 10.5pt;
                      line-height: 1.65;
                    }
                    h1, h2, h3, h4 {
                      color: #0f172a;
                      line-height: 1.3;
                      page-break-after: avoid;
                    }
                    h1 {
                      margin: 0 0 18pt;
                      padding-bottom: 8pt;
                      border-bottom: 1.5pt solid #0f766e;
                      font-size: 23pt;
                    }
                    h2 {
                      margin: 18pt 0 7pt;
                      padding-bottom: 3pt;
                      border-bottom: 0.6pt solid #cbd5e1;
                      font-size: 15pt;
                    }
                    h3 { margin: 13pt 0 5pt; font-size: 12.5pt; }
                    p { margin: 5pt 0; }
                    ul, ol { margin: 5pt 0 8pt; padding-left: 18pt; }
                    li { margin: 2pt 0; }
                    blockquote {
                      margin: 8pt 0;
                      padding: 5pt 10pt;
                      color: #475569;
                      border-left: 3pt solid #14b8a6;
                      background: #f0fdfa;
                    }
                    table {
                      width: 100%;
                      margin: 9pt 0 12pt;
                      border-collapse: collapse;
                      font-size: 9pt;
                      page-break-inside: auto;
                    }
                    tr { page-break-inside: avoid; page-break-after: auto; }
                    th, td {
                      padding: 5pt 6pt;
                      border: 0.5pt solid #cbd5e1;
                      vertical-align: top;
                    }
                    th { color: #0f172a; background: #e2e8f0; }
                    code {
                      padding: 1pt 3pt;
                      color: #0f766e;
                      background: #f1f5f9;
                    }
                    pre {
                      margin: 8pt 0;
                      padding: 8pt;
                      white-space: pre-wrap;
                      background: #f1f5f9;
                      border: 0.5pt solid #cbd5e1;
                    }
                    pre code { padding: 0; color: #334155; background: transparent; }
                    hr { border: 0; border-top: 0.6pt solid #cbd5e1; }
                    a { color: #0f766e; }
                    .report-chart {
                      margin: 9pt 0 14pt;
                      padding: 7pt;
                      border: 0.6pt solid #cbd5e1;
                      background: #ffffff;
                      page-break-inside: avoid;
                    }
                    .report-chart img { width: 100%; height: auto; }
                    .report-chart figcaption { margin-top: 4pt; color: #64748b; font-size: 8.5pt; }
                  </style>
                </head>
                <body>
                __REPORT_BODY__
                </body>
                </html>
                """.replace("__REPORT_BODY__", body);
    }

    private void registerFont(PdfRendererBuilder builder) {
        if (configuredFontPath != null) {
            builder.useFont(configuredFontPath.toFile(), FONT_FAMILY);
            return;
        }
        builder.useFont(this::openBundledFont, FONT_FAMILY);
    }

    private InputStream openBundledFont() {
        InputStream inputStream = MarkdownPdfRenderer.class.getResourceAsStream(BUNDLED_FONT_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException("JAR内置PDF中文字体不存在: " + BUNDLED_FONT_RESOURCE);
        }
        return inputStream;
    }

    private Path resolveConfiguredFontPath(String configuredPath) {
        if (!StringUtils.hasText(configuredPath)) {
            return null;
        }
        Path configured = Path.of(configuredPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(configured)) {
            throw new IllegalStateException("PDF中文字体文件不存在: " + configuredPath);
        }
        return configured;
    }
}
