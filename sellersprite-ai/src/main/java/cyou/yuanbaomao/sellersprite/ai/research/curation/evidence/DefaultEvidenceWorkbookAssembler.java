package cyou.yuanbaomao.sellersprite.ai.research.curation.evidence;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ImageAsset;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ImageAssetSourceType;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheetRow;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.RawCell;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.RawSheet;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 直接消费不可变 evidence JSON，不经 Excel 文件反向解析。
 */
@Component
@RequiredArgsConstructor
public class DefaultEvidenceWorkbookAssembler implements EvidenceWorkbookAssembler {

    private static final int HEADER_ROW_INDEX = 0;
    private static final int FIRST_DATA_ROW_INDEX = 1;

    private final ObjectMapper objectMapper;

    @Override
    public ProductWorkbook assemble(String jobId, List<EvidenceDatasetPayload> datasets) {
        if (!StringUtils.hasText(jobId)) {
            throw new IllegalArgumentException("jobId 不能为空");
        }
        Map<String, JsonNode> payloads = parsePayloads(datasets);
        List<ResearchEvidenceCatalog.Definition> definitions = definitionsFor(payloads.keySet());
        ProductWorkbook workbook = new ProductWorkbook();
        workbook.setFileName("market-research-" + jobId + ".xlsx");
        for (int sheetIndex = 0; sheetIndex < definitions.size(); sheetIndex++) {
            ResearchEvidenceCatalog.Definition definition = definitions.get(sheetIndex);
            JsonNode payload = payloads.get(definition.datasetCode());
            ProductSheet sheet = toProductSheet(definition, payload, sheetIndex);
            workbook.getSheets().add(sheet);
            workbook.getRawSheets().add(sheet.getRawSheet());
        }
        return workbook;
    }

    private Map<String, JsonNode> parsePayloads(List<EvidenceDatasetPayload> datasets) {
        if (datasets == null || datasets.isEmpty()) {
            throw new IllegalArgumentException("evidence 数据集不能为空");
        }
        Map<String, JsonNode> payloads = new LinkedHashMap<>();
        for (EvidenceDatasetPayload dataset : datasets) {
            if (dataset == null) {
                throw new IllegalArgumentException("evidence 数据集不能包含 null");
            }
            if (!isEvidenceDataset(dataset.datasetCode())) {
                continue;
            }
            if (payloads.containsKey(dataset.datasetCode())) {
                throw new IllegalStateException("证据数据集重复: " + dataset.datasetCode());
            }
            try {
                JsonNode payload = objectMapper.readTree(dataset.payloadJson());
                if (payload == null || !payload.isObject()) {
                    throw new IllegalStateException("证据数据集不是 JSON 对象: " + dataset.datasetCode());
                }
                payloads.put(dataset.datasetCode(), payload);
            } catch (RuntimeException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new IllegalStateException("证据数据集 JSON 损坏: " + dataset.datasetCode(), exception);
            }
        }
        return payloads;
    }

    private List<ResearchEvidenceCatalog.Definition> definitionsFor(Set<String> datasetCodes) {
        if (datasetCodes.equals(datasetCodes(ResearchEvidenceCatalog.DEFINITIONS))) {
            return ResearchEvidenceCatalog.DEFINITIONS;
        }
        if (datasetCodes.equals(datasetCodes(ResearchEvidenceCatalog.SCREENING_DEFINITIONS))) {
            return ResearchEvidenceCatalog.SCREENING_DEFINITIONS;
        }
        if (datasetCodes.equals(datasetCodes(ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS))) {
            return ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS;
        }
        List<ResearchEvidenceCatalog.Definition> expected = datasetCodes.stream().allMatch(
                datasetCodes(ResearchEvidenceCatalog.SCREENING_DEFINITIONS)::contains)
                ? ResearchEvidenceCatalog.SCREENING_DEFINITIONS
                : datasetCodes.stream().allMatch(
                        datasetCodes(ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS)::contains)
                        ? ResearchEvidenceCatalog.DEEP_DIVE_DEFINITIONS
                        : ResearchEvidenceCatalog.DEFINITIONS;
        String missingDatasetCode = expected.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .filter(datasetCode -> !datasetCodes.contains(datasetCode))
                .findFirst()
                .orElse("未知组合");
        throw new IllegalStateException("缺少证据数据集: " + missingDatasetCode);
    }

    private Set<String> datasetCodes(List<ResearchEvidenceCatalog.Definition> definitions) {
        return definitions.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    private ProductSheet toProductSheet(
            ResearchEvidenceCatalog.Definition definition, JsonNode payload, int sheetIndex) {
        validateEnvelope(definition, payload);
        List<String> columns = columns(payload, definition);
        List<JsonNode> items = items(payload, definition.datasetCode());

        ProductSheet sheet = new ProductSheet();
        sheet.setSheetName(definition.sheetName());
        sheet.setSheetIndex(sheetIndex);
        sheet.setHeaders(columns);
        RawSheet rawSheet = new RawSheet();
        rawSheet.setSheetName(definition.sheetName());
        rawSheet.setSheetIndex(sheetIndex);
        rawSheet.setRowCount(items.size() + FIRST_DATA_ROW_INDEX);
        rawSheet.setColumnCount(columns.size());
        appendHeaderCells(rawSheet, columns);

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            int rowIndex = itemIndex + FIRST_DATA_ROW_INDEX;
            JsonNode item = items.get(itemIndex);
            ProductSheetRow row = new ProductSheetRow();
            row.setRowIndex(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                String column = columns.get(columnIndex);
                String value = valueText(item.get(column));
                if (!value.isBlank()) {
                    row.getCells().put(column, value);
                    rawSheet.getRawCells().add(rawCell(rowIndex, columnIndex, value));
                    appendImageAsset(rawSheet, definition.sheetName(), rowIndex, columnIndex, column, value);
                }
            }
            sheet.getRows().add(row);
        }
        rawSheet.setRawMarkdown(renderMarkdown(definition, columns, items));
        sheet.setRawSheet(rawSheet);
        return sheet;
    }

