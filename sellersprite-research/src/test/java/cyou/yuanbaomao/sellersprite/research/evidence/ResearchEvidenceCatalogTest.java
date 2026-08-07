package cyou.yuanbaomao.sellersprite.research.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ResearchEvidenceCatalogTest {

    @Test
    void shouldExposeExactSevenPlusFiveStageContract() {
        assertThat(ResearchEvidenceCatalog.definitions(EvidenceStage.SCREENING))
                .hasSize(7)
                .allMatch(definition -> definition.stage() == EvidenceStage.SCREENING)
                .extracting(ResearchEvidenceCatalog.Definition::sheetName)
                .containsExactly(
                        "US",
                        "行业销售趋势",
                        "行业需求及趋势",
                        "细分市场现状",
                        "细分市场退货率",
                        "竞品品牌",
                        "商品集中度");

        assertThat(ResearchEvidenceCatalog.definitions(EvidenceStage.DEEP_DIVE))
                .hasSize(5)
                .allMatch(definition -> definition.stage() == EvidenceStage.DEEP_DIVE)
                .extracting(ResearchEvidenceCatalog.Definition::sheetName)
                .containsExactly("评价", "VOC", "Keywords", "ASIN销售趋势", "ASIN运营趋势");

        assertThat(ResearchEvidenceCatalog.DEFINITIONS)
                .containsExactlyElementsOf(List.of(
                        ResearchEvidenceCatalog.SCREENING_DEFINITIONS,
                        ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS)
                        .stream()
                        .flatMap(List::stream)
                        .toList());
    }
}
