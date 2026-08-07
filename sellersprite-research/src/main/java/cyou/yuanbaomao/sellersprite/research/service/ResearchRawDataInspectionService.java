package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.raw.ResearchRawDatasetStagePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/** 为分析 Agent 提供阶段受限的原始字段目录和只读字段投影。 */
@Service
@RequiredArgsConstructor
public class ResearchRawDataInspectionService {

    private static final int MAX_VISITED_NODES_PER_DATASET = 100_000;
    private static final int MAX_FIELDS_PER_GROUP = 500;
    private static final int MAX_SAMPLES_PER_FIELD = 2;
    private static final int MAX_SAMPLE_LENGTH = 120;
    private static final int MAX_QUERY_FIELDS = 12;
    private static final int DEFAULT_QUERY_LIMIT = 10;
    private static final int MAX_QUERY_LIMIT = 20;
    private static final int MAX_QUERY_VALUE_LENGTH = 300;
    private static final int MAX_CATALOG_LENGTH = 18_000;
    private static final int MAX_QUERY_OUTPUT_LENGTH = 16_000;
    private static final Set<String> EVIDENCE_REFERENCED_FIELD_NAMES = evidenceReferencedFieldNames();

    private final ResearchDatasetService datasetService;

    @Transactional(readOnly = true)
    public String describeCatalog(String jobId, ResearchStageCode stageCode) {
        List<DatasetGroupAccumulator> groups = profileGroups(jobId, stageCode);
        if (groups.isEmpty()) {
            return "当前阶段没有可查询的原始采集数据集。";
        }
        StringBuilder output = new StringBuilder("原始数据字段目录（证据引用状态仅用于探索提示，不是精确字段血缘）：\n");
        for (DatasetGroupAccumulator group : groups) {
            appendBounded(output, "\n## 接口组 " + valueOrUnknown(group.operation) + "\n");
            appendBounded(output, "采集节点：" + valueOrUnknown(group.nodeCode) + "\n");
            appendBounded(output, "可查询 datasetCode（" + group.datasetCodes.size() + " 个）："
                    + String.join("、", group.datasetCodes) + "\n");
            appendBounded(output, "字段数：" + group.fields.size() + "\n");
            group.fields.values().stream()
                    .sorted((left, right) -> left.path.compareTo(right.path))
                    .forEach(field -> appendBounded(output, formatField(field)));
            if (output.length() >= MAX_CATALOG_LENGTH) {
                output.append("\n字段目录已达到输出上限；请先依据已列字段按需查询。\n");
                break;
            }
        }
        return output.toString();
    }

    @Transactional(readOnly = true)
    public String queryFields(
            String jobId,
            ResearchStageCode stageCode,
            String datasetCode,
            List<String> requestedFieldPaths,
            Integer requestedLimit) {
        MarketResearchDataset dataset = requireAllowedDataset(jobId, stageCode, datasetCode);
        JsonNode payload = datasetService.readPayload(dataset);
        Set<String> availablePaths = profilePayload(payload).keySet();
        List<String> fieldPaths = normalizeRequestedPaths(requestedFieldPaths);
        List<String> unknownPaths = fieldPaths.stream()
                .filter(path -> !availablePaths.contains(path))
                .toList();
        if (!unknownPaths.isEmpty()) {
            throw new IllegalArgumentException("字段不在当前数据集目录中：" + String.join("、", unknownPaths));
        }

        int limit = normalizeLimit(requestedLimit);
        Map<String, List<FieldObservation>> observations = new LinkedHashMap<>();
        fieldPaths.forEach(path -> observations.put(path, new ArrayList<>()));
        collectSelectedValues(payload, "", "", observations, limit, new VisitBudget());

        StringBuilder output = new StringBuilder();
        appendQueryBounded(output, "datasetCode：" + datasetCode + "\n");
        appendQueryBounded(output, "采集接口：" + valueOrUnknown(dataset.getOperation()) + "\n");
        appendQueryBounded(output, "以下为字段实际路径和值，数组下标可用于关联同一条原始记录。\n");
        for (String fieldPath : fieldPaths) {
            List<FieldObservation> values = observations.get(fieldPath);
            appendQueryBounded(output, "\n## " + fieldPath + "（返回 " + values.size() + " 条）\n");
            if (values.isEmpty()) {
                appendQueryBounded(output, "- 当前响应中没有非空值\n");
                continue;
            }
            for (FieldObservation value : values) {
                appendQueryBounded(output, "- " + value.actualPath + " = " + value.value + "\n");
            }
        }
        return output.toString();
    }

    private List<DatasetGroupAccumulator> profileGroups(String jobId, ResearchStageCode stageCode) {
        Map<String, DatasetGroupAccumulator> groups = new LinkedHashMap<>();
        for (MarketResearchDataset dataset : allowedDatasets(jobId, stageCode)) {
            String groupKey = valueOrUnknown(dataset.getNodeCode()) + "|" + valueOrUnknown(dataset.getOperation());
            DatasetGroupAccumulator group = groups.computeIfAbsent(
                    groupKey,
                    ignored -> new DatasetGroupAccumulator(dataset.getNodeCode(), dataset.getOperation()));
            group.datasetCodes.add(dataset.getDatasetCode());
            mergeFields(group.fields, profilePayload(datasetService.readPayload(dataset)));
        }
        return List.copyOf(groups.values());
    }

