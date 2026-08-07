package cyou.yuanbaomao.sellersprite.research.excel;

import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.math.BigInteger;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** 将结构化证据数据集写入固定业务工作表。 */
@Component
public class ResearchEvidenceWorkbookWriter {

    private static final int EXCEL_MAX_ROWS = 1_048_576;
    private static final int EXCEL_MAX_COLUMNS = 16_384;
    private static final int MIN_COLUMN_WIDTH = 12;
    private static final int MAX_COLUMN_WIDTH = 60;
    private static final int EXCEL_MAX_CELL_TEXT_LENGTH = 32_767;
    private static final BigInteger MAX_EXACT_EXCEL_INTEGER = new BigInteger("999999999999999");

    public void writeEvidenceTable(
            Workbook workbook,
            ResearchEvidenceCatalog.Definition definition,
            List<String> columns,
            List<JsonNode> records) {
        if (records.size() + 1 > EXCEL_MAX_ROWS) {
            throw new IllegalStateException(definition.sheetName() + "超过Excel最大行数");
        }
        validateEvidenceColumns(definition, columns);
        if (columns.size() > EXCEL_MAX_COLUMNS) {
            throw new IllegalStateException(definition.sheetName() + "超过Excel最大列数");
        }
        Sheet sheet = workbook.createSheet(definition.sheetName());
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle textStyle = textStyle(workbook);
        Row header = sheet.createRow(0);
        for (int index = 0; index < columns.size(); index++) {
            writeHeaderCell(header, index, columns.get(index), headerStyle);
        }
        header.setHeightInPoints(28);

        for (int rowIndex = 0; rowIndex < records.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            row.setHeightInPoints("US".equals(definition.sheetName()) ? 72 : 42);
            JsonNode record = records.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                String field = columns.get(columnIndex);
                JsonNode value = record == null ? null : record.get(field);
                if (hasCellValue(value)) {
                    writeEvidenceCell(
                            row.createCell(columnIndex),
                            value,
                            textStyle,
                            definition.sheetName(),
                            field);
                }
            }
        }
        styleEvidenceSheet(sheet, definition, columns, records);
    }

    public void writeEvidenceTable(
            Workbook workbook,
            ResearchEvidenceCatalog.Definition definition,
            List<JsonNode> records) {
        writeEvidenceTable(
                workbook,
                definition,
                definition.columns(),
                records);
    }

    private void writeHeaderCell(Row header, int columnIndex, String value, CellStyle style) {
        Cell cell = header.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void writeJsonCell(Cell cell, JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return;
        }
        if (value.isIntegralNumber()) {
            BigInteger integer = value.bigIntegerValue();
            if (integer.abs().compareTo(MAX_EXACT_EXCEL_INTEGER) > 0) {
                writeTextCell(cell, integer.toString());
            } else {
                cell.setCellValue(integer.doubleValue());
            }
            return;
        }
        if (value.isFloatingPointNumber()) {
            cell.setCellValue(value.doubleValue());
            return;
        }
        if (value.isBoolean()) {
            cell.setCellValue(value.booleanValue());
            return;
        }
        writeTextCell(cell, value.isString() ? value.stringValue() : value.toString());
    }

    private void writeEvidenceCell(
            Cell cell,
            JsonNode value,
            CellStyle textStyle,
            String sheetName,
            String field) {
        cell.setCellStyle(textStyle);
        if ("US".equals(sheetName)
                && ResearchEvidenceCatalog.IMAGE_FIELD.equals(field)
                && value.isString()
                && isHttpUrl(value.asText())) {
            String escapedUrl = value.asText().replace("\"", "\"\"");
            setFutureFunctionFormula(
                    cell,
                    "_xlfn.IMAGE(\"" + escapedUrl + "\",\"商品图片\",0)");
            return;
        }
        writeJsonCell(cell, value);
        if ("US".equals(sheetName)
                && ResearchEvidenceCatalog.IMAGE_URL_FIELD.equals(field)
                && value.isString()
                && isHttpUrl(value.asText())) {
            org.apache.poi.ss.usermodel.Hyperlink hyperlink =
                    cell.getSheet().getWorkbook().getCreationHelper()
                            .createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(value.asText());
            cell.setHyperlink(hyperlink);
        }
    }

    private void setFutureFunctionFormula(Cell cell, String formula) {
        Workbook workbook = cell.getSheet().getWorkbook();
        if (!(workbook instanceof XSSFWorkbook xssfWorkbook)) {
            cell.setCellFormula(formula);
            return;
        }
        boolean formulaValidationEnabled = xssfWorkbook.getCellFormulaValidation();
        xssfWorkbook.setCellFormulaValidation(false);
        try {
            // IMAGE 是 Excel 新函数，POI 5.5 尚未识别；仅跳过该受控公式的语法校验。
            cell.setCellFormula(formula);
        } finally {
            xssfWorkbook.setCellFormulaValidation(formulaValidationEnabled);
        }
    }

    private void styleEvidenceSheet(
            Sheet sheet,
            ResearchEvidenceCatalog.Definition definition,
            List<String> columns,
            List<JsonNode> records) {
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(
                0,
                Math.max(0, records.size()),
                0,
                columns.size() - 1));
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            String field = columns.get(columnIndex);
            int contentWidth = field.length() + 2;
            for (JsonNode record : records) {
                JsonNode value = record == null ? null : record.get(field);
                if (value != null && !value.isNull() && !value.isMissingNode()) {
                    contentWidth = Math.max(contentWidth, displayLength(value) + 2);
                }
            }
            int width = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, contentWidth));
            if (ResearchEvidenceCatalog.IMAGE_FIELD.equals(field)) {
                width = 16;
            }
            sheet.setColumnWidth(columnIndex, width * 256);
        }
    }

    private void validateEvidenceColumns(
            ResearchEvidenceCatalog.Definition definition, List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalStateException(definition.sheetName() + "缺少Excel表头");
        }
        if (!definition.columns().equals(columns)) {
            throw new IllegalStateException(definition.sheetName() + "Excel表头不匹配");
        }
        Set<String> uniqueColumns = new LinkedHashSet<>();
        for (int index = 0; index < columns.size(); index++) {
            String column = columns.get(index);
            if (column == null || column.isBlank()) {
                throw new IllegalStateException(definition.sheetName() + "包含空Excel表头");
            }
            if (!uniqueColumns.add(column)) {
                throw new IllegalStateException(definition.sheetName() + "包含重复Excel表头: " + column);
            }
        }
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean hasCellValue(JsonNode value) {
        return value != null
                && !value.isNull()
                && !value.isMissingNode()
                && (!value.isString() || !value.asText().isBlank());
    }

    private void writeTextCell(Cell cell, String value) {
        String safeValue = safeText(value);
        if (safeValue.length() > EXCEL_MAX_CELL_TEXT_LENGTH) {
            safeValue = safeValue.substring(0, EXCEL_MAX_CELL_TEXT_LENGTH);
        }
        cell.setCellValue(safeValue);
    }

    private int displayLength(Object value) {
        String text;
        if (value instanceof JsonNode node) {
            text = node.isString() ? node.stringValue() : node.toString();
        } else {
            text = String.valueOf(value);
        }
        return Math.min(MAX_COLUMN_WIDTH, text.length());
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle textStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setDataFormat(workbook.createDataFormat().getFormat("@"));
        return style;
    }

    private String safeText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' ? "'" + value : value;
    }
}
