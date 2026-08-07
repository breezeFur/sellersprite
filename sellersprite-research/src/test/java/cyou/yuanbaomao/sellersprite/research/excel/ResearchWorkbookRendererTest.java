package cyou.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchWorkbookRendererTest {

    private static final String JOB_ID = "job-workbook-evidence-001";

    @Mock
    private ResearchDatasetService datasetService;

    @Mock
    private ResearchScreeningSummaryWorkbookAppender screeningSummaryAppender;

    @TempDir
    private Path temporaryDirectory;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldRenderExactlyTwelveBusinessEvidenceSheetsAndImageFormula(CapturedOutput output) throws Exception {
        stubEvidenceDatasets(ResearchEvidenceCatalog.DEFINITIONS.size());
        Path target = temporaryDirectory.resolve("evidence-market-research.xlsx");

        renderer().render(job(), target);

        new ResearchWorkbookValidator().validate(target);
        assertThat(Files.size(target)).isGreaterThan(1024L);
        try (InputStream inputStream = Files.newInputStream(target);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            assertThat(sheetNames(workbook)).containsExactlyElementsOf(evidenceSheetNames());
            assertThat(sheetNames(workbook))
                    .doesNotContain("市场调研总结", "AI分析总览", "原始数据索引");

            Sheet products = workbook.getSheet("US");
            Cell image = products.getRow(1).getCell(findColumn(products, "图片"));
            assertThat(image.getCellType()).isEqualTo(CellType.FORMULA);
            assertThat(image.getCellFormula()).contains("IMAGE");
            Cell imageUrl = products.getRow(1).getCell(findColumn(products, "图片链接"));
            assertThat(imageUrl.getStringCellValue()).startsWith("https://");
            assertThat(imageUrl.getHyperlink()).isNotNull();
            assertThat(imageUrl.getHyperlink().getAddress()).isEqualTo(imageUrl.getStringCellValue());
            Cell title = products.getRow(1).getCell(findColumn(products, "标题"));
            assertThat(title.getStringCellValue()).isEqualTo("'=SUM(A1:A2)");

            assertThat(workbook.getSheet("Keywords")).isNotNull();
            for (ResearchEvidenceCatalog.Definition definition : ResearchEvidenceCatalog.DEFINITIONS) {
                Sheet sheet = workbook.getSheet(definition.sheetName());
                assertThat((int) sheet.getRow(0).getLastCellNum())
                        .isEqualTo(definition.columns().size());
                assertThat(headers(sheet)).containsExactlyElementsOf(definition.columns());
                assertThat(headers(sheet)).noneMatch(header -> header.equals("数据状态")
                        || header.equals("证据范围")
                        || header.equals("来源数据集")
                        || header.equals("数据局限")
                        || header.startsWith("原始."));
                assertThat(sheet.getLastRowNum()).isEqualTo(1);
            }
        }
        assertThat(output.getAll())
                .doesNotContain("Name '_xlfn.IMAGE' is completely unknown in the current workbook");
    }

    @Test
    void shouldRejectRenderingWhenOneEvidenceDatasetIsMissing() {
        stubEvidenceDatasets(ResearchEvidenceCatalog.DEFINITIONS.size() - 1);
        Path target = temporaryDirectory.resolve("incomplete-evidence.xlsx");

        assertThatThrownBy(() -> renderer().render(job(), target))
                .isInstanceOf(java.io.IOException.class)
                .hasMessageContaining("生成市场调研证据Excel失败")
                .hasRootCauseMessage("生成Excel缺少证据数据集: evidence.asin-operation-trend");
    }

    @Test
    void shouldRenderHeaderOnlySheetForEmptyEvidence() throws Exception {
        stubEvidenceDatasets(ResearchEvidenceCatalog.DEFINITIONS.size(), "evidence.keywords");
        Path target = temporaryDirectory.resolve("empty-keywords.xlsx");

        renderer().render(job(), target);

        try (InputStream inputStream = Files.newInputStream(target);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet keywords = workbook.getSheet("Keywords");
            assertThat(keywords.getLastRowNum()).isZero();
            assertThat(headers(keywords)).containsExactlyElementsOf(
                    ResearchEvidenceCatalog.requireByDatasetCode("evidence.keywords").columns());
        }
    }

    @Test
    void shouldRenderSevenScreeningEvidenceSheetsAndOneSummarySheet() throws Exception {
        stubEvidenceDatasets(ResearchEvidenceCatalog.SCREENING_DEFINITIONS, null);
        doAnswer(invocation -> {
            appendScreeningSummary(invocation.getArgument(0));
            return null;
        }).when(screeningSummaryAppender).append(
                org.mockito.ArgumentMatchers.any(XSSFWorkbook.class),
                org.mockito.ArgumentMatchers.eq(JOB_ID));
        Path target = temporaryDirectory.resolve("screening-evidence.xlsx");

        renderer().render(job(), target, EvidenceStage.SCREENING);

        new ResearchWorkbookValidator().validate(target, EvidenceStage.SCREENING);
        try (InputStream inputStream = Files.newInputStream(target);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            List<String> expected = new ArrayList<>(evidenceSheetNames(EvidenceStage.SCREENING));
            expected.add(ResearchScreeningSummaryWorkbookAppender.SUMMARY_SHEET_NAME);
            assertThat(sheetNames(workbook)).containsExactlyElementsOf(expected);
            assertThat(workbook.getSheet("评价")).isNull();
            assertThat(workbook.getSheet("VOC")).isNull();
            assertThat(workbook.getSheet("Keywords")).isNull();
        }
    }

    @Test
    void shouldRenderExactlyFiveDeepDiveEvidenceSheets() throws Exception {
        stubEvidenceDatasets(ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS, null);
        Path target = temporaryDirectory.resolve("deep-dive-evidence.xlsx");

        renderer().render(job(), target, EvidenceStage.DEEP_DIVE);

        new ResearchWorkbookValidator().validate(target, EvidenceStage.DEEP_DIVE);
        try (InputStream inputStream = Files.newInputStream(target);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            assertThat(sheetNames(workbook))
                    .containsExactly("评价", "VOC", "Keywords", "ASIN销售趋势", "ASIN运营趋势");
            assertThat(workbook.getSheet("US")).isNull();
        }
    }

    private void stubEvidenceDatasets(int count) {
        stubEvidenceDatasets(count, null);
    }

    private void stubEvidenceDatasets(int count, String emptyDatasetCode) {
        stubEvidenceDatasets(
                ResearchEvidenceCatalog.DEFINITIONS.subList(0, count), emptyDatasetCode);
    }

    private void stubEvidenceDatasets(
            List<ResearchEvidenceCatalog.Definition> definitions,
            String emptyDatasetCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<MarketResearchDataset> stored = new ArrayList<>();
        for (int index = 0; index < definitions.size(); index++) {
            ResearchEvidenceCatalog.Definition definition = definitions.get(index);
            MarketResearchDataset dataset = new MarketResearchDataset();
            dataset.setDatasetId("evidence-" + index);
            dataset.setJobId(JOB_ID);
            dataset.setDatasetCode(definition.datasetCode());
            dataset.setOperation("PREPARE_EVIDENCE");
            stored.add(dataset);

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("sheetName", definition.sheetName());
            List<String> columns = definition.columns();
            columns.forEach(payload.putArray("columns")::add);
            if (definition.datasetCode().equals(emptyDatasetCode)) {
                payload.putArray("items");
                when(datasetService.readPayload(dataset)).thenReturn(payload);
                continue;
            }
            ObjectNode item = payload.putArray("items").addObject();
            columns.forEach(item::putNull);
            String firstColumn = definition.columns().getFirst();
            item.put(firstColumn, index + 1);
            if ("US".equals(definition.sheetName())) {
                item.put("ASIN", "B0TEST0001");
                item.put("图片", "https://images.example.com/product.jpg");
                item.put("图片链接", "https://images.example.com/product.jpg");
                item.put("标题", "=SUM(A1:A2)");
            }
            when(datasetService.readPayload(dataset)).thenReturn(payload);
        }
        when(datasetService.listByJobId(JOB_ID)).thenReturn(stored);
    }

    private ResearchWorkbookRenderer renderer() {
        return new ResearchWorkbookRenderer(
                datasetService,
                new ResearchWorkbookValidator(),
                new ResearchEvidenceWorkbookWriter(),
                screeningSummaryAppender);
    }

    private void appendScreeningSummary(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(
                ResearchScreeningSummaryWorkbookAppender.SUMMARY_SHEET_NAME);
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue(ResearchScreeningSummaryWorkbookAppender.SHEET_COLUMN);
        header.createCell(1).setCellValue(
                ResearchScreeningSummaryWorkbookAppender.CONCLUSION_COLUMN);
        for (int index = 0;
                index < ResearchEvidenceCatalog.SCREENING_DEFINITIONS.size();
                index++) {
            var row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(
                    ResearchEvidenceCatalog.SCREENING_DEFINITIONS.get(index).sheetName());
            row.createCell(1).setCellValue("AI结论" + index);
        }
        var overall = sheet.createRow(
                ResearchEvidenceCatalog.SCREENING_DEFINITIONS.size() + 1);
        overall.createCell(0).setCellValue("阶段一总体结论");
        overall.createCell(1).setCellValue("整体总结");
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        return job;
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

    private List<String> evidenceSheetNames(EvidenceStage stage) {
        return ResearchEvidenceCatalog.definitions(stage).stream()
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
}
