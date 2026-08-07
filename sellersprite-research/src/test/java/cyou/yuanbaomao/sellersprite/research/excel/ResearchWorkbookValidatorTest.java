package cyou.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResearchWorkbookValidatorTest {

    @TempDir
    private Path temporaryDirectory;

    private final ResearchWorkbookValidator validator = new ResearchWorkbookValidator();

    @Test
    void shouldAcceptTwelveHeaderOnlyBusinessSheets() throws Exception {
        Path workbook = validWorkbook("valid.xlsx");

        validator.validate(workbook);
    }

    @Test
    void shouldRejectExtraOrOutOfOrderSheet() throws Exception {
        Path extra = validWorkbook("extra.xlsx");
        rewrite(extra, workbook -> workbook.createSheet("市场调研总结"));
        assertThatThrownBy(() -> validator.validate(extra))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("期望12张证据页");

        Path reordered = validWorkbook("reordered.xlsx");
        rewrite(reordered, workbook -> workbook.setSheetOrder("US", 1));
        assertThatThrownBy(() -> validator.validate(reordered))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("顺序不正确");
    }

    @Test
    void shouldRejectUnexpectedHeader() throws Exception {
        Path workbook = validWorkbook("bad-header.xlsx");
        rewrite(workbook, value -> value.getSheet("Keywords").getRow(0).getCell(0).setCellValue("keywords"));

        assertThatThrownBy(() -> validator.validate(workbook))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Keywords工作表表头错误");
    }

    @Test
    void shouldRejectExtraHeader() throws Exception {
        Path workbook = validWorkbook("extra-header.xlsx");
        rewrite(workbook, value -> {
            org.apache.poi.ss.usermodel.Row header = value.getSheet("Keywords").getRow(0);
            header.createCell(header.getLastCellNum()).setCellValue("额外列");
        });

        assertThatThrownBy(() -> validator.validate(workbook))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Keywords工作表表头数量不匹配");
    }

    @Test
    void shouldValidateExactScreeningAndDeepDiveSheetSets() throws Exception {
        Path screening = validWorkbook(
                "screening.xlsx", ResearchEvidenceCatalog.SCREENING_DEFINITIONS);
        Path deepDive = validWorkbook(
                "deep-dive.xlsx", ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS);

        validator.validate(screening, EvidenceStage.SCREENING);
        validator.validate(deepDive, EvidenceStage.DEEP_DIVE);
        assertThatThrownBy(() -> validator.validate(screening, EvidenceStage.DEEP_DIVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("期望5张证据页");
        assertThatThrownBy(() -> validator.validate(deepDive, EvidenceStage.SCREENING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("期望8张证据页");
    }

    private Path validWorkbook(String fileName) throws Exception {
        return validWorkbook(fileName, ResearchEvidenceCatalog.DEFINITIONS);
    }

    private Path validWorkbook(
            String fileName, List<ResearchEvidenceCatalog.Definition> definitions) throws Exception {
        ResearchEvidenceWorkbookWriter writer = new ResearchEvidenceWorkbookWriter();
        Path path = temporaryDirectory.resolve(fileName);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (ResearchEvidenceCatalog.Definition definition : definitions) {
                writer.writeEvidenceTable(workbook, definition, definition.columns(), List.of());
            }
            if (definitions.equals(ResearchEvidenceCatalog.SCREENING_DEFINITIONS)) {
                var summary = workbook.createSheet(
                        ResearchScreeningSummaryWorkbookAppender.SUMMARY_SHEET_NAME);
                var header = summary.createRow(0);
                header.createCell(0).setCellValue(
                        ResearchScreeningSummaryWorkbookAppender.SHEET_COLUMN);
                header.createCell(1).setCellValue(
                        ResearchScreeningSummaryWorkbookAppender.CONCLUSION_COLUMN);
                for (int index = 1;
                        index <= ResearchEvidenceCatalog.SCREENING_DEFINITIONS.size() + 1;
                        index++) {
                    var row = summary.createRow(index);
                    row.createCell(0).setCellValue("表" + index);
                    row.createCell(1).setCellValue("结论" + index);
                }
            }
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                workbook.write(outputStream);
            }
        }
        return path;
    }

    private void rewrite(Path path, WorkbookMutation mutation) throws Exception {
        XSSFWorkbook workbook;
        try (InputStream inputStream = Files.newInputStream(path)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        try (workbook) {
            mutation.apply(workbook);
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                workbook.write(outputStream);
            }
        }
    }

    @FunctionalInterface
    private interface WorkbookMutation {
        void apply(XSSFWorkbook workbook);
    }
}
