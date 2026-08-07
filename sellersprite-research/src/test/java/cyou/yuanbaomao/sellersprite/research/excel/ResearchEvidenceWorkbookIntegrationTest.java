package cyou.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceService;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ResearchEvidenceWorkbookIntegrationTest {

    private static final String JOB_ID = "job-evidence-workbook-001";
    private static final String SAMPLE_OUTPUT_PROPERTY = "research.sample.output";

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldRenderMockProviderEvidenceAsTwelveSheetWorkbook() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchDatasetService datasetService = mock(ResearchDatasetService.class);
        List<MarketResearchDataset> stored = new ArrayList<>();
        Map<MarketResearchDataset, JsonNode> payloads = new IdentityHashMap<>();
        when(datasetService.listByJobId(JOB_ID)).thenAnswer(ignored -> stored);
        when(datasetService.readPayload(any(MarketResearchDataset.class)))
                .thenAnswer(invocation -> payloads.get(invocation.getArgument(0)));

        ResearchInput input = input();
        MockResearchDataProvider provider = new MockResearchDataProvider(
                objectMapper,
                new ClassPathResource("research/mock/v1/market-research.json"));
        List<ResearchDataset> products = provider.collectProducts(input);
        List<ResearchDataset> sources = new ArrayList<>();
        sources.addAll(provider.checkQuota(input));
        sources.addAll(products);
        sources.addAll(provider.collectMarketSalesTrend(input));
        sources.addAll(provider.collectKeywordDemandTrend(input));
        sources.addAll(provider.collectSegmentOpportunity(input));
        sources.addAll(provider.collectReviews(input));
        sources.addAll(provider.collectKeywordIntelligence(input));
        sources.addAll(provider.collectAsinIntelligence(input));
        append(stored, payloads, sources);

        MarketResearchJob job = job();
        ResearchEvidenceService evidenceService = new ResearchEvidenceService(datasetService, objectMapper);
        List<ResearchDataset> evidence = ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(definition -> evidenceService.prepare(job, definition.phase()))
                .toList();
        append(stored, payloads, evidence);
        evidenceService.validate(JOB_ID);

        Path workbookPath = temporaryDirectory.resolve("market-research-evidence-sample.xlsx");
        ResearchWorkbookValidator validator = new ResearchWorkbookValidator();
        new ResearchWorkbookRenderer(
                        datasetService,
                        validator,
                        new ResearchEvidenceWorkbookWriter(),
                        mock(ResearchScreeningSummaryWorkbookAppender.class))
                .render(job, workbookPath);

        validator.validate(workbookPath);
        try (InputStream inputStream = Files.newInputStream(workbookPath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(evidenceSheetNames().size());
            assertThat(sheetNames(workbook)).containsExactlyElementsOf(evidenceSheetNames());
            for (ResearchEvidenceCatalog.Definition definition : ResearchEvidenceCatalog.DEFINITIONS) {
                assertThat((int) workbook.getSheet(definition.sheetName())
                                .getRow(0)
                                .getLastCellNum())
                        .isEqualTo(definition.columns().size());
                assertThat(headers(workbook.getSheet(definition.sheetName())))
                        .containsExactlyElementsOf(definition.columns())
                        .noneMatch(header -> header.equals("数据状态")
                                || header.equals("证据范围")
                                || header.equals("来源数据集")
                                || header.equals("数据局限")
                                || header.startsWith("原始."));
            }
            Sheet productsSheet = workbook.getSheet("US");
            Cell imageCell = productsSheet.getRow(1).getCell(findColumn(productsSheet, "图片"));
            assertThat(imageCell.getCellType()).isEqualTo(CellType.FORMULA);
            assertThat(imageCell.getCellFormula()).contains("IMAGE");
            assertBlank(workbook.getSheet("行业需求及趋势"), 1, "关键词");
            assertBlank(workbook.getSheet("VOC"), 1, "代表负向评价");
            assertBlank(workbook.getSheet("Keywords"), 1, "关联ASIN");
        }
        exportSampleWhenRequested(workbookPath);
    }

    private void exportSampleWhenRequested(Path workbookPath) throws Exception {
        String requestedOutput = System.getProperty(SAMPLE_OUTPUT_PROPERTY);
        if (requestedOutput == null || requestedOutput.isBlank()) {
            return;
        }
        Path output = Path.of(requestedOutput).toAbsolutePath().normalize();
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        Files.copy(workbookPath, output, StandardCopyOption.REPLACE_EXISTING);
    }

    private void append(
            List<MarketResearchDataset> stored,
            Map<MarketResearchDataset, JsonNode> payloads,
            List<ResearchDataset> values) {
        for (ResearchDataset value : values) {
            MarketResearchDataset dataset = new MarketResearchDataset();
            dataset.setDatasetId("dataset-" + stored.size());
            dataset.setJobId(JOB_ID);
            dataset.setDatasetCode(value.getDatasetCode());
            dataset.setOperation(value.getOperation());
            stored.add(dataset);
            payloads.put(dataset, value.getPayload());
        }
    }

    private ResearchDataset find(List<ResearchDataset> datasets, String datasetCode) {
        return datasets.stream()
                .filter(dataset -> datasetCode.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow();
    }

    private List<String> sheetNames(XSSFWorkbook workbook) {
        List<String> names = new ArrayList<>();
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            names.add(workbook.getSheetName(index));
        }
        return names;
    }

    private List<String> evidenceSheetNames() {
        return ResearchEvidenceCatalog.DEFINITIONS.stream()
                .map(ResearchEvidenceCatalog.Definition::sheetName)
                .toList();
    }

    private int findColumn(Sheet sheet, String header) {
        for (Cell cell : sheet.getRow(0)) {
            if (header.equals(cell.getStringCellValue())) {
                return cell.getColumnIndex();
            }
        }
        throw new AssertionError("缺少列: " + header);
    }

    private List<String> headers(Sheet sheet) {
        List<String> values = new ArrayList<>();
        for (Cell cell : sheet.getRow(0)) {
            values.add(cell.getStringCellValue());
        }
        return values;
    }

    private void assertBlank(Sheet sheet, int rowIndex, String header) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return;
        }
        assertThat(row.getCell(
                        findColumn(sheet, header), MissingCellPolicy.RETURN_BLANK_AS_NULL))
                .isNull();
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setResearchMonth("2026-07");
        return job;
    }

    private ResearchInput input() {
        return ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace("US")
                .nodeIdPath("172282:281407")
                .month("2026-07")
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0MOCK0001"))
                .collectionConfig(new CollectionGraphConfig())
                .build();
    }
}
