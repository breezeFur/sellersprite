package cyou.yuanbaomao.sellersprite.ai.research.curation.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ImageAssetSourceType;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class DefaultEvidenceWorkbookAssemblerTest {

    private ObjectMapper objectMapper;
    private EvidenceWorkbookAssembler assembler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        assembler = new DefaultEvidenceWorkbookAssembler(objectMapper);
    }

    @Test
    void shouldAssembleAllEvidenceDatasetsInCatalogOrder() {
        ProductWorkbook workbook = assembler.assemble("job-1", completeDatasets());

        assertThat(workbook.getFileName()).isEqualTo("market-research-job-1.xlsx");
        assertThat(workbook.getSheets())
                .extracting(sheet -> sheet.getSheetName())
                .containsExactlyElementsOf(ResearchEvidenceCatalog.DEFINITIONS.stream()
                        .map(ResearchEvidenceCatalog.Definition::sheetName)
                        .toList());
        assertThat(workbook.getRawSheets()).hasSize(ResearchEvidenceCatalog.DEFINITIONS.size());

        var productSheet = workbook.getSheets().getFirst();
        assertThat(productSheet.getHeaders())
                .containsExactlyElementsOf(ResearchEvidenceCatalog.DEFINITIONS.getFirst().columns());
        assertThat(productSheet.getRows()).hasSize(1);
        assertThat(productSheet.getRawSheet().getRawMarkdown())
                .contains("Sheet: US")
                .contains("Row 1:")
                .doesNotContain("证据范围", "来源数据集", "数据局限");
        assertThat(productSheet.getRawSheet().getRawCells())
                .anyMatch(cell -> "A1".equals(cell.getCellAddress()))
                .anyMatch(cell -> cell.getRowIndex() == 1 && !cell.getValue().isBlank());
        assertThat(productSheet.getRawSheet().getImageAssets())
                .singleElement()
                .satisfies(asset -> {
                    assertThat(asset.getSourceType()).isEqualTo(ImageAssetSourceType.URL);
                    assertThat(asset.getReference()).isEqualTo("https://example.com/product.jpg");
                });
    }

    @Test
    void shouldAssembleSevenScreeningDatasetsInCatalogOrder() {
        ProductWorkbook workbook = assembler.assemble(
                "job-1", datasets(ResearchEvidenceCatalog.SCREENING_DEFINITIONS));

        assertThat(workbook.getSheets())
                .extracting(sheet -> sheet.getSheetName())
                .containsExactlyElementsOf(ResearchEvidenceCatalog.SCREENING_DEFINITIONS.stream()
                        .map(ResearchEvidenceCatalog.Definition::sheetName)
                        .toList());
        assertThat(workbook.getRawSheets())
                .hasSize(ResearchEvidenceCatalog.SCREENING_DEFINITIONS.size());
    }

    @Test
    void shouldAssembleThreeDeepDiveDatasetsInCatalogOrder() {
        ProductWorkbook workbook = assembler.assemble(
                "job-1", datasets(ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS));

        assertThat(workbook.getSheets())
                .extracting(sheet -> sheet.getSheetName())
                .containsExactlyElementsOf(ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS.stream()
                        .map(ResearchEvidenceCatalog.Definition::sheetName)
                        .toList());
        assertThat(workbook.getRawSheets())
                .hasSize(ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS.size());
    }

    @Test
    void shouldRejectMissingEvidenceDataset() {
        List<EvidenceDatasetPayload> incomplete = new ArrayList<>(completeDatasets());
        incomplete.removeLast();

        assertThatThrownBy(() -> assembler.assemble("job-1", incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("缺少证据数据集")
                .hasMessageContaining("evidence.asin-operation-trend");
    }

    @Test
    void shouldRejectChangedBusinessColumns() {
        List<EvidenceDatasetPayload> datasets = new ArrayList<>(completeDatasets());
        ResearchEvidenceCatalog.Definition definition = ResearchEvidenceCatalog.DEFINITIONS.getFirst();
        ObjectNode payload = payload(definition);
        ((ArrayNode) payload.get("columns")).set(0, objectMapper.getNodeFactory().textNode("非法列"));
        datasets.set(0, new EvidenceDatasetPayload(definition.datasetCode(), payload.toString()));

        assertThatThrownBy(() -> assembler.assemble("job-1", datasets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("业务列不匹配");
    }

    @Test
    void shouldIgnoreNonEvidenceDatasetsFromTheSameResearchJob() {
        List<EvidenceDatasetPayload> datasets = new ArrayList<>(completeDatasets());
        datasets.addFirst(new EvidenceDatasetPayload("raw.products", "not-json"));

        ProductWorkbook workbook = assembler.assemble("job-1", datasets);

        assertThat(workbook.getSheets()).hasSize(ResearchEvidenceCatalog.DEFINITIONS.size());
    }

    private List<EvidenceDatasetPayload> completeDatasets() {
        return datasets(ResearchEvidenceCatalog.DEFINITIONS);
    }

    private List<EvidenceDatasetPayload> datasets(
            List<ResearchEvidenceCatalog.Definition> definitions) {
        return definitions.stream()
                .map(definition -> new EvidenceDatasetPayload(definition.datasetCode(), payload(definition).toString()))
                .toList();
    }

    private ObjectNode payload(ResearchEvidenceCatalog.Definition definition) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("sheetName", definition.sheetName());
        List<String> columns = definition.columns();
        ArrayNode columnsNode = payload.putArray("columns");
        columns.forEach(columnsNode::add);
        ObjectNode item = payload.putArray("items").addObject();
        for (String column : columns) {
            String value = "值-" + column;
            if (ResearchEvidenceCatalog.IMAGE_URL_FIELD.equals(column)) {
                value = "https://example.com/product.jpg";
            } else if (ResearchEvidenceCatalog.IMAGE_FIELD.equals(column)) {
                value = "图片预览";
            }
            item.put(column, value);
        }
        return payload;
    }
}
