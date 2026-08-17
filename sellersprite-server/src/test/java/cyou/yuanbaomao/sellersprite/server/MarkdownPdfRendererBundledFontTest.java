package cyou.yuanbaomao.sellersprite.server;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.report.MarkdownPdfRenderer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownPdfRendererBundledFontTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldRenderChinesePdfWithBundledFont() throws Exception {
        MarkdownPdfRenderer renderer = new MarkdownPdfRenderer(new ResearchProperties());
        Path target = temporaryDirectory.resolve("bundled-font.pdf");

        renderer.render("# 内置中文字体\n\n市场分析报告", target);

        assertThat(Files.size(target)).isGreaterThan(1_000L);
        try (PDDocument document = PDDocument.load(target.toFile())) {
            assertThat(new PDFTextStripper().getText(document))
                    .contains("内置中文字体", "市场分析报告");
        }
    }
}
