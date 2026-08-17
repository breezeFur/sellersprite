package cyou.yuanbaomao.sellersprite.research.report;

import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/** 从已持久化证据表生成不依赖模型发挥的最终报告图表。 */
@Service
@RequiredArgsConstructor
public class ResearchReportChartService implements ResearchReportChartPort {

    private static final String COMPETITOR_REVERSE_KEYWORD = "竞品反查词";
    private static final int KEYWORD_LIMIT = 10;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern EDGE_PUNCTUATION = Pattern.compile("^[\\p{Punct}，。；：！？、]+|[\\p{Punct}，。；：！？、]+$");

    private final ResearchDatasetService datasetService;
    private final ResearchStageInputService stageInputService;

    @Override
    public List<ResearchReportChart> buildCharts(String jobId) {
        Map<String, ResearchDataset> datasets = new LinkedHashMap<>();
        datasetService.readEvidenceDatasets(jobId)
                .forEach(dataset -> datasets.put(dataset.getDatasetCode(), dataset));
        List<ResearchReportChart> charts = new ArrayList<>();
        addIfPresent(charts, salesTrend(datasets.get("evidence.market-sales-trend")));
        addIfPresent(charts, demandTrend(datasets.get("evidence.market-demand-trend")));
        Set<String> selectedAsins = stageInputService.findSelection(jobId)
                .map(selection -> selection.selectedAsins().stream()
                        .map(asin -> asin.toUpperCase(Locale.ROOT))
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()))
                .orElse(Set.of());
        addIfPresent(charts, competitorKeywords(datasets.get("evidence.keywords"), selectedAsins));
        return List.copyOf(charts);
    }

    private ResearchReportChart salesTrend(ResearchDataset dataset) {
        TreeMap<String, BigDecimal> values = aggregateByMonth(dataset, "样本总月销量");
        return trendChart(
                "market-sales-volume",
                "market-sales-trend",
                "行业销售趋势",
                "行业月销量趋势",
                "销量",
                values,
                "件",
                "按行业销售趋势证据表月份汇总“样本总月销量”。");
    }

    private ResearchReportChart demandTrend(ResearchDataset dataset) {
        TreeMap<String, BigDecimal> values = aggregateByMonth(dataset, "浏览量/搜索量");
        return trendChart(
                "market-demand-volume",
                "market-demand-trend",
                "行业需求及趋势",
                "行业月度搜索需求趋势",
                "搜索需求",
                values,
                "次",
                "按行业需求及趋势证据表月份汇总“浏览量/搜索量”。");
    }

    private ResearchReportChart competitorKeywords(ResearchDataset dataset, Set<String> selectedAsins) {
        if (dataset == null) {
            return null;
        }
        Map<String, Set<String>> asinCoverage = new HashMap<>();
        Set<String> coveredAsins = new HashSet<>();
        Set<String> deduplicationKeys = new HashSet<>();
        for (JsonNode item : items(dataset)) {
            String sourceType = text(item, "来源类型");
            String asin = text(item, "关联ASIN").toUpperCase(Locale.ROOT);
            String keyword = normalizeKeyword(text(item, "关键词"));
            if (!COMPETITOR_REVERSE_KEYWORD.equals(sourceType)
                    || asin.isBlank()
                    || (!selectedAsins.isEmpty() && !selectedAsins.contains(asin))
                    || keyword.isBlank()
                    || !deduplicationKeys.add(asin + "\u0000" + keyword)) {
                continue;
            }
            coveredAsins.add(asin);
            asinCoverage.computeIfAbsent(keyword, ignored -> new HashSet<>()).add(asin);
        }
        List<Map.Entry<String, Set<String>>> ranked = asinCoverage.entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<String, Set<String>>>comparingInt(entry -> entry.getValue().size())
                        .reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(KEYWORD_LIMIT)
                .toList();
        if (ranked.isEmpty()) {
            return null;
        }
        boolean hasCrossAsinKeyword = asinCoverage.values().stream()
                .anyMatch(asins -> asins.size() > 1);
        boolean lacksCrossAsinKeyword = coveredAsins.size() > 1 && !hasCrossAsinKeyword;
        ResearchFinalReportSection section = ResearchFinalReportSection.requireByDatasetCode("evidence.keywords");
        return new ResearchReportChart(
                "competitor-keyword-frequency",
                section.code(),
                section.title(),
                ResearchReportChart.TYPE_HORIZONTAL_BAR,
                lacksCrossAsinKeyword
                        ? "所选竞品关键词覆盖（未形成跨 ASIN 高频共词）"
                        : "所选竞品 ASIN 高频关键词",
                ranked.stream().map(Map.Entry::getKey).toList(),
                List.of(new ResearchReportChart.Series(
                        "覆盖竞品 ASIN 数",
                        ranked.stream()
                                .map(entry -> BigDecimal.valueOf(entry.getValue().size()))
                                .toList())),
                "个 ASIN",
                "仅统计来源类型为“竞品反查词”且有关联 ASIN 的记录；同一 ASIN 内相同规范化关键词只计一次。"
                        + (lacksCrossAsinKeyword
                                ? "当前所选竞品未形成覆盖两个及以上 ASIN 的高频共词。"
                                : ""));
    }

    private TreeMap<String, BigDecimal> aggregateByMonth(ResearchDataset dataset, String valueField) {
        TreeMap<String, BigDecimal> values = new TreeMap<>();
        if (dataset == null) {
            return values;
        }
        for (JsonNode item : items(dataset)) {
            String month = text(item, "月份");
            BigDecimal value = decimal(item, valueField);
            if (!month.isBlank() && value != null) {
                values.merge(month, value, BigDecimal::add);
            }
        }
        return values;
    }

    private ResearchReportChart trendChart(
            String chartCode,
            String sectionCode,
            String sectionTitle,
            String title,
            String seriesName,
            TreeMap<String, BigDecimal> values,
            String unit,
            String methodology) {
        if (values.isEmpty()) {
            return null;
        }
        return new ResearchReportChart(
                chartCode,
                sectionCode,
                sectionTitle,
                "LINE",
                title,
                List.copyOf(values.keySet()),
                List.of(new ResearchReportChart.Series(seriesName, List.copyOf(values.values()))),
                unit,
                methodology);
    }

    private Iterable<JsonNode> items(ResearchDataset dataset) {
        JsonNode payload = dataset.getPayload();
        JsonNode items = payload == null ? null : payload.get("items");
        return items != null && items.isArray() ? items : List.of();
    }

    private String text(JsonNode item, String field) {
        JsonNode value = item.get(field);
        return value == null || value.isNull() ? "" : value.asText("").trim();
    }

    private BigDecimal decimal(JsonNode item, String field) {
        String raw = text(item, field).replace(",", "").replace("%", "");
        if (raw.isBlank() || "-".equals(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String normalizeKeyword(String keyword) {
        String normalized = keyword.toLowerCase(Locale.ROOT).trim();
        normalized = WHITESPACE.matcher(normalized).replaceAll(" ");
        return EDGE_PUNCTUATION.matcher(normalized).replaceAll("");
    }

    private void addIfPresent(List<ResearchReportChart> charts, ResearchReportChart chart) {
        if (chart != null) {
            charts.add(chart);
        }
    }
}