    private List<MarketResearchDataset> allowedDatasets(String jobId, ResearchStageCode stageCode) {
        return datasetService.listByJobId(jobId).stream()
                .filter(dataset -> ResearchRawDatasetStagePolicy.allows(stageCode, dataset.getNodeCode()))
                .toList();
    }

    private MarketResearchDataset requireAllowedDataset(
            String jobId, ResearchStageCode stageCode, String datasetCode) {
        if (datasetCode == null || datasetCode.isBlank()) {
            throw new IllegalArgumentException("datasetCode 不能为空");
        }
        return allowedDatasets(jobId, stageCode).stream()
                .filter(dataset -> datasetCode.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("当前分析阶段无权读取数据集：" + datasetCode));
    }

    private Map<String, FieldAccumulator> profilePayload(JsonNode payload) {
        Map<String, FieldAccumulator> fields = new LinkedHashMap<>();
        profileNode(payload, "", fields, new VisitBudget());
        return fields;
    }

    private void profileNode(
            JsonNode node,
            String normalizedPath,
            Map<String, FieldAccumulator> fields,
            VisitBudget budget) {
        if (node == null || budget.exhausted() || fields.size() >= MAX_FIELDS_PER_GROUP) {
            return;
        }
        budget.visit();
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                profileNode(property.getValue(), appendProperty(normalizedPath, property.getKey()), fields, budget);
            }
            return;
        }
        if (node.isArray()) {
            String arrayPath = normalizedPath + "[]";
            for (JsonNode item : node) {
                profileNode(item, arrayPath, fields, budget);
            }
            return;
        }
        if (node.isNull() || normalizedPath.isBlank()) {
            return;
        }
        fields.computeIfAbsent(normalizedPath, FieldAccumulator::new).observe(node);
    }

    private void mergeFields(Map<String, FieldAccumulator> target, Map<String, FieldAccumulator> source) {
        for (FieldAccumulator sourceField : source.values()) {
            if (target.size() >= MAX_FIELDS_PER_GROUP && !target.containsKey(sourceField.path)) {
                continue;
            }
            target.computeIfAbsent(sourceField.path, FieldAccumulator::new).merge(sourceField);
        }
    }

    private void collectSelectedValues(
            JsonNode node,
            String normalizedPath,
            String actualPath,
            Map<String, List<FieldObservation>> observations,
            int limit,
            VisitBudget budget) {
        if (node == null || budget.exhausted() || allFieldsComplete(observations, limit)) {
            return;
        }
        budget.visit();
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                collectSelectedValues(
                        property.getValue(),
                        appendProperty(normalizedPath, property.getKey()),
                        appendProperty(actualPath, property.getKey()),
                        observations,
                        limit,
                        budget);
            }
            return;
        }
        if (node.isArray()) {
            int index = 0;
            for (JsonNode item : node) {
                collectSelectedValues(
                        item,
                        normalizedPath + "[]",
                        actualPath + "[" + index + "]",
                        observations,
                        limit,
                        budget);
                index++;
            }
            return;
        }
        if (node.isNull()) {
            return;
        }
        List<FieldObservation> fieldValues = observations.get(normalizedPath);
        if (fieldValues != null && fieldValues.size() < limit) {
            fieldValues.add(new FieldObservation(actualPath, truncate(node.asText(), MAX_QUERY_VALUE_LENGTH)));
        }
    }

    private List<String> normalizeRequestedPaths(List<String> requestedFieldPaths) {
        if (requestedFieldPaths == null) {
            throw new IllegalArgumentException("fieldPaths 不能为空");
        }
        List<String> paths = requestedFieldPaths.stream()
                .filter(path -> path != null && !path.isBlank())
                .map(String::trim)
                .distinct()
                .limit(MAX_QUERY_FIELDS)
                .toList();
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("至少指定一个字段路径");
        }
        return paths;
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_QUERY_LIMIT;
        }
        return Math.max(1, Math.min(requestedLimit, MAX_QUERY_LIMIT));
    }

    private boolean allFieldsComplete(Map<String, List<FieldObservation>> observations, int limit) {
        return observations.values().stream().allMatch(values -> values.size() >= limit);
    }

    private String formatField(FieldAccumulator field) {
        String types = String.join("|", field.types);
        String samples = field.samples.isEmpty() ? "" : "，样例=" + String.join(" / ", field.samples);
        String usage = isEvidenceReferenced(field.path) ? "证据已引用" : "尚未引用";
        return "- " + field.path + "：类型=" + types + "，非空观测=" + field.nonNullCount
                + "，[" + usage + "]" + samples + "\n";
    }

    private boolean isEvidenceReferenced(String path) {
        int separatorIndex = path.lastIndexOf('.');
        String leaf = separatorIndex >= 0 ? path.substring(separatorIndex + 1) : path;
        return EVIDENCE_REFERENCED_FIELD_NAMES.contains(leaf.replace("[]", "").toLowerCase(Locale.ROOT));
    }

    private String appendProperty(String path, String property) {
        return path == null || path.isBlank() ? property : path + "." + property;
    }

    private void appendBounded(StringBuilder output, String value) {
        if (output.length() >= MAX_CATALOG_LENGTH) {
            return;
        }
        int remaining = MAX_CATALOG_LENGTH - output.length();
        output.append(value, 0, Math.min(value.length(), remaining));
    }

    private void appendQueryBounded(StringBuilder output, String value) {
        if (output.length() >= MAX_QUERY_OUTPUT_LENGTH) {
            return;
        }
        int remaining = MAX_QUERY_OUTPUT_LENGTH - output.length();
        output.append(value, 0, Math.min(value.length(), remaining));
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private static String truncate(String value, int maxLength) {
        String normalized = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private static Set<String> evidenceReferencedFieldNames() {
        String[] names = {
                "asin", "imageUrl", "image", "brand", "title", "nodeLabelPath", "category", "bsr", "bsrCr",
                "units", "monthlyUnits", "amzUnit", "unitsGr", "revenue", "monthlyRevenue", "amzSales", "price",
                "averagePrice", "profit", "ratings", "reviewCount", "ratingsCv", "ratingDelta", "ratingsRate", "rating",
                "star", "variations", "fba", "sellers", "sellerName", "sellerNation", "fulfillment", "availableDate",
                "pkgWeight", "weight", "pkgDimensions", "dimension", "parent", "sku", "ebc", "video", "month", "date",
                "time", "products", "productCount", "totalProducts", "sampledUnits", "totalUnits", "sampledRevenue",
                "totalRevenue", "averageUnits", "avgUnits", "averageRevenue", "avgRevenue", "averageBsr", "avgBsr",
                "avgPrice", "averageRatings", "avgRatings", "averageRating", "avgRating", "brands", "newProducts",
                "newProductProportion", "averageProfit", "avgProfit", "averageSellers", "avgSellers", "asinCount",
                "returnRatio", "avgReturnRatio", "searchToPurchaseRatio", "avgSearchToPurchaseRatio", "keywrod", "keyword",
                "keywords", "glanceViews", "search", "searches", "monthlySearches", "purchase", "purchases", "purchaseRate",
                "ranking", "nodeLabelName", "nodeLabelLocale", "nodeLabelPathLocale", "nodeIdPath", "topProducts",
                "ebcProportion", "amazonSelfProportion", "fbaProportion", "fbmProportion", "sellerNationLabel",
                "sellerProportion", "l1NewRatio", "l3NewRatio", "l6NewRatio", "l12NewRatio", "top3ProductCrn",
                "top10ProductCrn", "count", "sales", "proportion", "ratio", "percentage", "author", "content",
                "verified", "vine", "skus", "likes", "images", "videos", "keywordCn", "keywordsCn", "marketPeriod",
                "clicks", "impressions", "supplyDemandRatio", "bid", "bidMin", "bidMax", "rankPosition", "position",
                "organicRank", "adPosition", "adRank", "titleDensityExact", "growth", "searchMonthlyCr", "parentUnitSales",
                "childUnitSales", "parentSalesRevenue", "childSalesRevenue", "dataAsin", "parentAsin", "rootCategoryLabel",
                "timePoint", "value"
        };
        return Arrays.stream(names)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static final class VisitBudget {

        private int visitedNodes;

        private void visit() {
            visitedNodes++;
        }

        private boolean exhausted() {
            return visitedNodes >= MAX_VISITED_NODES_PER_DATASET;
        }
    }

    private static final class DatasetGroupAccumulator {

        private final String nodeCode;
        private final String operation;
        private final Set<String> datasetCodes = new LinkedHashSet<>();
        private final Map<String, FieldAccumulator> fields = new LinkedHashMap<>();

        private DatasetGroupAccumulator(String nodeCode, String operation) {
            this.nodeCode = nodeCode;
            this.operation = operation;
        }
    }

    private static final class FieldAccumulator {

        private final String path;
        private final Set<String> types = new LinkedHashSet<>();
        private final Set<String> samples = new LinkedHashSet<>();
        private long nonNullCount;

        private FieldAccumulator(String path) {
            this.path = path;
        }

        private void observe(JsonNode node) {
            nonNullCount++;
            types.add(valueType(node));
            if (samples.size() < MAX_SAMPLES_PER_FIELD) {
                samples.add(truncate(node.asText(), MAX_SAMPLE_LENGTH));
            }
        }

        private void merge(FieldAccumulator source) {
            nonNullCount += source.nonNullCount;
            types.addAll(source.types);
            for (String sample : source.samples) {
                if (samples.size() >= MAX_SAMPLES_PER_FIELD) {
                    break;
                }
                samples.add(sample);
            }
        }

        private static String valueType(JsonNode node) {
            if (node.isTextual()) {
                return "STRING";
            }
            if (node.isNumber()) {
                return "NUMBER";
            }
            if (node.isBoolean()) {
                return "BOOLEAN";
            }
            return "VALUE";
        }
    }

    private record FieldObservation(String actualPath, String value) {
    }
}
