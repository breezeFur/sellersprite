package cyou.yuanbaomao.sellersprite.research.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ResearchReportChartImageRendererTest {

    @Test
    void shouldRenderTenLongKeywordsAsTallHorizontalBars() throws Exception {
        List<String> keywords = java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(index -> "long countertop nugget ice maker keyword " + index)
                .toList();
        ResearchReportChart chart = new ResearchReportChart(
                "competitor-keyword-frequency",
                "keywords",
                "Keywords",
                ResearchReportChart.TYPE_HORIZONTAL_BAR,
                "所选竞品 ASIN 高频关键词",
                keywords,
                List.of(new ResearchReportChart.Series(
                        "覆盖竞品 ASIN 数",
                        java.util.stream.IntStream.rangeClosed(1, 10)
                                .mapToObj(index -> BigDecimal.valueOf(index % 3 + 1L))
                                .toList())),
                "个 ASIN",
                "按证据表生成。");

        BufferedImage image = decode(new ResearchReportChartImageRenderer().renderDataUri(chart));

        assertThat(image.getWidth()).isEqualTo(960);
        assertThat(image.getHeight()).isEqualTo(604);
        assertThat(new Color(image.getRGB(500, 75))).isNotEqualTo(Color.WHITE);
    }

    private BufferedImage decode(String dataUri) throws Exception {
        byte[] png = Base64.getDecoder().decode(dataUri.substring(dataUri.indexOf(',') + 1));
        return ImageIO.read(new ByteArrayInputStream(png));
    }
}