    private void validateEnvelope(ResearchEvidenceCatalog.Definition definition, JsonNode payload) {
        if (!definition.sheetName().equals(payload.path("sheetName").asText())) {
            throw new IllegalStateException(definition.datasetCode() + " 工作表名称不匹配");
        }
    }

    private List<String> columns(JsonNode payload, ResearchEvidenceCatalog.Definition definition) {
        JsonNode columnsNode = payload.get("columns");
        if (columnsNode == null || !columnsNode.isArray() || columnsNode.isEmpty()) {
            throw new IllegalStateException(definition.datasetCode() + " 缺少表头");
        }
        List<String> columns = new ArrayList<>();
        for (JsonNode column : columnsNode) {
            if (!column.isTextual() || !StringUtils.hasText(column.asText())) {
                throw new IllegalStateException(definition.datasetCode() + " 包含无效表头");
            }
            columns.add(column.asText());
        }
        if (!definition.columns().equals(columns)) {
            throw new IllegalStateException(definition.datasetCode() + " 业务列不匹配");
        }
        return List.copyOf(columns);
    }

    private List<JsonNode> items(JsonNode payload, String datasetCode) {
        JsonNode itemsNode = payload.get("items");
        if (itemsNode == null || !itemsNode.isArray()) {
            throw new IllegalStateException(datasetCode + " 缺少证据记录数组");
        }
        List<JsonNode> items = new ArrayList<>();
        for (JsonNode item : itemsNode) {
            if (item == null || !item.isObject()) {
                throw new IllegalStateException(datasetCode + " 包含非对象证据记录");
            }
            items.add(item);
        }
        return List.copyOf(items);
    }

    private void appendHeaderCells(RawSheet rawSheet, List<String> columns) {
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            rawSheet.getRawCells().add(rawCell(HEADER_ROW_INDEX, columnIndex, columns.get(columnIndex)));
        }
    }

    private RawCell rawCell(int rowIndex, int columnIndex, String value) {
        RawCell rawCell = new RawCell();
        rawCell.setRowIndex(rowIndex);
        rawCell.setColumnIndex(columnIndex);
        rawCell.setCellAddress(excelColumnName(columnIndex) + (rowIndex + 1));
        rawCell.setValue(value);
        return rawCell;
    }

    private void appendImageAsset(
            RawSheet rawSheet,
            String sheetName,
            int rowIndex,
            int columnIndex,
            String column,
            String value) {
        boolean imageColumn = ResearchEvidenceCatalog.IMAGE_URL_FIELD.equals(column)
                || ResearchEvidenceCatalog.IMAGE_FIELD.equals(column);
        if (!imageColumn || !(value.startsWith("http://") || value.startsWith("https://"))) {
            return;
        }
        ImageAsset asset = new ImageAsset();
        asset.setSheetName(sheetName);
        asset.setRowIndex(rowIndex);
        asset.setColumnIndex(columnIndex);
        asset.setCellAddress(excelColumnName(columnIndex) + (rowIndex + 1));
        asset.setSourceType(ImageAssetSourceType.URL);
        asset.setReference(value);
        rawSheet.getImageAssets().add(asset);
    }

    private String renderMarkdown(
            ResearchEvidenceCatalog.Definition definition,
            List<String> columns,
            List<JsonNode> items) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("Sheet: ").append(definition.sheetName()).append('\n');
        markdown.append("Row 1: ").append(String.join(" | ", columns));
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            JsonNode item = items.get(itemIndex);
            List<String> values = columns.stream()
                    .map(column -> normalizeInline(valueText(item.get(column))))
                    .toList();
            markdown.append('\n')
                    .append("Row ")
                    .append(itemIndex + 2)
                    .append(": ")
                    .append(String.join(" | ", values));
        }
        return markdown.toString();
    }

    private String valueText(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return "";
        }
        if (value.isTextual() || value.isNumber() || value.isBoolean()) {
            return value.asText();
        }
        return value.toString();
    }

    private String normalizeInline(String value) {
        return value == null ? "" : value.replaceAll("\\R+", " ").trim();
    }

    private boolean isEvidenceDataset(String datasetCode) {
        return ResearchEvidenceCatalog.DEFINITIONS.stream()
                .anyMatch(definition -> definition.datasetCode().equals(datasetCode));
    }

    private String excelColumnName(int columnIndex) {
        int value = columnIndex + 1;
        StringBuilder name = new StringBuilder();
        while (value > 0) {
            int remainder = (value - 1) % 26;
            name.append((char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        return name.reverse().toString();
    }
}
