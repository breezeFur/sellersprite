package com.yuanbaomao.sellersprite.research.excel;

import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * 发布前重新打开并核对 Excel 结构。
 */
@Component
public class ResearchWorkbookValidator {

    private static final long MIN_FILE_SIZE = 1024;
    private static final List<HeaderExpectation> US_HEADERS = List.of(
            new HeaderExpectation(2, "ASIN"),
            new HeaderExpectation(3, "品牌"),
            new HeaderExpectation(11, "月销量"),
            new HeaderExpectation(13, "月销售额($)"),
            new HeaderExpectation(17, "价格($)"),
            new HeaderExpectation(21, "评分数"),
            new HeaderExpectation(23, "评分"),
            new HeaderExpectation(28, "上架时间"));
    private static final List<HeaderExpectation> KEYWORD_HEADERS = List.of(
            new HeaderExpectation(0, "流量词"),
            new HeaderExpectation(7, "月搜索量"),
            new HeaderExpectation(8, "购买量"),
            new HeaderExpectation(9, "购买率"),
            new HeaderExpectation(14, "商品数"),
            new HeaderExpectation(19, "PPC价格"));
    private static final List<HeaderExpectation> REVIEW_HEADERS = List.of(
            new HeaderExpectation(0, "ASIN"),
            new HeaderExpectation(1, "标题"),
            new HeaderExpectation(3, "内容"),
            new HeaderExpectation(5, "VP评论"),
            new HeaderExpectation(8, "星级"),
            new HeaderExpectation(9, "赞同数"));
    private final DataFormatter dataFormatter = new DataFormatter();

    public void validate(Path path) throws IOException {
        if (!Files.isRegularFile(path) || Files.size(path) < MIN_FILE_SIZE) {
            throw new IllegalStateException("市场调研Excel文件不存在或文件过小");
        }
        try (InputStream inputStream = Files.newInputStream(path);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            validateSheets(workbook);
            requireHeaders(workbook.getSheet("US"), 0, "US", US_HEADERS);
            requireHeaders(workbook.getSheet("keywords"), 0, "keywords", KEYWORD_HEADERS);
            requireHeaders(workbook.getSheet("评价"), 32, "评价", REVIEW_HEADERS);
            requireRawHeaders(workbook);
            Sheet summary = workbook.getSheet("市场调研总结");
            if (summary.getRow(0) == null || summary.getRow(0).getCell(0) == null
                    || summary.getRow(0).getCell(0).toString().isBlank()) {
                throw new IllegalStateException("市场调研总结缺少报告标题");
            }
        }
    }

    private void validateSheets(XSSFWorkbook workbook) {
        if (workbook.getNumberOfSheets() != ResearchConstants.REPORT_SHEETS.size()) {
            throw new IllegalStateException("市场调研Excel页签数量不正确");
        }
        for (int index = 0; index < ResearchConstants.REPORT_SHEETS.size(); index++) {
            String expected = ResearchConstants.REPORT_SHEETS.get(index);
            if (!expected.equals(workbook.getSheetName(index))) {
                throw new IllegalStateException("市场调研Excel页签顺序错误: " + expected);
            }
        }
    }

    private void requireRawHeaders(XSSFWorkbook workbook) {
        requireLeadingHeaders(
                workbook.getSheet("原始数据索引"),
                "原始数据索引",
                ResearchConstants.RAW_INDEX_HEADERS);
        for (String sheetName : ResearchConstants.RAW_DATA_SHEETS.subList(
                1, ResearchConstants.RAW_DATA_SHEETS.size())) {
            requireLeadingHeaders(
                    workbook.getSheet(sheetName),
                    sheetName,
                    ResearchConstants.RAW_RECORD_HEADERS);
        }
    }

    private void requireLeadingHeaders(Sheet sheet, String sheetName, List<String> headers) {
        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            Cell cell = sheet == null || sheet.getRow(0) == null
                    ? null
                    : sheet.getRow(0).getCell(columnIndex);
            String actual = cell == null ? "" : dataFormatter.formatCellValue(cell);
            if (!headers.get(columnIndex).equals(actual)) {
                throw new IllegalStateException(sheetName + "工作表缺少来源元数据列: "
                        + headers.get(columnIndex));
            }
        }
    }

    private void requireHeaders(
            Sheet sheet,
            int rowIndex,
            String sheetName,
            List<HeaderExpectation> expectations) {
        for (HeaderExpectation expectation : expectations) {
            Cell cell = sheet == null || sheet.getRow(rowIndex) == null
                    ? null
                    : sheet.getRow(rowIndex).getCell(expectation.columnIndex());
            String actual = cell == null ? "" : normalize(dataFormatter.formatCellValue(cell));
            if (!normalize(expectation.value()).equals(actual)) {
                String cellAddress = CellReference.convertNumToColString(expectation.columnIndex())
                        + (rowIndex + 1);
                throw new IllegalStateException(sheetName + "工作表表头" + cellAddress
                        + "错误，期望: " + expectation.value());
            }
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    private record HeaderExpectation(int columnIndex, String value) {
    }
}
