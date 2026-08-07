package cyou.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ResearchRawWorkbookWriterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResearchRawWorkbookWriter writer = new ResearchRawWorkbookWriter();

    @Test
    void shouldSplitByOperationAndKeepTopLevelFields() throws Exception {
        MarketResearchDataset firstReviews = dataset("reviews.B0FIRST", "REVIEW_LIST", 2L);
        MarketResearchDataset secondReviews = dataset("reviews.B0SECOND", "REVIEW_LIST", 3L);
        MarketResearchDataset brands = dataset("market.brand", "MARKET_BRAND", 1L);
        List<MarketResearchDataset> datasets = List.of(firstReviews, secondReviews, brands);
        java.util.Map<MarketResearchDataset, JsonNode> payloads = java.util.Map.of(
                firstReviews,
                objectMapper.readTree("""
                        {"items":[{"asin":"B0FIRST","title":"First","badges":["A+", "Video"]}]}
                        """),
                secondReviews,
                objectMapper.readTree("""
                        {"items":[{"title":"Second","badges":{"vine":true}}]}
                        """),
                brands,
                objectMapper.readTree("""
                        [{"brand":"Acme","share":0.42}]
                        """));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            writer.append(workbook, datasets, payloads::get);

            assertThat(sheetNames(workbook)).containsExactly("MARKET_BRAND", "REVIEW_LIST");
            assertThat(workbook.getSheet("原始数据索引")).isNull();
            Sheet reviews = workbook.getSheet("REVIEW_LIST");
            assertThat(headers(reviews)).containsExactly("asin", "title", "badges");
            assertThat(headers(reviews))
                    .noneMatch(header -> header.startsWith("_dataset")
                            || header.startsWith("item.")
                            || header.startsWith("response.")
                            || header.startsWith("原始."));
            assertThat(reviews.getRow(2).getCell(0).getStringCellValue()).isEqualTo("B0SECOND");
            assertThat(objectMapper.readTree(reviews.getRow(1).getCell(2).getStringCellValue()))
                    .isEqualTo(objectMapper.readTree("[\"A+\",\"Video\"]"));
            assertThat(objectMapper.readTree(reviews.getRow(2).getCell(2).getStringCellValue()))
                    .isEqualTo(objectMapper.readTree("{\"vine\":true}"));
        }
    }

    @Test
    void shouldCreateEmptySheetAndTruncateLongCellBeforeReopen() throws Exception {
        MarketResearchDataset empty = dataset("market.empty", "EMPTY_OPERATION", 1L);
        MarketResearchDataset longText = dataset("market.long", "LONG_OPERATION", 2L);
        String oversized = "x".repeat(ResearchRawWorkbookWriter.EXCEL_MAX_CELL_TEXT_LENGTH + 100);
        java.util.Map<MarketResearchDataset, JsonNode> payloads = java.util.Map.of(
                empty, objectMapper.readTree("{\"items\":[]}"),
                longText, objectMapper.valueToTree(List.of(java.util.Map.of("content", oversized))));

        byte[] bytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writer.append(workbook, List.of(empty, longText), payloads::get);
            workbook.write(output);
            bytes = output.toByteArray();
        }

        try (XSSFWorkbook reopened = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(reopened.getSheet("EMPTY_OPERATION").getPhysicalNumberOfRows()).isZero();
            assertThat(reopened.getSheet("LONG_OPERATION")
                            .getRow(1)
                            .getCell(0)
                            .getStringCellValue())
                    .hasSize(ResearchRawWorkbookWriter.EXCEL_MAX_CELL_TEXT_LENGTH);
        }
    }

    private MarketResearchDataset dataset(String datasetCode, String operation, long fetchedAt) {
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetId(datasetCode);
        dataset.setDatasetCode(datasetCode);
        dataset.setOperation(operation);
        dataset.setFetchedAt(fetchedAt);
        return dataset;
    }

    private List<String> sheetNames(XSSFWorkbook workbook) {
        List<String> names = new ArrayList<>();
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            names.add(workbook.getSheetName(index));
        }
        return names;
    }

    private List<String> headers(Sheet sheet) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : sheet.getRow(0)) {
            headers.add(cell.getStringCellValue());
        }
        return headers;
    }
}
