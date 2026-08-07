package cyou.yuanbaomao.sellersprite.research.excel;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

/** 将采集数据集按外部操作写入独立原始工作表。 */
@Component
public class ResearchRawWorkbookWriter {

    static final int EXCEL_MAX_CELL_TEXT_LENGTH = 32_767;

    private static final int EXCEL_MAX_ROWS = 1_048_576;
    private static final int EXCEL_MAX_COLUMNS = 16_384;
    private static final int MIN_COLUMN_WIDTH = 12;
    private static final int MAX_COLUMN_WIDTH = 60;
    private static final BigInteger MAX_EXACT_EXCEL_INTEGER = new BigInteger("999999999999999");

    public void append(
            Workbook workbook,
            List<MarketResearchDataset> datasets,
            Function<MarketResearchDataset, JsonNode> payloadReader) {
        Map<String, List<MarketResearchDataset>> grouped = groupDatasets(datasets);
        Set<String> sheetNames = new LinkedHashSet<>();
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            sheetNames.add(workbook.getSheetName(index));
        }
        grouped.forEach((logicalCode, values) -> writeDatasetSheet(
                workbook,
                uniqueSheetName(logicalCode, sheetNames),
                values,
                payloadReader));
    }

    private Map<String, List<MarketResearchDataset>> groupDatasets(
            List<MarketResearchDataset> datasets) {
        List<MarketResearchDataset> source = datasets == null
                ? new ArrayList<>()
                : new ArrayList<>(datasets);
        source.sort(Comparator
                .comparing(this::logicalCode)
                .thenComparing(dataset -> text(dataset.getDatasetCode()))
                .thenComparing(dataset -> dataset.getFetchedAt() == null ? 0L : dataset.getFetchedAt())
                .thenComparing(dataset -> text(dataset.getDatasetId())));
        Map<String, List<MarketResearchDataset>> grouped = new LinkedHashMap<>();
        for (MarketResearchDataset dataset : source) {
            grouped.computeIfAbsent(logicalCode(dataset), ignored -> new ArrayList<>()).add(dataset);
        }
        return grouped;
    }

    private String logicalCode(MarketResearchDataset dataset) {
        if (dataset != null && StringUtils.hasText(dataset.getOperation())) {
            return dataset.getOperation();
        }
        if (dataset != null && StringUtils.hasText(dataset.getDatasetCode())) {
            return dataset.getDatasetCode();
        }
        return "dataset";
    }

    private void writeDatasetSheet(
            Workbook workbook,
            String sheetName,
            List<MarketResearchDataset> datasets,
            Function<MarketResearchDataset, JsonNode> payloadReader) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MarketResearchDataset dataset : datasets) {
            appendPayloadRows(rows, dataset, payloadReader.apply(dataset));
        }
        if (rows.size() + 1 > EXCEL_MAX_ROWS) {
            throw new IllegalStateException(sheetName + "超过Excel最大行数");
        }
        List<String> columns = columns(rows);
        if (columns.size() > EXCEL_MAX_COLUMNS) {
            throw new IllegalStateException(sheetName + "超过Excel最大列数");
        }

        Sheet sheet = workbook.createSheet(sheetName);
        if (columns.isEmpty()) {
            return;
        }
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle textStyle = textStyle(workbook);
        Row header = sheet.createRow(0);
        for (int index = 0; index < columns.size(); index++) {
            Cell cell = header.createCell(index);
            cell.setCellValue(columns.get(index));
            cell.setCellStyle(headerStyle);
        }
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            Map<String, Object> values = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                Object value = values.get(columns.get(columnIndex));
                if (value != null) {
                    writeCell(row.createCell(columnIndex), value, textStyle);
                }
            }
        }
        styleSheet(sheet, columns, rows);
    }

    private void appendPayloadRows(
            List<Map<String, Object>> rows,
            MarketResearchDataset dataset,
            JsonNode payload) {
        if (payload == null || payload.isNull() || payload.isMissingNode()) {
            return;
        }
        if (payload.isObject()) {
            JsonNode items = payload.get("items");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    rows.add(toRow(dataset, item));
                }
                return;
            }
            rows.add(toRow(dataset, payload));
            return;
        }
        if (payload.isArray()) {
            payload.forEach(item -> rows.add(toRow(dataset, item)));
            return;
        }
        rows.add(toRow(dataset, payload));
    }

    private Map<String, Object> toRow(MarketResearchDataset dataset, JsonNode record) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (record != null && record.isObject()) {
            record.properties().forEach(entry -> row.put(entry.getKey(), cellValue(entry.getValue())));
        } else if (record != null && !record.isNull() && !record.isMissingNode()) {
            row.put("value", cellValue(record));
        }
        addRequestIdentity(row, dataset);
        return row;
    }

    private void addRequestIdentity(Map<String, Object> row, MarketResearchDataset dataset) {
        String datasetCode = dataset == null ? null : dataset.getDatasetCode();
        if (!StringUtils.hasText(datasetCode)) {
            return;
        }
        addSuffixField(row, datasetCode, "reviews.", "asin");
        addSuffixField(row, datasetCode, "traffic-keywords.", "asin");
    }

    private void addSuffixField(
            Map<String, Object> row, String datasetCode, String prefix, String field) {
        if (datasetCode.startsWith(prefix) && !row.containsKey(field)) {
            row.put(field, datasetCode.substring(prefix.length()));
        }
    }

    private Object cellValue(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isObject() || value.isArray()) {
            return value.toString();
        }
        if (value.isIntegralNumber()) {
            BigInteger exact = value.bigIntegerValue();
            return exact.abs().compareTo(MAX_EXACT_EXCEL_INTEGER) <= 0
                    ? exact.longValue()
                    : exact.toString();
        }
        if (value.isFloatingPointNumber()) {
            BigDecimal exact = value.decimalValue();
            double numeric = exact.doubleValue();
            return exact.precision() <= 15 && Double.isFinite(numeric)
                    ? numeric
                    : exact.toPlainString();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        return value.asText();
    }

    private List<String> columns(List<Map<String, Object>> rows) {
        Set<String> columns = new LinkedHashSet<>();
        rows.forEach(row -> columns.addAll(row.keySet()));
        return List.copyOf(columns);
    }

    private String uniqueSheetName(String requestedName, Set<String> usedNames) {
        String base = WorkbookUtil.createSafeSheetName(requestedName, '_');
        if (!StringUtils.hasText(base)) {
            base = "dataset";
        }
        base = base.length() > 31 ? base.substring(0, 31) : base;
        String candidate = base;
        int suffix = 2;
        while (!usedNames.add(candidate)) {
            String marker = "_" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), 31 - marker.length())) + marker;
        }
        return candidate;
    }

    private void writeCell(Cell cell, Object value, CellStyle textStyle) {
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }
        cell.setCellValue(safeText(String.valueOf(value)));
        cell.setCellStyle(textStyle);
    }

    private String safeText(String value) {
        if (value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        boolean escapeFormula = first == '=' || first == '+' || first == '-' || first == '@';
        int maximum = escapeFormula
                ? EXCEL_MAX_CELL_TEXT_LENGTH - 1
                : EXCEL_MAX_CELL_TEXT_LENGTH;
        String visible = value.length() > maximum ? value.substring(0, maximum) : value;
        return escapeFormula ? "'" + visible : visible;
    }

    private void styleSheet(
            Sheet sheet, List<String> columns, List<Map<String, Object>> rows) {
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(
                0,
                Math.max(0, rows.size()),
                0,
                columns.size() - 1));
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            String column = columns.get(columnIndex);
            int width = column.length() + 2;
            for (Map<String, Object> row : rows) {
                Object value = row.get(column);
                if (value != null) {
                    width = Math.max(width, String.valueOf(value).length() + 2);
                }
            }
            sheet.setColumnWidth(
                    columnIndex,
                    Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, width)) * 256);
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle textStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setDataFormat(workbook.createDataFormat().getFormat("@"));
        return style;
    }

    private String text(String value) {
        return value == null ? "" : value;
    }
}
