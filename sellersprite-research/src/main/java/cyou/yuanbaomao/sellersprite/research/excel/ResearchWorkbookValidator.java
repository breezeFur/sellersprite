package cyou.yuanbaomao.sellersprite.research.excel;

import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** 发布前重新打开并核对阶段证据工作簿结构。 */
@Component
public class ResearchWorkbookValidator {

    private final DataFormatter dataFormatter = new DataFormatter();

    public void validate(Path path) throws IOException {
        validate(path, ResearchEvidenceCatalog.DEFINITIONS);
    }

    public void validate(Path path, EvidenceStage stage) throws IOException {
        validate(path, ResearchEvidenceCatalog.definitions(stage), stage == EvidenceStage.SCREENING);
    }

    private void validate(
            Path path, List<ResearchEvidenceCatalog.Definition> definitions) throws IOException {
        validate(path, definitions, false);
    }

    private void validate(
            Path path,
            List<ResearchEvidenceCatalog.Definition> definitions,
            boolean requireScreeningSummary) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("市场调研Excel文件不存在");
        }
        try (InputStream inputStream = Files.newInputStream(path);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            validateSheets(workbook, definitions, requireScreeningSummary);
            for (ResearchEvidenceCatalog.Definition definition : definitions) {
                validateHeaders(workbook.getSheet(definition.sheetName()), definition);
            }
            if (requireScreeningSummary) {
                validateSummarySheet(workbook.getSheet(
                        ResearchScreeningSummaryWorkbookAppender.SUMMARY_SHEET_NAME));
            }
        }
    }

    private void validateSheets(
            XSSFWorkbook workbook,
            List<ResearchEvidenceCatalog.Definition> definitions,
            boolean requireScreeningSummary) {
        int expectedSheetCount = definitions.size() + (requireScreeningSummary ? 1 : 0);
        if (workbook.getNumberOfSheets() != expectedSheetCount) {
            throw new IllegalStateException("市场调研Excel页签数量不正确，期望"
                    + expectedSheetCount + "张证据页");
        }
        for (int index = 0; index < definitions.size(); index++) {
            String expected = definitions.get(index).sheetName();
            if (!expected.equals(workbook.getSheetName(index))) {
                throw new IllegalStateException("市场调研Excel页签数量或顺序不正确，位置"
                        + (index + 1) + "期望: " + expected);
            }
        }
        if (requireScreeningSummary
                && !ResearchScreeningSummaryWorkbookAppender.SUMMARY_SHEET_NAME.equals(
                        workbook.getSheetName(definitions.size()))) {
            throw new IllegalStateException("阶段一总结页位置不正确");
        }
    }

    private void validateSummarySheet(Sheet sheet) {
        Row header = sheet == null ? null : sheet.getRow(0);
        if (header == null
                || !ResearchScreeningSummaryWorkbookAppender.SHEET_COLUMN.equals(
                        dataFormatter.formatCellValue(header.getCell(0)))
                || !ResearchScreeningSummaryWorkbookAppender.CONCLUSION_COLUMN.equals(
                        dataFormatter.formatCellValue(header.getCell(1)))) {
            throw new IllegalStateException("阶段一总结工作表表头不正确");
        }
        if (sheet.getLastRowNum() < ResearchEvidenceCatalog.SCREENING_DEFINITIONS.size() + 1) {
            throw new IllegalStateException("阶段一总结工作表缺少逐表结论或总体结论");
        }
    }

    private void validateHeaders(
            Sheet sheet, ResearchEvidenceCatalog.Definition definition) {
        Row header = sheet == null ? null : sheet.getRow(0);
        if (header == null) {
            throw new IllegalStateException(definition.sheetName() + "工作表缺少标题行");
        }
        List<String> stableColumns = definition.columns();
        int actualColumnCount = header.getLastCellNum();
        if (actualColumnCount != stableColumns.size()) {
            throw new IllegalStateException(definition.sheetName() + "工作表表头数量不匹配");
        }
        for (int index = 0; index < actualColumnCount; index++) {
            Cell cell = header.getCell(index);
            String actual = cell == null ? "" : dataFormatter.formatCellValue(cell);
            if (!stableColumns.get(index).equals(actual)) {
                throw new IllegalStateException(definition.sheetName()
                        + "工作表表头错误，列" + (index + 1)
                        + "期望: " + stableColumns.get(index));
            }
        }
    }
}
