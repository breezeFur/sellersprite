package com.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.research.config.ResearchProperties;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import com.yuanbaomao.sellersprite.research.service.ResearchSnapshotService;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchWorkbookRendererTest {

    private static final String JOB_ID = "job-workbook-001";

    @Mock
    private ResearchSnapshotService snapshotService;

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldRenderMockWorkbookWithStableSheetsAndNeutralizedFormulaText() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ResearchInput input = ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace(ResearchConstants.MARKETPLACE_US)
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0DB5VT4QJ"))
                .build();
        MockResearchDataProvider provider = new MockResearchDataProvider(
                objectMapper,
                new ClassPathResource("research/mock/v1/market-research.json"));
        List<ResearchDataset> datasets = new ArrayList<>();
        datasets.addAll(provider.checkQuota(input));
        datasets.addAll(provider.collectMarketAndProducts(input));
        datasets.addAll(provider.collectKeywords(input));
        datasets.addAll(provider.collectReviews(input));
        String longDescription = "完整字段".repeat(16_000);
        ResearchDataset productsDataset = datasets.stream()
                .filter(dataset -> "products".equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow();
        ObjectNode firstProduct = (ObjectNode) productsDataset.getPayload().get("items").get(0);
        firstProduct.putObject("experimental").put("score", 7);
        firstProduct.putArray("audiences").add("home").add("professional");
        firstProduct.putNull("explicitNull");
        firstProduct.put("largeIdentifier", new BigInteger("123456789012345678901234567890"));
        firstProduct.put("longDescription", longDescription);
        firstProduct.put("formulaLike", "=SUM(A1:A2)");
        List<MarketResearchSnapshot> snapshots = snapshots(datasets);
        when(snapshotService.listByJobId(JOB_ID)).thenReturn(snapshots);
        for (int index = 0; index < datasets.size(); index++) {
            ResearchDataset dataset = datasets.get(index);
            when(snapshotService.readPayload(snapshots.get(index)))
                    .thenReturn(dataset.getPayload());
        }

        ResearchWorkbookRenderer renderer = new ResearchWorkbookRenderer(
                new ResearchProperties(),
                new DefaultResourceLoader(),
                snapshotService,
                new ResearchWorkbookValidator(),
                new ResearchRawWorkbookWriter());
        ResearchWorkbookValidator validator = new ResearchWorkbookValidator();
        Path target = temporaryDirectory.resolve("mock-market-research.xlsx");

        renderer.render(job(), target);
        validator.validate(target);

        assertThat(Files.size(target)).isGreaterThan(1024L);
        try (InputStream inputStream = Files.newInputStream(target);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(ResearchConstants.REPORT_SHEETS.size());
            assertThat(sheetNames(workbook)).containsExactlyElementsOf(ResearchConstants.REPORT_SHEETS);

            Sheet summary = workbook.getSheet("市场调研总结");
            assertStringCell(summary.getRow(0).getCell(0), "'=2+3");
            assertStringCell(summary.getRow(4).getCell(1), "'@facial cleansing device");
            assertStringCell(summary.getRow(5).getCell(1), "MOCK");

            Sheet productSheet = workbook.getSheet("US");
            assertThat(containsText(productSheet, "B0DB5VT4QJ")).isTrue();
            assertThat(productSheet.getRow(1)
                    .getCell(findColumn(productSheet, "销量环比增长率"))
                    .getCellType()).isEqualTo(CellType.BLANK);
            assertThat(containsText(workbook.getSheet("竞品品牌"), "OLANLY")).isTrue();
            assertStringCell(workbook.getSheet("竞品品牌").getRow(1)
                    .getCell(findColumn(workbook.getSheet("竞品品牌"), "ASIN")), "B0DB5VT4QJ");
            assertStringCell(workbook.getSheet("keywords").getRow(1).getCell(0),
                    "facial cleansing device");
            assertThat(workbook.getSheet("评价").getRow(33).getCell(5).getBooleanCellValue()).isTrue();

            Sheet indexSheet = workbook.getSheet("原始数据索引");
            assertThat(indexSheet.getLastRowNum()).isEqualTo(snapshots.size());
            assertThat(containsText(indexSheet, "quota.visits")).isTrue();
            assertStringCell(indexSheet.getRow(1).getCell(findColumn(indexSheet, "_snapshot.fetchedAt")),
                    "2024-07-03T09:46:40Z");
            assertStringCell(indexSheet.getRow(1).getCell(findColumn(indexSheet, "_snapshot.fetchedAtEpochMs")),
                    "1720000000000");
            assertThat(indexSheet.getRow(1).getCell(findColumn(indexSheet, "_snapshot.fetchedAtEpochMs"))
                    .getCellStyle().getDataFormatString()).isEqualTo("@");

            Sheet quotaSheet = workbook.getSheet("原始_配额");
            int quotaStatusColumn = findColumn(quotaSheet, "response.details.status");
            assertStringCell(quotaSheet.getRow(1).getCell(quotaStatusColumn), "mock-available");

            Sheet rawProductSheet = workbook.getSheet("原始_市场商品");
            Row rawProduct = findRow(rawProductSheet, "_snapshot.businessKey", "products");
            assertStringCell(rawProduct.getCell(findColumn(rawProductSheet, "item.asin")), "B0DB5VT4QJ");
            assertThat(rawProduct.getCell(findColumn(rawProductSheet, "item.experimental.score"))
                    .getNumericCellValue()).isEqualTo(7D);
            assertStringCell(rawProduct.getCell(findColumn(rawProductSheet, "item.explicitNull")),
                    ResearchRawWorkbookWriter.NULL_MARKER);
            assertStringCell(rawProduct.getCell(findColumn(rawProductSheet, "item.largeIdentifier")),
                    "123456789012345678901234567890");
            assertStringCell(rawProduct.getCell(findColumn(rawProductSheet, "item.audiences")),
                    "[\"home\",\"professional\"]");
            assertStringCell(rawProduct.getCell(findColumn(rawProductSheet, "item.formulaLike")),
                    "'=SUM(A1:A2)");
            String restoredLongText = stringCell(rawProduct, rawProductSheet, "item.longDescription.part1")
                    + stringCell(rawProduct, rawProductSheet, "item.longDescription.part2")
                    + stringCell(rawProduct, rawProductSheet, "item.longDescription.part3");
            assertThat(restoredLongText).isEqualTo(longDescription);
            assertThat(rawProductSheet.getPaneInformation()).isNotNull();
            assertThat(rawProductSheet.getPaneInformation().isFreezePane()).isTrue();

            Sheet rawKeywordSheet = workbook.getSheet("原始_关键词");
            assertStringCell(rawKeywordSheet.getRow(1).getCell(findColumn(rawKeywordSheet, "item.keyword")),
                    "facial cleansing device");
            Sheet rawReviewSheet = workbook.getSheet("原始_评论");
            assertThat(rawReviewSheet.getRow(1).getCell(findColumn(rawReviewSheet, "item.verified"))
                    .getBooleanCellValue()).isTrue();
        }
    }

    @Test
    void shouldRejectJsonFieldPathsThatWouldOverwriteEachOther() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putObject("a").put("b", "nested-value");
        payload.put("a.b", "flat-value");
        MarketResearchSnapshot snapshot = new MarketResearchSnapshot();
        snapshot.setPhase(ResearchPhase.CHECK_QUOTA.name());
        snapshot.setOperation("ACCOUNT_VISITS");
        snapshot.setBusinessKey("quota.visits");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            assertThatThrownBy(() -> new ResearchRawWorkbookWriter().append(
                    workbook, List.of(snapshot), ignored -> payload))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("字段路径")
                    .hasMessageContaining("response.a.b");
        }
    }

    private List<MarketResearchSnapshot> snapshots(List<ResearchDataset> datasets) {
        List<MarketResearchSnapshot> snapshots = new ArrayList<>();
        for (int index = 0; index < datasets.size(); index++) {
            ResearchDataset dataset = datasets.get(index);
            MarketResearchSnapshot snapshot = new MarketResearchSnapshot();
            snapshot.setSnapshotId("snapshot-" + index);
            snapshot.setJobId(JOB_ID);
            snapshot.setPhase(phase(dataset).name());
            snapshot.setOperation(dataset.getOperation());
            snapshot.setBusinessKey(dataset.getDatasetCode());
            snapshot.setDataSourceMode("MOCK");
            snapshot.setRecordCount(dataset.getRecordCount());
            snapshot.setFetchedAt(1_720_000_000_000L + index);
            snapshot.setSha256("sha256-" + index);
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    private ResearchPhase phase(ResearchDataset dataset) {
        if ("quota.visits".equals(dataset.getDatasetCode())) {
            return ResearchPhase.CHECK_QUOTA;
        }
        if (dataset.getDatasetCode().startsWith("reviews")) {
            return ResearchPhase.COLLECT_REVIEWS;
        }
        if ("keywords".equals(dataset.getDatasetCode())) {
            return ResearchPhase.COLLECT_KEYWORDS;
        }
        return ResearchPhase.COLLECT_MARKET_AND_PRODUCTS;
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setReportName("=2+3");
        job.setMarketplace(ResearchConstants.MARKETPLACE_US);
        job.setKeyword("@facial cleansing device");
        job.setDataSourceMode("MOCK");
        return job;
    }

    private List<String> sheetNames(XSSFWorkbook workbook) {
        List<String> names = new ArrayList<>();
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            names.add(workbook.getSheetName(index));
        }
        return names;
    }

    private boolean containsText(Sheet sheet, String expected) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.toString().contains(expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int findColumn(Sheet sheet, String header) {
        for (Cell cell : sheet.getRow(0)) {
            if (header.equals(cell.getStringCellValue())) {
                return cell.getColumnIndex();
            }
        }
        throw new AssertionError("未找到列: " + header);
    }

    private Row findRow(Sheet sheet, String header, String expected) {
        int columnIndex = findColumn(sheet, header);
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && row.getCell(columnIndex) != null
                    && expected.equals(row.getCell(columnIndex).getStringCellValue())) {
                return row;
            }
        }
        throw new AssertionError("未找到数据行: " + expected);
    }

    private String stringCell(Row row, Sheet sheet, String header) {
        return row.getCell(findColumn(sheet, header)).getStringCellValue();
    }

    private void assertStringCell(Cell cell, String expected) {
        assertThat(cell.getCellType()).isEqualTo(CellType.STRING);
        assertThat(cell.getStringCellValue()).isEqualTo(expected);
    }
}
