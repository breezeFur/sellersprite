package com.yuanbaomao.sellersprite.research.excel;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * 将快照响应按动态字段追加为可追溯的原始数据工作表。
 */
@Component
public class ResearchRawWorkbookWriter {

    static final String NULL_MARKER = "<NULL>";
    static final int TEXT_CHUNK_SIZE = 30_000;

    private static final int EXCEL_MAX_ROWS = 1_048_576;
    private static final int EXCEL_MAX_COLUMNS = 16_384;
    private static final int MIN_COLUMN_WIDTH = 12;
    private static final int MAX_COLUMN_WIDTH = 60;
    private static final BigInteger MAX_EXACT_EXCEL_INTEGER = new BigInteger("999999999999999");

    public void append(
            Workbook workbook,
            List<MarketResearchSnapshot> snapshots,
            Function<MarketResearchSnapshot, JsonNode> payloadReader) {
        requireRawSheetsAbsent(workbook);
        List<MarketResearchSnapshot> source = snapshots == null ? List.of() : snapshots;
        writeIndexSheet(workbook, source);
        writePhaseSheet(
                workbook,
                "原始_配额",
                source,
                ResearchPhase.CHECK_QUOTA,
                payloadReader);
        writePhaseSheet(
                workbook,
                "原始_市场商品",
                source,
                ResearchPhase.COLLECT_MARKET_AND_PRODUCTS,
                payloadReader);
        writePhaseSheet(
                workbook,
                "原始_关键词",
                source,
                ResearchPhase.COLLECT_KEYWORDS,
                payloadReader);
        writePhaseSheet(
                workbook,
                "原始_评论",
                source,
                ResearchPhase.COLLECT_REVIEWS,
                payloadReader);
    }

    private void writeIndexSheet(Workbook workbook, List<MarketResearchSnapshot> snapshots) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MarketResearchSnapshot snapshot : snapshots) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("_snapshot.snapshotId", snapshot.getSnapshotId());
            row.put("_snapshot.jobId", snapshot.getJobId());
            row.put("_snapshot.phase", snapshot.getPhase());
            row.put("_snapshot.operation", snapshot.getOperation());
            row.put("_snapshot.businessKey", snapshot.getBusinessKey());
            row.put("_snapshot.sourceMode", snapshot.getDataSourceMode());
            row.put("_snapshot.recordCount", snapshot.getRecordCount());
            row.put("_snapshot.fetchedAt", timestampText(snapshot.getFetchedAt()));
            row.put("_snapshot.fetchedAtEpochMs", exactText(snapshot.getFetchedAt()));
            row.put("_snapshot.sha256", snapshot.getSha256());
            rows.add(row);
        }
        writeSheet(workbook, "原始数据索引", ResearchConstants.RAW_INDEX_HEADERS, rows);
    }

    private void writePhaseSheet(
            Workbook workbook,
            String sheetName,
            List<MarketResearchSnapshot> snapshots,
            ResearchPhase phase,
            Function<MarketResearchSnapshot, JsonNode> payloadReader) {
        List<Map<String, Object>> rows = new ArrayList<>();
        snapshots.stream()
                .filter(snapshot -> phase.name().equals(snapshot.getPhase()))
                .forEach(snapshot -> appendSnapshotRows(rows, snapshot, payloadReader.apply(snapshot)));
        writeSheet(workbook, sheetName, ResearchConstants.RAW_RECORD_HEADERS, rows);
    }

    private void appendSnapshotRows(
            List<Map<String, Object>> rows,
            MarketResearchSnapshot snapshot,
            JsonNode payload) {
        if (payload != null && payload.isObject()) {
            JsonNode items = payload.get("items");
            if (items != null && items.isArray()) {
                Map<String, Object> responseFields = new LinkedHashMap<>();
                payload.properties().stream()
                        .filter(entry -> !"items".equals(entry.getKey()))
                        .forEach(entry -> flattenNode(
                                "response." + entry.getKey(), entry.getValue(), responseFields));
                if (items.isEmpty()) {
                    Map<String, Object> row = metadata(snapshot, 0);
                    row.putAll(responseFields);
                    rows.add(row);
                    return;
                }
                int recordIndex = 0;
                for (JsonNode item : items) {
                    Map<String, Object> row = metadata(snapshot, recordIndex++);
                    row.putAll(responseFields);
                    flattenNode(item != null && item.isObject() ? "item" : "item.value", item, row);
                    rows.add(row);
                }
                return;
            }
        }
        if (payload != null && payload.isArray()) {
            if (payload.isEmpty()) {
                rows.add(metadata(snapshot, 0));
                return;
            }
            int recordIndex = 0;
            for (JsonNode item : payload) {
                Map<String, Object> row = metadata(snapshot, recordIndex++);
                flattenNode(item != null && item.isObject() ? "item" : "item.value", item, row);
                rows.add(row);
            }
            return;
        }
        Map<String, Object> row = metadata(snapshot, 0);
        flattenNode(payload != null && payload.isObject() ? "response" : "response.value", payload, row);
        rows.add(row);
    }

    private Map<String, Object> metadata(MarketResearchSnapshot snapshot, int recordIndex) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("_snapshot.phase", snapshot.getPhase());
        row.put("_snapshot.operation", snapshot.getOperation());
        row.put("_snapshot.businessKey", snapshot.getBusinessKey());
        row.put("_snapshot.sourceMode", snapshot.getDataSourceMode());
        row.put("_snapshot.recordCount", snapshot.getRecordCount());
        row.put("_snapshot.fetchedAt", timestampText(snapshot.getFetchedAt()));
        row.put("_snapshot.fetchedAtEpochMs", exactText(snapshot.getFetchedAt()));
        row.put("_recordIndex", recordIndex);
        return row;
    }

    private void flattenNode(String path, JsonNode node, Map<String, Object> target) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            putFlattenedValue(target, path, NULL_MARKER);
            return;
        }
        if (node.isObject()) {
            if (node.isEmpty()) {
                putFlattenedValue(target, path, "{}");
                return;
            }
            node.properties().forEach(entry -> flattenNode(
                    path + "." + entry.getKey(), entry.getValue(), target));
            return;
        }
        if (node.isArray()) {
            putFlattenedValue(target, path, node.toString());
            return;
        }
        putFlattenedValue(target, path, scalarValue(node));
    }

    private void putFlattenedValue(Map<String, Object> target, String path, Object value) {
        if (target.containsKey(path)) {
            throw new IllegalStateException("JSON字段路径展开后发生冲突，无法无损导出: " + path);
        }
        target.put(path, value);
    }

    private Object scalarValue(JsonNode node) {
        if (node.isIntegralNumber()) {
            BigInteger exact = node.bigIntegerValue();
            if (exact.abs().compareTo(MAX_EXACT_EXCEL_INTEGER) <= 0) {
                return exact.longValue();
            }
            return exact.toString();
        }
        if (node.isFloatingPointNumber()) {
            BigDecimal exact = node.decimalValue();
            double numeric = exact.doubleValue();
            if (exact.precision() <= 15 && Double.isFinite(numeric)) {
                return numeric;
            }
            return exact.toPlainString();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node.isString() ? node.stringValue() : node.toString();
    }

    private void writeSheet(
            Workbook workbook,
            String sheetName,
            List<String> fixedHeaders,
            List<Map<String, Object>> rows) {
        if (rows.size() + 1 > EXCEL_MAX_ROWS) {
            throw new IllegalStateException(sheetName + "超过Excel最大行数，无法无损导出");
        }
        List<String> dynamicFields = dynamicFields(fixedHeaders, rows);
        Map<String, Integer> partCounts = partCounts(dynamicFields, rows);
        List<Column> columns = columns(fixedHeaders, dynamicFields, partCounts);
        if (columns.size() > EXCEL_MAX_COLUMNS) {
            throw new IllegalStateException(sheetName + "超过Excel最大列数，无法无损导出");
        }

        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle textStyle = textStyle(workbook);
        Row header = sheet.createRow(0);
        for (int index = 0; index < columns.size(); index++) {
            Cell cell = header.createCell(index);
            cell.setCellValue(columns.get(index).header());
            cell.setCellStyle(headerStyle);
        }
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row excelRow = sheet.createRow(rowIndex + 1);
            Map<String, Object> values = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                Object value = columns.get(columnIndex).value(values);
                if (value != null) {
                    writeCell(excelRow.createCell(columnIndex), value, textStyle);
                }
            }
        }
        styleSheet(sheet, columns, rows);
    }

    private List<String> dynamicFields(
            List<String> fixedHeaders,
            List<Map<String, Object>> rows) {
        Set<String> fields = new TreeSet<>();
        for (Map<String, Object> row : rows) {
            fields.addAll(row.keySet());
        }
        fields.removeAll(fixedHeaders);
        return List.copyOf(fields);
    }

    private Map<String, Integer> partCounts(
            List<String> dynamicFields,
            List<Map<String, Object>> rows) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String field : dynamicFields) {
            int maximum = 1;
            for (Map<String, Object> row : rows) {
                Object value = row.get(field);
                if (value instanceof String text) {
                    maximum = Math.max(maximum, parts(text));
                }
            }
            counts.put(field, maximum);
        }
        return counts;
    }

    private List<Column> columns(
            List<String> fixedHeaders,
            List<String> dynamicFields,
            Map<String, Integer> partCounts) {
        List<Column> result = new ArrayList<>();
        Set<String> headers = new LinkedHashSet<>();
        fixedHeaders.forEach(header -> addColumn(result, headers, new Column(header, header, 0, 1)));
        for (String field : dynamicFields) {
            int count = partCounts.getOrDefault(field, 1);
            if (count == 1) {
                addColumn(result, headers, new Column(field, field, 0, 1));
                continue;
            }
            for (int part = 0; part < count; part++) {
                addColumn(result, headers, new Column(
                        field + ".part" + (part + 1), field, part, count));
            }
        }
        return result;
    }

    private void addColumn(List<Column> columns, Set<String> headers, Column column) {
        if (!headers.add(column.header())) {
            throw new IllegalStateException("原始字段展开后出现重复列名: " + column.header());
        }
        columns.add(column);
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

    private void styleSheet(
            Sheet sheet,
            List<Column> columns,
            List<Map<String, Object>> rows) {
        sheet.createFreezePane(0, 1);
        if (!columns.isEmpty()) {
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rows.size()), 0, columns.size() - 1));
        }
        for (int index = 0; index < columns.size(); index++) {
            Column column = columns.get(index);
            int contentWidth = column.header().length() + 2;
            for (Map<String, Object> row : rows) {
                Object value = column.value(row);
                if (value != null) {
                    contentWidth = Math.max(contentWidth, String.valueOf(value).length() + 2);
                }
            }
            int width = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, contentWidth));
            sheet.setColumnWidth(index, width * 256);
        }
    }

    private String exactText(Long value) {
        return value == null ? null : value.toString();
    }

    private String timestampText(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value).toString();
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

    private int parts(String value) {
        return Math.max(1, (value.length() + TEXT_CHUNK_SIZE - 1) / TEXT_CHUNK_SIZE);
    }

    private String safeText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' ? "'" + value : value;
    }

    private void requireRawSheetsAbsent(Workbook workbook) {
        ResearchConstants.RAW_DATA_SHEETS.forEach(sheetName -> {
            if (workbook.getSheet(sheetName) != null) {
                throw new IllegalStateException("市场调研模板不应预置原始数据页: " + sheetName);
            }
        });
    }

    private record Column(String header, String sourceField, int partIndex, int partCount) {

        private Object value(Map<String, Object> row) {
            Object source = row.get(sourceField);
            if (source == null || partCount == 1) {
                return source;
            }
            String text = String.valueOf(source);
            int start = partIndex * TEXT_CHUNK_SIZE;
            if (start >= text.length()) {
                return null;
            }
            return text.substring(start, Math.min(text.length(), start + TEXT_CHUNK_SIZE));
        }
    }
}
