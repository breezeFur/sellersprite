package cyou.yuanbaomao.sellersprite.research.evidence;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 将已持久化源数据确定性整理为阶段化可发布证据。 */
@Service
@RequiredArgsConstructor
public class ResearchEvidenceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int DECIMAL_SCALE = 4;
    private static final int SALES_ROLLING_MONTHS = 3;
    private static final BigDecimal HIGH_RETURN_RISK_THRESHOLD = new BigDecimal("0.20");
    private static final BigDecimal LOW_RETURN_RISK_THRESHOLD = new BigDecimal("-0.20");
    private static final Set<String> RANKED_CONCENTRATION_DATASET_CODES = Set.of(
            "market.goods-concentration",
            "market.brand-concentration",
            "market.seller-concentration");

    private final ResearchDatasetService datasetService;
    private final ObjectMapper objectMapper;

    public ResearchDataset prepare(MarketResearchJob job, ResearchPhase phase) {
        ResearchEvidenceCatalog.Definition definition = ResearchEvidenceCatalog.requireByPhase(phase);
        List<MarketResearchDataset> datasets = datasetService.listByJobId(job.getJobId());
        EvidenceRows evidence = switch (phase) {
            case PREPARE_US_EVIDENCE -> productsEvidence(datasets);
            case PREPARE_SALES_TREND_EVIDENCE -> salesTrendEvidence(job, datasets);
            case PREPARE_DEMAND_TREND_EVIDENCE -> demandTrendEvidence(job, datasets);
            case PREPARE_SEGMENT_MARKET_EVIDENCE -> segmentMarketEvidence(datasets);
            case PREPARE_SEGMENT_RETURN_EVIDENCE -> segmentReturnEvidence(datasets);
            case PREPARE_BRAND_EVIDENCE -> brandEvidence(datasets);
            case PREPARE_CONCENTRATION_EVIDENCE -> concentrationEvidence(datasets);
            case PREPARE_REVIEW_EVIDENCE -> reviewEvidence(datasets);
            case PREPARE_VOC_EVIDENCE -> vocEvidence(datasets);
            case PREPARE_KEYWORD_EVIDENCE -> keywordEvidence(datasets);
            case PREPARE_ASIN_SALES_TREND_EVIDENCE -> asinSalesTrendEvidence(datasets);
            case PREPARE_ASIN_OPERATION_TREND_EVIDENCE -> asinOperationTrendEvidence(datasets);
            default -> throw new IllegalArgumentException("阶段不是证据整理节点: " + phase);
        };
        ObjectNode envelope = envelope(definition, evidence.rows());
        return new ResearchDataset(
                definition.datasetCode(),
                "PREPARE_EVIDENCE",
                envelope,
                envelope.path("items").size());
    }

    public void validate(String jobId) {
        validate(jobId, ResearchEvidenceCatalog.DEFINITIONS);
    }

    public void validate(String jobId, EvidenceStage stage) {
        validate(jobId, ResearchEvidenceCatalog.definitions(stage));
    }

    private void validate(
            String jobId, List<ResearchEvidenceCatalog.Definition> definitions) {
        List<MarketResearchDataset> datasets = datasetService.listByJobId(jobId);
        for (ResearchEvidenceCatalog.Definition definition : definitions) {
            MarketResearchDataset dataset = requireDataset(datasets, definition.datasetCode());
            JsonNode payload = datasetService.readPayload(dataset);
            validateEnvelope(definition, payload);
        }
    }

    private EvidenceRows productsEvidence(List<MarketResearchDataset> datasets) {
        MarketResearchDataset products = requireDataset(datasets, ResearchConstants.PRODUCTS_DATASET_CODE);
        List<ObjectNode> rows = new ArrayList<>();
        List<SourceRecord> items = sourceRecords(products);
        for (int index = 0; index < items.size(); index++) {
            SourceRecord source = items.get(index);
            JsonNode item = source.item();
            ObjectNode row = objectMapper.createObjectNode();
            row.put("排名", index + 1);
            copy(row, "ASIN", item, "asin");
            copy(row, ResearchEvidenceCatalog.IMAGE_FIELD, item, "imageUrl", "image");
            copy(row, ResearchEvidenceCatalog.IMAGE_URL_FIELD, item, "imageUrl", "image");
            copy(row, "品牌", item, "brand");
            copy(row, "标题", item, "title");
            copy(row, "类目", item, "nodeLabelPath", "category");
            copy(row, "BSR", item, "bsr");
            copy(row, "BSR增长率", item, "bsrCr");
            copy(row, "月销量", item, "units", "monthlyUnits", "amzUnit");
            copy(row, "销量增长率", item, "unitsGr");
            copy(row, "月销售额($)", item, "revenue", "monthlyRevenue", "amzSales");
            copy(row, "价格($)", item, "price", "averagePrice");
            copy(row, "毛利率", item, "profit");
            copy(row, "评分数", item, "ratings", "reviewCount");
            copy(row, "月新增评分", item, "ratingsCv", "ratingDelta");
            copy(row, "留评率", item, "ratingsRate");
            copy(row, "评分", item, "rating", "star");
            copy(row, "变体数", item, "variations");
            copy(row, "FBA费用($)", item, "fba");
            copy(row, "卖家数", item, "sellers");
            copy(row, "卖家名称", item, "sellerName");
            copy(row, "卖家所属地", item, "sellerNation");
            copy(row, "配送方式", item, "fulfillment");
            putDate(row, "上架时间", first(item, "availableDate"));
            copy(row, "包装重量", item, "pkgWeight");
            copy(row, "重量", item, "weight");
            copy(row, "包装尺寸", item, "pkgDimensions");
            copy(row, "尺寸", item, "dimension");
            copy(row, "父体ASIN", item, "parent");
            copy(row, "SKU", item, "sku");
            copy(row, "A+", item.path("badge"), "ebc");
            copy(row, "视频", item.path("badge"), "video");
            rows.add(row);
        }
        return evidence(rows);
    }

    private EvidenceRows salesTrendEvidence(
            MarketResearchJob job, List<MarketResearchDataset> datasets) {
        MarketResearchDataset trend = requireDataset(
                datasets, ResearchConstants.MARKET_SALES_TREND_DATASET_CODE);
        MarketResearchDataset products = requireDataset(datasets, ResearchConstants.PRODUCTS_DATASET_CODE);
        Map<String, ObjectNode> byMonth = new TreeMap<>();
        for (SourceRecord source : sourceRecords(trend)) {
            JsonNode item = source.item();
            String month = text(item, "month", "date", "time");
            if (!StringUtils.hasText(month)) {
                continue;
            }
            ObjectNode row = objectMapper.createObjectNode();
            row.put("月份", normalizeMonth(month));
            copy(row, "商品样本数", item, "products", "productCount", "totalProducts");
            copy(row, "样本总月销量", item, "sampledUnits", "units", "monthlyUnits", "totalUnits");
            copy(row, "样本总月销售额($)", item,
                    "sampledRevenue", "revenue", "monthlyRevenue", "totalRevenue");
            copy(row, "单品月均销量", item, "averageUnits", "avgUnits");
            copy(row, "单品月均销售额($)", item, "averageRevenue", "avgRevenue");
            copy(row, "平均BSR", item, "averageBsr", "avgBsr");
            copy(row, "平均价格($)", item, "averagePrice", "avgPrice");
            copy(row, "平均评分数", item, "averageRatings", "avgRatings");
            copy(row, "平均评分", item, "averageRating", "avgRating");
            copy(row, "品牌数", item, "brands");
            copy(row, "卖家数", item, "sellers");
            copy(row, "新品数", item, "newProducts");
            copy(row, "新品占比", item, "newProductProportion");
            copy(row, "平均毛利率", item, "averageProfit", "avgProfit");
            copy(row, "平均卖家数", item, "averageSellers", "avgSellers");
            byMonth.put(normalizeMonth(month), row);
        }
        byMonth.putIfAbsent(job.getResearchMonth(), currentSalesPoint(job, records(products)));
        List<ObjectNode> rows = new ArrayList<>(byMonth.values());
        return evidence(rows);
    }

    private ObjectNode currentSalesPoint(MarketResearchJob job, List<JsonNode> products) {
        long units = 0L;
        BigDecimal revenue = BigDecimal.ZERO;
        long bsrTotal = 0L;
        int bsrCount = 0;
        for (JsonNode product : products) {
            units += longValue(product, "units", "monthlyUnits", "amzUnit");
            revenue = revenue.add(decimalValue(product, "revenue", "monthlyRevenue", "amzSales"));
            long bsr = longValue(product, "bsr");
            if (bsr > 0) {
                bsrTotal += bsr;
                bsrCount++;
            }
        }
        ObjectNode row = objectMapper.createObjectNode();
        row.put("月份", job.getResearchMonth());
        row.put("商品样本数", products.size());
        row.put("样本总月销量", units);
        row.put("样本总月销售额($)", revenue);
        row.put("单品月均销量", products.isEmpty() ? 0D : (double) units / products.size());
        row.put("单品月均销售额($)", products.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(products.size()), 2, RoundingMode.HALF_UP));
        row.put("平均BSR", bsrCount == 0 ? 0D : (double) bsrTotal / bsrCount);
        return row;
    }

    private EvidenceRows demandTrendEvidence(
            MarketResearchJob job, List<MarketResearchDataset> datasets) {
        MarketResearchDataset marketTrend = requireDataset(
                datasets, ResearchConstants.MARKET_DEMAND_TREND_DATASET_CODE);
        MarketResearchDataset keywordTrend = findDataset(
                datasets, ResearchConstants.KEYWORD_TREND_DATASET_CODE);
        JsonNode marketPayload = datasetService.readPayload(marketTrend);
        List<ObjectNode> rows = new ArrayList<>();
        List<SourceRecord> marketRecords = sourceRecords(marketTrend);
        for (SourceRecord source : marketRecords) {
            ObjectNode row = demandRow(source, "SellerSprite类目需求", "");
            copyDemandSummary(row, marketPayload);
            rows.add(row);
        }
        if (marketRecords.isEmpty()) {
            ObjectNode row = objectMapper.createObjectNode();
            row.put("来源类型", hasDemandSummary(marketPayload)
                    ? "SellerSprite类目需求（仅类目汇总）"
                    : "SellerSprite类目需求（接口未返回数据）");
            row.put("关键词", "");
            row.put("月份", job.getResearchMonth());
            copyDemandSummary(row, marketPayload);
            rows.add(row);
        }
        if (keywordTrend != null) {
            for (SourceRecord source : sourceRecords(keywordTrend)) {
                JsonNode item = source.item();
                rows.add(demandRow(
                        source,
                        "核心关键词趋势",
                        text(item, "keywrod", "keyword", "keywords")));
            }
        }
        return evidence(rows);
    }

    private void copyDemandSummary(ObjectNode row, JsonNode marketPayload) {
        copy(row, "商品数", marketPayload, "asinCount");
        copy(row, "SellerSprite退货率", marketPayload, "returnRatio");
        copy(row, "类目平均退货率", marketPayload, "avgReturnRatio");
        copy(row, "搜索购买比", marketPayload, "searchToPurchaseRatio");
        copy(row, "类目平均搜索购买比", marketPayload, "avgSearchToPurchaseRatio");
    }

    private boolean hasDemandSummary(JsonNode marketPayload) {
        return first(
                marketPayload,
                "asinCount",
                "returnRatio",
                "avgReturnRatio",
                "searchToPurchaseRatio",
                "avgSearchToPurchaseRatio") != null;
    }

    private ObjectNode demandRow(SourceRecord source, String sourceType, String keyword) {
        JsonNode item = source.item();
        ObjectNode row = objectMapper.createObjectNode();
        row.put("来源类型", sourceType);
        row.put("关键词", keyword == null ? "" : keyword);
        row.put("月份", normalizeMonth(text(item, "month", "date", "time")));
        copy(row, "浏览量/搜索量", item, "glanceViews", "search", "searches", "monthlySearches", "units");
        copy(row, "购买量", item, "purchase", "purchases");
        copy(row, "购买率", item, "purchaseRate");
        copy(row, "商品数", item, "products", "asinCount");
        return row;
    }

    private EvidenceRows segmentMarketEvidence(List<MarketResearchDataset> datasets) {
        MarketResearchDataset markets = requireDataset(datasets, ResearchConstants.MARKET_RESEARCH_DATASET_CODE);
        MarketResearchDataset statistics = requireDataset(
                datasets, ResearchConstants.MARKET_STATISTICS_DATASET_CODE);
        List<SourceRecord> source = sourceRecords(markets);
        if (source.isEmpty()) {
            source = sourceRecords(statistics);
        }
        List<ObjectNode> rows = new ArrayList<>();
        for (int index = 0; index < source.size(); index++) {
            SourceRecord sourceRecord = source.get(index);
            JsonNode item = sourceRecord.item();
            ObjectNode row = objectMapper.createObjectNode();
            copyOrPut(row, "排名", item, index + 1, "ranking");
            copy(row, "细分市场", item, "nodeLabelName", "nodeLabelLocale", "nodeLabelPath");
            copy(row, "类目路径", item, "nodeLabelPath", "nodeLabelPathLocale", "nodeIdPath");
            copy(row, "商品总数", item, "totalProducts", "productCount", "products");
            copy(row, "样本商品数", item, "topProducts", "products");
            copy(row, "月销量", item, "totalUnits", "monthlyUnits", "avgUnits");
            copy(row, "月销售额($)", item, "totalRevenue", "monthlyRevenue", "avgRevenue");
            copy(row, "单品月均销量", item, "avgUnits");
            copy(row, "单品月均销售额($)", item, "avgRevenue");
            copy(row, "平均价格($)", item, "avgPrice", "averagePrice");
            copy(row, "平均BSR", item, "avgBsr");
            copy(row, "平均评分数", item, "avgRatings");
            copy(row, "平均评分", item, "avgRating");
            copy(row, "平均毛利率", item, "avgProfit");
            copy(row, "平均卖家数", item, "avgSellers");
            copy(row, "品牌数", item, "brands");
            copy(row, "卖家数", item, "sellers");
            copy(row, "A+占比", item, "ebcProportion");
            copy(row, "Amazon自营占比", item, "amazonSelfProportion");
            copy(row, "FBA占比", item, "fbaProportion");
            copy(row, "FBM占比", item, "fbmProportion");
            copy(row, "主要卖家所属地", item, "sellerNationLabel", "sellerNation");
            copy(row, "主要卖家地区占比", item, "sellerProportion");
            copy(row, "搜索购买比", item, "searchToPurchaseRatio");
            copy(row, "退货率", item, "returnRatio");
            copy(row, "近1月新品占比", item, "l1NewRatio");
            copy(row, "近3月新品占比", item, "l3NewRatio");
            copy(row, "近6月新品占比", item, "l6NewRatio", "newProductProportion");
            copy(row, "近12月新品占比", item, "l12NewRatio");
            copy(row, "头部3商品集中度", item, "top3ProductCrn");
            copy(row, "头部10商品集中度", item, "top10ProductCrn");
            rows.add(row);
        }
        return evidence(rows);
    }

    private EvidenceRows segmentReturnEvidence(List<MarketResearchDataset> datasets) {
        MarketResearchDataset marketTrend = requireDataset(
                datasets, ResearchConstants.MARKET_DEMAND_TREND_DATASET_CODE);
        MarketResearchDataset markets = requireDataset(datasets, ResearchConstants.MARKET_RESEARCH_DATASET_CODE);
        JsonNode demand = datasetService.readPayload(marketTrend);
        List<SourceRecord> marketRows = sourceRecords(markets);
        List<ObjectNode> rows = new ArrayList<>();
        for (SourceRecord source : marketRows) {
            JsonNode item = source.item();
            ObjectNode row = objectMapper.createObjectNode();
            copy(row, "细分市场", item, "nodeLabelName", "nodeLabelLocale", "nodeLabelPath");
            copy(row, "类目路径", item, "nodeLabelPath", "nodeLabelPathLocale", "nodeIdPath");
            JsonNode returnRatio = first(item, "returnRatio");
            if (missing(returnRatio)) {
                returnRatio = first(demand, "returnRatio");
            }
            putNode(row, "SellerSprite退货率", returnRatio);
            JsonNode average = first(item, "avgReturnRatio");
            if (missing(average)) {
                average = first(demand, "avgReturnRatio");
            }
            putNode(row, "类目平均退货率", average);
            BigDecimal returnValue = decimalOrNull(returnRatio);
            BigDecimal averageValue = decimalOrNull(average);
            if (returnValue != null && averageValue != null) {
                BigDecimal difference = returnValue.subtract(averageValue);
                putDecimal(row, "退货率差值", difference);
                BigDecimal relativeDifference = ratio(difference, averageValue);
                putDecimal(row, "相对类目均值", relativeDifference);
                if (relativeDifference != null) {
                    row.put("风险等级", returnRiskLevel(relativeDifference));
                }
            }
            rows.add(row);
        }
        return evidence(rows);
    }

    private EvidenceRows brandEvidence(List<MarketResearchDataset> datasets) {
        MarketResearchDataset productsDataset = requireDataset(datasets, ResearchConstants.PRODUCTS_DATASET_CODE);
        Map<String, BrandAggregate> grouped = new LinkedHashMap<>();
        for (SourceRecord source : sourceRecords(productsDataset)) {
            JsonNode product = source.item();
            String brand = text(product, "brand");
            if (!StringUtils.hasText(brand)) {
                brand = "未知品牌";
            }
            grouped.computeIfAbsent(brand, BrandAggregate::new).add(source);
        }
        MarketResearchDataset brandDataset = findDataset(datasets, "market.brand-concentration");
        List<SourceRecord> brandRecords = brandDataset == null ? List.of() : sourceRecords(brandDataset);
        if (!brandRecords.isEmpty()) {
            List<ObjectNode> rows = new ArrayList<>();
            for (int index = 0; index < brandRecords.size(); index++) {
                JsonNode item = brandRecords.get(index).item();
                String brandName = text(item, "brand");
                BrandAggregate aggregate = grouped.get(brandName);
                ObjectNode row = objectMapper.createObjectNode();
                copyOrPut(row, "排名", item, index + 1, "ranking");
                row.put("品牌", brandName);
                String representativeAsin = firstArrayText(item, "asins");
                if (!StringUtils.hasText(representativeAsin) && aggregate != null) {
                    representativeAsin = aggregate.representativeAsin();
                }
                row.put("代表ASIN", representativeAsin);
                JsonNode asins = item.get("asins");
                row.put("ASIN数", asins != null && asins.isArray()
                        ? asins.size() : aggregate == null ? 0 : aggregate.asinCount());
                JsonNode productCount = first(item, "products", "productCount", "count");
                if (missing(productCount) && aggregate != null) {
                    row.put("商品数", aggregate.products);
                } else {
                    putNode(row, "商品数", productCount);
                }
                copy(row, "新品数", item, "newProducts");
                copy(row, "月销量", item, "totalUnits", "units", "sales");
                copy(row, "月销售额($)", item, "totalRevenue", "revenue");
                copy(row, "平均价格($)", item, "avgPrice", "price");
                copy(row, "平均评分", item, "rating", "avgRating");
                copy(row, "平均评分数", item, "ratings", "avgRatings");
                appendBrandSampleFields(row, aggregate);
                copy(row, "销量份额", item,
                        "totalUnitsRatio", "proportion", "ratio", "percentage");
                copy(row, "销售额份额", item, "totalRevenueRatio");
                copy(row, "新品销量占比", item, "newUnitsRatio");
                copy(row, "新品销售额占比", item, "newRevenueRatio");
                rows.add(row);
            }
            return evidence(rows);
        }
        double totalUnits = grouped.values().stream().mapToDouble(BrandAggregate::units).sum();
        BigDecimal totalRevenue = grouped.values().stream()
                .map(BrandAggregate::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BrandAggregate> sorted = grouped.values().stream()
                .sorted(Comparator.comparingDouble(BrandAggregate::units).reversed())
                .toList();
        List<ObjectNode> rows = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            BrandAggregate brand = sorted.get(index);
            ObjectNode row = objectMapper.createObjectNode();
            row.put("排名", index + 1);
            row.put("品牌", brand.brand);
            row.put("代表ASIN", brand.representativeAsin());
            row.put("ASIN数", brand.asinCount());
            row.put("商品数", brand.products);
            row.put("月销量", brand.units);
            row.put("月销售额($)", brand.revenue);
            row.put("平均价格($)", brand.averagePrice());
            row.put("平均评分", brand.averageRating());
            row.put("平均评分数", brand.averageRatings());
            appendBrandSampleFields(row, brand);
            putDecimal(row, "销量份额", totalUnits == 0D
                    ? null : BigDecimal.valueOf(brand.units / totalUnits));
            putDecimal(row, "销售额份额", ratio(brand.revenue, totalRevenue));
            rows.add(row);
        }
        return evidence(rows);
    }

    private void appendBrandSampleFields(ObjectNode row, BrandAggregate aggregate) {
        if (aggregate == null) {
            return;
        }
        row.put("平均变体数", aggregate.averageVariations());
        row.put("主要卖家所属地", aggregate.mainSellerNation());
        putDecimal(row, "FBA商品占比", aggregate.products == 0
                ? null : BigDecimal.valueOf((double) aggregate.fbaProducts / aggregate.products));
        row.put("产品线线索", aggregate.productLineClues());
    }

    private EvidenceRows concentrationEvidence(List<MarketResearchDataset> datasets) {
        MarketResearchDataset productsDataset = requireDataset(datasets, ResearchConstants.PRODUCTS_DATASET_CODE);
        List<String> distributionCodes = List.of(
                "market.goods-concentration",
                "market.brand-concentration",
                "market.seller-concentration",
                "market.seller-location",
                "market.seller-type",
                "market.shelf-time",
                "market.ratings",
                "market.rating",
                "market.price",
                "market.shelf-trend",
                "market.ebc");
        List<ObjectNode> rows = new ArrayList<>();
        int rank = 1;
        List<SourceRecord> products = sourceRecords(productsDataset);
        MarketResearchDataset goodsDataset = findDataset(datasets, "market.goods-concentration");
        List<SourceRecord> goodsRecords = goodsDataset == null ? List.of() : sourceRecords(goodsDataset);
        if (goodsRecords.isEmpty()) {
            BigDecimal sampledUnits = products.stream()
                    .map(source -> decimalValue(source.item(), "units", "monthlyUnits", "amzUnit"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sampledRevenue = products.stream()
                    .map(source -> decimalValue(source.item(), "revenue", "monthlyRevenue", "amzSales"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cumulativeUnitsRatio = BigDecimal.ZERO;
            BigDecimal cumulativeRevenueRatio = BigDecimal.ZERO;
            for (int index = 0; index < Math.min(products.size(), 20); index++) {
                SourceRecord source = products.get(index);
                JsonNode product = source.item();
                ObjectNode row = objectMapper.createObjectNode();
                row.put("排名", rank++);
                row.put("集中维度", "商品集中度（样本）");
                copy(row, "对象/区间", product, "title", "asin");
                copy(row, "ASIN", product, "asin");
                copy(row, "品牌/卖家", product, "brand", "sellerName");
                row.put("商品数", 1);
                copy(row, "月销量", product, "units", "monthlyUnits", "amzUnit");
                copy(row, "月销售额($)", product, "revenue", "monthlyRevenue", "amzSales");
                BigDecimal unitsRatio = ratio(
                        decimalOrNull(product, "units", "monthlyUnits", "amzUnit"), sampledUnits);
                BigDecimal revenueRatio = ratio(
                        decimalOrNull(product, "revenue", "monthlyRevenue", "amzSales"), sampledRevenue);
                putDecimal(row, "占比", unitsRatio);
                putDecimal(row, "销售额占比", revenueRatio);
                if (unitsRatio != null) {
                    cumulativeUnitsRatio = cumulativeUnitsRatio.add(unitsRatio);
                    putDecimal(row, "累计销量占比", cumulativeUnitsRatio);
                }
                if (revenueRatio != null) {
                    cumulativeRevenueRatio = cumulativeRevenueRatio.add(revenueRatio);
                    putDecimal(row, "累计销售额占比", cumulativeRevenueRatio);
                }
                copy(row, "平均评分", product, "rating");
                copy(row, "平均价格($)", product, "price", "averagePrice");
                rows.add(row);
            }
        }
        for (String code : distributionCodes) {
            MarketResearchDataset distribution = findDataset(datasets, code);
            if (distribution == null) {
                continue;
            }
            int distributionRank = 1;
            BigDecimal cumulativeUnitsRatio = BigDecimal.ZERO;
            BigDecimal cumulativeRevenueRatio = BigDecimal.ZERO;
            List<SourceRecord> distributionRecords = "market.goods-concentration".equals(code)
                    ? goodsRecords : sourceRecords(distribution);
            for (SourceRecord source : distributionRecords) {
                JsonNode item = source.item();
                ObjectNode row = objectMapper.createObjectNode();
                copyOrPut(row, "排名", item, distributionRank++, "ranking");
                row.put("集中维度", concentrationDimension(code));
                copy(row, "对象/区间", item,
                        "label", "range", "asin", "brand", "sellerName", "sellerNationLabel", "month");
                copy(row, "ASIN", item, "asin");
                copy(row, "品牌/卖家", item, "brand", "sellerName", "sellerNationLabel");
                copy(row, "商品数", item, "productCount", "products", "count");
                copy(row, "月销量", item, "units", "sales", "totalUnits");
                copy(row, "月销售额($)", item, "revenue", "totalRevenue");
                copy(row, "占比", item,
                        "totalUnitsRatio", "proportion", "ratio", "percentage");
                copy(row, "销售额占比", item, "totalRevenueRatio");
                if (RANKED_CONCENTRATION_DATASET_CODES.contains(code)) {
                    BigDecimal unitsRatio = decimalOrNull(item, "totalUnitsRatio");
                    BigDecimal revenueRatio = decimalOrNull(item, "totalRevenueRatio");
                    if (unitsRatio != null) {
                        cumulativeUnitsRatio = cumulativeUnitsRatio.add(unitsRatio);
                        putDecimal(row, "累计销量占比", cumulativeUnitsRatio);
                    }
                    if (revenueRatio != null) {
                        cumulativeRevenueRatio = cumulativeRevenueRatio.add(revenueRatio);
                        putDecimal(row, "累计销售额占比", cumulativeRevenueRatio);
                    }
                }
                copy(row, "平均评分", item, "rating", "avgRating");
                copy(row, "平均价格($)", item, "price", "avgPrice");
                rows.add(row);
            }
        }
        return evidence(rows);
    }

    private EvidenceRows reviewEvidence(List<MarketResearchDataset> datasets) {
        List<MarketResearchDataset> reviewDatasets = datasetsByPrefix(
                datasets, ResearchConstants.REVIEWS_DATASET_CODE_PREFIX);
        if (reviewDatasets.isEmpty()) {
            throw new IllegalStateException("缺少评论源数据集: " + ResearchConstants.REVIEWS_DATASET_CODE_PREFIX);
        }
        List<ObjectNode> rows = new ArrayList<>();
        for (MarketResearchDataset dataset : reviewDatasets) {
            String sourceAsin = dataset.getDatasetCode().substring(
                    ResearchConstants.REVIEWS_DATASET_CODE_PREFIX.length());
            for (SourceRecord source : sourceRecords(dataset)) {
                JsonNode item = source.item();
                ObjectNode row = objectMapper.createObjectNode();
                String asin = text(item, "asin");
                row.put("ASIN", StringUtils.hasText(asin) ? asin : sourceAsin);
                copy(row, "作者", item, "author");
                copy(row, "标题", item, "title");
                copy(row, "内容", item, "content");
                putDate(row, "评论时间", first(item, "date"));
                copy(row, "星级", item, "star", "rating");
                copy(row, "VP评论", item, "verified");
                copy(row, "Vine评论", item, "vine");
                copy(row, "型号/SKU", item, "skus", "sku");
                copy(row, "赞同数", item, "likes");
                copy(row, "图片", item, "images");
                copy(row, "视频", item, "videos");
                rows.add(row);
            }
        }
        return evidence(rows);
    }

    private EvidenceRows vocEvidence(List<MarketResearchDataset> datasets) {
        List<MarketResearchDataset> reviewDatasets = datasetsByPrefix(
                datasets, ResearchConstants.REVIEWS_DATASET_CODE_PREFIX);
        if (reviewDatasets.isEmpty()) {
            throw new IllegalStateException("VOC基础证据缺少评论数据集");
        }
        Map<String, VocAggregate> grouped = new LinkedHashMap<>();
        for (MarketResearchDataset dataset : reviewDatasets) {
            String sourceAsin = dataset.getDatasetCode().substring(
                    ResearchConstants.REVIEWS_DATASET_CODE_PREFIX.length());
            for (SourceRecord source : sourceRecords(dataset)) {
                JsonNode item = source.item();
                String asin = text(item, "asin");
                if (!StringUtils.hasText(asin)) {
                    asin = sourceAsin;
                }
                grouped.computeIfAbsent(asin, VocAggregate::new).add(source);
            }
        }
        List<ObjectNode> rows = new ArrayList<>();
        for (VocAggregate aggregate : grouped.values()) {
            ObjectNode row = objectMapper.createObjectNode();
            row.put("ASIN", aggregate.asin);
            row.put("评论样本数", aggregate.count);
            putDecimal(row, "平均星级", aggregate.averageStar());
            row.put("正向样本数", aggregate.positiveCount);
            putDecimal(row, "正向占比", aggregate.sampleRatio(aggregate.positiveCount));
            row.put("中性样本数", aggregate.neutralCount);
            putDecimal(row, "中性占比", aggregate.sampleRatio(aggregate.neutralCount));
            row.put("负向样本数", aggregate.negativeCount);
            putDecimal(row, "负向占比", aggregate.sampleRatio(aggregate.negativeCount));
            putDecimal(row, "VP评论占比", aggregate.sampleRatio(aggregate.verifiedCount));
            putDecimal(row, "带图或视频评论占比", aggregate.sampleRatio(aggregate.mediaCount));
            putDecimal(row, "平均赞同数", aggregate.averageLikes());
            row.put("代表正向评价", aggregate.positiveSample);
            row.put("代表负向评价", aggregate.negativeSample);
            rows.add(row);
        }
        return evidence(rows);
    }

    private EvidenceRows keywordEvidence(List<MarketResearchDataset> datasets) {
        MarketResearchDataset keywords = requireDataset(datasets, ResearchConstants.KEYWORDS_DATASET_CODE);
        List<MarketResearchDataset> sources = new ArrayList<>();
        sources.add(keywords);
        datasets.stream()
                .filter(dataset -> "keywords.miner".equals(dataset.getDatasetCode())
                        || dataset.getDatasetCode().startsWith(
                                ResearchConstants.TRAFFIC_KEYWORDS_DATASET_CODE_PREFIX))
                .forEach(sources::add);
        List<ObjectNode> rows = new ArrayList<>();
        for (MarketResearchDataset source : sources) {
            String code = source.getDatasetCode();
            String type = keywordSourceType(code);
            String sourceAsin = code.startsWith(ResearchConstants.TRAFFIC_KEYWORDS_DATASET_CODE_PREFIX)
                    ? code.substring(ResearchConstants.TRAFFIC_KEYWORDS_DATASET_CODE_PREFIX.length())
                    : "";
            for (SourceRecord sourceRecord : sourceRecords(source)) {
                JsonNode item = sourceRecord.item();
                ObjectNode row = objectMapper.createObjectNode();
                row.put("来源类型", type);
                row.put("关联ASIN", StringUtils.hasText(text(item, "asin")) ? text(item, "asin") : sourceAsin);
                copy(row, "关键词", item, "keywords", "keyword");
                copy(row, "中文翻译", item, "keywordCn", "keywordsCn");
                copy(row, "月份", item, "month", "marketPeriod");
                copy(row, "月搜索量", item, "searches", "monthlySearches", "search");
                copy(row, "点击量", item, "clicks");
                copy(row, "曝光量", item, "impressions");
                copy(row, "购买量", item, "purchases", "purchase");
                copy(row, "购买率", item, "purchaseRate");
                copy(row, "商品数", item, "products");
                copy(row, "供需比", item, "supplyDemandRatio");
                copy(row, "PPC建议竞价", item, "bid");
                copy(row, "PPC最低价", item, "bidMin");
                copy(row, "PPC最高价", item, "bidMax");
                putNode(row, "自然排名", firstNested(item, "rankPosition", "position", "organicRank"));
                putNode(row, "广告排名", firstNested(item, "adPosition", "position", "adRank"));
                copy(row, "标题密度", item, "titleDensityExact");
                copy(row, "搜索量增长率", item, "growth", "searchMonthlyCr");
                rows.add(row);
            }
        }
        return evidence(rows);
    }

    private EvidenceRows asinSalesTrendEvidence(List<MarketResearchDataset> datasets) {
        List<ObjectNode> rows = new ArrayList<>();
        for (MarketResearchDataset dataset : datasetsByPrefix(
                datasets, ResearchConstants.ASIN_SALES_TREND_DATASET_CODE_PREFIX)) {
            String sourceAsin = dataset.getDatasetCode().substring(
                    ResearchConstants.ASIN_SALES_TREND_DATASET_CODE_PREFIX.length());
            JsonNode payload = datasetService.readPayload(dataset);
            JsonNode asin = payload.path("asin");
            String responseAsin = text(asin, "asin");
            JsonNode points = payload.path("salesTrendPoints");
            if (!points.isArray()) {
                continue;
            }
            List<JsonNode> orderedPoints = arrayValues(points).stream()
                    .sorted(Comparator.comparing(point -> normalizeMonth(text(point, "month"))))
                    .toList();
            for (int index = 0; index < orderedPoints.size(); index++) {
                JsonNode point = orderedPoints.get(index);
                ObjectNode row = objectMapper.createObjectNode();
                row.put("ASIN", StringUtils.hasText(responseAsin) ? responseAsin : sourceAsin);
                copy(row, "品牌", asin, "brand");
                copy(row, "标题", asin, "title");
                copy(row, "月份", point, "month");
                copy(row, "父体销量", point, "parentUnitSales");
                copy(row, "子体销量", point, "childUnitSales");
                BigDecimal parentUnits = decimalOrNull(point, "parentUnitSales");
                BigDecimal childUnits = decimalOrNull(point, "childUnitSales");
                if (index > 0) {
                    JsonNode previous = orderedPoints.get(index - 1);
                    putDecimal(row, "父体销量环比", relativeChange(
                            parentUnits, decimalOrNull(previous, "parentUnitSales")));
                    putDecimal(row, "子体销量环比", relativeChange(
                            childUnits, decimalOrNull(previous, "childUnitSales")));
                }
                putDecimal(row, "子体销量贡献率", ratio(childUnits, parentUnits));
                SalesWindowStats windowStats = salesWindowStats(orderedPoints, index);
                if (windowStats != null) {
                    putDecimal(row, "近3月子体月均销量", windowStats.average());
                    putDecimal(row, "近3月子体销量波动率", windowStats.coefficientOfVariation());
                }
                copy(row, "父体销售额($)", point, "parentSalesRevenue");
                copy(row, "子体销售额($)", point, "childSalesRevenue");
                copy(row, "标价($)", point, "price");
                copy(row, "平均价格($)", point, "averagePrice");
                rows.add(row);
            }
        }
        return evidence(rows);
    }

    private EvidenceRows asinOperationTrendEvidence(List<MarketResearchDataset> datasets) {
        List<MetricDefinition> metrics = List.of(
                new MetricDefinition("price", "价格", true),
                new MetricDefinition("dealPrice", "成交价", true),
                new MetricDefinition("buyBox", "黄金购物车价格", true),
                new MetricDefinition("priceList", "划线价格", true),
                new MetricDefinition("bsr", "大类BSR", true),
                new MetricDefinition("reviews", "评分数", true),
                new MetricDefinition("rating", "评分", true),
                new MetricDefinition("sellers", "卖家数", true),
                new MetricDefinition("salesRankReferenceHistory", "排名类目", false),
                new MetricDefinition("buyBoxSellerIdHistory", "黄金购物车卖家", false));
        List<ObjectNode> rows = new ArrayList<>();
        for (MarketResearchDataset dataset : datasetsByPrefix(
                datasets, ResearchConstants.ASIN_KEEPA_TREND_DATASET_CODE_PREFIX)) {
            String sourceAsin = dataset.getDatasetCode().substring(
                    ResearchConstants.ASIN_KEEPA_TREND_DATASET_CODE_PREFIX.length());
            JsonNode payload = datasetService.readPayload(dataset);
            for (MetricDefinition metric : metrics) {
                JsonNode points = payload.path(metric.field());
                if (!points.isArray()) {
                    continue;
                }
                List<JsonNode> orderedPoints = arrayValues(points).stream()
                        .sorted(Comparator.comparingLong(point -> longValue(point, "timePoint")))
                        .toList();
                NumericRange range = metric.numeric() ? numericRange(orderedPoints) : null;
                for (int index = 0; index < orderedPoints.size(); index++) {
                    JsonNode point = orderedPoints.get(index);
                    ObjectNode row = objectMapper.createObjectNode();
                    row.put("ASIN", StringUtils.hasText(text(payload, "asin"))
                            ? text(payload, "asin") : sourceAsin);
                    copy(row, "数据ASIN", payload, "dataAsin");
                    copy(row, "父体ASIN", payload, "parentAsin");
                    copy(row, "品牌", payload, "brand");
                    copy(row, "标题", payload, "title");
                    copy(row, "类目", payload, "nodeLabelPath", "rootCategoryLabel");
                    row.put("指标", metric.label());
                    putDate(row, "时间", first(point, "timePoint"));
                    copy(row, "数值", point, "value");
                    JsonNode previousValue = index == 0
                            ? null : first(orderedPoints.get(index - 1), "value");
                    putNode(row, "前值", previousValue);
                    BigDecimal currentNumeric = metric.numeric() ? decimalOrNull(point, "value") : null;
                    BigDecimal previousNumeric = metric.numeric() ? decimalOrNull(previousValue) : null;
                    if (currentNumeric != null && previousNumeric != null) {
                        putDecimal(row, "变化量", currentNumeric.subtract(previousNumeric));
                        putDecimal(row, "变化率", relativeChange(currentNumeric, previousNumeric));
                    }
                    if (range != null) {
                        putDecimal(row, "区间最小值", range.minimum());
                        putDecimal(row, "区间最大值", range.maximum());
                    }
                    rows.add(row);
                }
            }
        }
        return evidence(rows);
    }

    private ObjectNode envelope(
            ResearchEvidenceCatalog.Definition definition, List<ObjectNode> sourceRows) {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("sheetName", definition.sheetName());
        List<String> resolvedColumns = definition.columns();
        ArrayNode columns = envelope.putArray("columns");
        resolvedColumns.forEach(columns::add);
        ArrayNode items = envelope.putArray("items");
        for (ObjectNode sourceRow : sourceRows) {
            ObjectNode ordered = objectMapper.createObjectNode();
            for (String column : resolvedColumns) {
                JsonNode value = sourceRow.get(column);
                if (value == null || value.isMissingNode()) {
                    ordered.putNull(column);
                } else {
                    ordered.set(column, value.deepCopy());
                }
            }
            items.add(ordered);
        }
        return envelope;
    }

    private void validateEnvelope(
            ResearchEvidenceCatalog.Definition definition,
            JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new IllegalStateException(definition.datasetCode() + "证据信封不是JSON对象");
        }
        if (!definition.sheetName().equals(payload.path("sheetName").asText())) {
            throw new IllegalStateException(definition.datasetCode() + "工作表名称不匹配");
        }
        Set<String> visibleColumns = new LinkedHashSet<>(definition.columns());
        JsonNode columns = payload.get("columns");
        if (columns == null || !columns.isArray()) {
            throw new IllegalStateException(definition.datasetCode() + "列契约不匹配");
        }
        List<String> actualColumns = arrayTexts(columns);
        if (!actualColumns.equals(definition.columns())) {
            throw new IllegalStateException(definition.datasetCode() + "稳定列前缀不匹配");
        }
        if (new LinkedHashSet<>(actualColumns).size() != actualColumns.size()) {
            throw new IllegalStateException(definition.datasetCode() + "包含重复列");
        }
        JsonNode items = payload.get("items");
        if (items == null || !items.isArray()) {
            throw new IllegalStateException(definition.datasetCode() + "缺少证据记录");
        }
        for (JsonNode item : items) {
            if (item == null || !item.isObject()) {
                throw new IllegalStateException(definition.datasetCode() + "包含非对象证据记录");
            }
            for (String column : actualColumns) {
                if (!item.has(column)) {
                    throw new IllegalStateException(definition.datasetCode() + "证据记录缺少列: " + column);
                }
            }
            item.properties().forEach(entry -> {
                if (!visibleColumns.contains(entry.getKey())) {
                    throw new IllegalStateException(definition.datasetCode()
                            + "证据记录包含未声明列: " + entry.getKey());
                }
            });
        }
    }

    private EvidenceRows evidence(List<ObjectNode> rows) {
        return new EvidenceRows(List.copyOf(rows));
    }

    private List<JsonNode> records(MarketResearchDataset dataset) {
        JsonNode payload = datasetService.readPayload(dataset);
        if (payload == null || payload.isNull() || payload.isMissingNode()) {
            return List.of();
        }
        if (payload.isArray()) {
            return arrayValues(payload);
        }
        JsonNode items = payload.get("items");
        if (items != null && items.isArray()) {
            return arrayValues(items);
        }
        return payload.isObject() ? List.of(payload) : List.of();
    }

    private List<SourceRecord> sourceRecords(MarketResearchDataset dataset) {
        JsonNode payload = datasetService.readPayload(dataset);
        if (payload == null || payload.isNull() || payload.isMissingNode()) {
            return List.of();
        }
        List<SourceRecord> records = new ArrayList<>();
        if (payload.isObject()) {
            JsonNode items = payload.get("items");
            if (items != null && items.isArray()) {
                for (int index = 0; index < items.size(); index++) {
                    records.add(new SourceRecord(items.get(index)));
                }
                return List.copyOf(records);
            }
            return List.of(new SourceRecord(payload));
        }
        if (payload.isArray()) {
            for (int index = 0; index < payload.size(); index++) {
                records.add(new SourceRecord(payload.get(index)));
            }
            return List.copyOf(records);
        }
        return List.of(new SourceRecord(payload));
    }

    private List<JsonNode> arrayValues(JsonNode array) {
        List<JsonNode> values = new ArrayList<>();
        array.forEach(values::add);
        return values;
    }

    private List<String> arrayTexts(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add(value.asText()));
        return values;
    }

    private MarketResearchDataset requireDataset(
            List<MarketResearchDataset> datasets, String datasetCode) {
        MarketResearchDataset dataset = findDataset(datasets, datasetCode);
        if (dataset == null) {
            throw new IllegalStateException("缺少数据集: " + datasetCode);
        }
        return dataset;
    }

    private MarketResearchDataset findDataset(
            List<MarketResearchDataset> datasets, String datasetCode) {
        return datasets.stream()
                .filter(dataset -> datasetCode.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElse(null);
    }

    private List<MarketResearchDataset> datasetsByPrefix(
            List<MarketResearchDataset> datasets, String prefix) {
        return datasets.stream()
                .filter(dataset -> dataset.getDatasetCode() != null
                        && dataset.getDatasetCode().startsWith(prefix))
                .sorted(Comparator.comparing(MarketResearchDataset::getDatasetCode))
                .toList();
    }

    private void copy(ObjectNode target, String targetField, JsonNode source, String... sourceFields) {
        putNode(target, targetField, first(source, sourceFields));
    }

    private void copyOrPut(
            ObjectNode target,
            String targetField,
            JsonNode source,
            int fallback,
            String... sourceFields) {
        JsonNode value = first(source, sourceFields);
        if (missing(value)) {
            target.put(targetField, fallback);
        } else {
            putNode(target, targetField, value);
        }
    }

    private void putNode(ObjectNode target, String field, JsonNode value) {
        if (!missing(value)) {
            target.set(field, value.deepCopy());
        }
    }

    private JsonNode first(JsonNode source, String... fields) {
        if (source == null || !source.isObject()) {
            return null;
        }
        for (String field : fields) {
            JsonNode value = source.get(field);
            if (!missing(value) && (!value.isString() || StringUtils.hasText(value.asText()))) {
                return value;
            }
        }
        return null;
    }

    private JsonNode firstNested(
            JsonNode source, String objectField, String nestedField, String fallbackField) {
        JsonNode nested = source == null ? null : source.get(objectField);
        JsonNode value = first(nested, nestedField);
        return missing(value) ? first(source, fallbackField) : value;
    }

    private boolean missing(JsonNode value) {
        return value == null || value.isNull() || value.isMissingNode();
    }

    private String text(JsonNode source, String... fields) {
        JsonNode value = first(source, fields);
        return missing(value) ? "" : value.asText();
    }

    private long longValue(JsonNode source, String... fields) {
        JsonNode value = first(source, fields);
        if (missing(value)) {
            return 0L;
        }
        if (value.isNumber()) {
            return value.longValue();
        }
        try {
            return new BigDecimal(value.asText()).longValue();
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private BigDecimal decimalValue(JsonNode source, String... fields) {
        JsonNode value = first(source, fields);
        if (missing(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return value.isNumber() ? value.decimalValue() : new BigDecimal(value.asText());
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal decimalOrNull(JsonNode source, String... fields) {
        return decimalOrNull(first(source, fields));
    }

    private BigDecimal decimalOrNull(JsonNode value) {
        if (missing(value)) {
            return null;
        }
        try {
            return value.isNumber() ? value.decimalValue() : new BigDecimal(value.asText());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void putDecimal(ObjectNode target, String field, BigDecimal value) {
        if (value != null) {
            target.put(field, value.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP));
        }
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal relativeChange(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null) {
            return null;
        }
        return ratio(current.subtract(previous), previous);
    }

    private String returnRiskLevel(BigDecimal relativeDifference) {
        if (relativeDifference.compareTo(HIGH_RETURN_RISK_THRESHOLD) >= 0) {
            return "高";
        }
        if (relativeDifference.compareTo(LOW_RETURN_RISK_THRESHOLD) <= 0) {
            return "低";
        }
        return "中";
    }

    private String firstArrayText(JsonNode source, String field) {
        JsonNode values = source == null ? null : source.get(field);
        if (values == null || !values.isArray() || values.isEmpty()) {
            return "";
        }
        return values.get(0).asText("");
    }

    private SalesWindowStats salesWindowStats(List<JsonNode> points, int currentIndex) {
        if (currentIndex + 1 < SALES_ROLLING_MONTHS) {
            return null;
        }
        List<BigDecimal> values = new ArrayList<>(SALES_ROLLING_MONTHS);
        for (int index = currentIndex - SALES_ROLLING_MONTHS + 1; index <= currentIndex; index++) {
            BigDecimal value = decimalOrNull(points.get(index), "childUnitSales");
            if (value == null) {
                return null;
            }
            values.add(value);
        }
        BigDecimal average = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(SALES_ROLLING_MONTHS), DECIMAL_SCALE, RoundingMode.HALF_UP);
        if (average.compareTo(BigDecimal.ZERO) == 0) {
            return new SalesWindowStats(average, null);
        }
        BigDecimal variance = values.stream()
                .map(value -> value.subtract(average).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(SALES_ROLLING_MONTHS), 12, RoundingMode.HALF_UP);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        return new SalesWindowStats(average, ratio(standardDeviation, average));
    }

    private NumericRange numericRange(List<JsonNode> points) {
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        for (JsonNode point : points) {
            BigDecimal value = decimalOrNull(point, "value");
            if (value == null) {
                continue;
            }
            minimum = minimum == null || value.compareTo(minimum) < 0 ? value : minimum;
            maximum = maximum == null || value.compareTo(maximum) > 0 ? value : maximum;
        }
        return minimum == null ? null : new NumericRange(minimum, maximum);
    }

    private void putDate(ObjectNode target, String field, JsonNode value) {
        if (missing(value)) {
            return;
        }
        if (value.isIntegralNumber()) {
            target.put(field, Instant.ofEpochMilli(value.longValue())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .format(DATE_FORMATTER));
            return;
        }
        target.put(field, value.asText());
    }

    private String normalizeMonth(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 7 && trimmed.charAt(4) == '-') {
            return trimmed.substring(0, 7);
        }
        if (trimmed.length() >= 6 && trimmed.chars().limit(6).allMatch(Character::isDigit)) {
            return trimmed.substring(0, 4) + "-" + trimmed.substring(4, 6);
        }
        return trimmed;
    }

    private String concentrationDimension(String code) {
        return switch (code) {
            case "market.goods-concentration" -> "商品集中度";
            case "market.brand-concentration" -> "品牌集中度";
            case "market.seller-concentration" -> "卖家集中度";
            case "market.seller-location" -> "卖家地区";
            case "market.seller-type" -> "配送类型";
            case "market.shelf-time" -> "上架时长";
            case "market.ratings" -> "评分数区间";
            case "market.rating" -> "评分区间";
            case "market.price" -> "价格区间";
            case "market.shelf-trend" -> "上架趋势";
            case "market.ebc" -> "A+/视频";
            default -> code;
        };
    }

    private String keywordSourceType(String code) {
        if (ResearchConstants.KEYWORDS_DATASET_CODE.equals(code)) {
            return "关键词研究";
        }
        if ("keywords.miner".equals(code)) {
            return "关键词挖掘";
        }
        return "竞品反查词";
    }

    private record EvidenceRows(List<ObjectNode> rows) {
    }

    private record SourceRecord(JsonNode item) {
    }

    private record MetricDefinition(String field, String label, boolean numeric) {
    }

    private record SalesWindowStats(BigDecimal average, BigDecimal coefficientOfVariation) {
    }

    private record NumericRange(BigDecimal minimum, BigDecimal maximum) {
    }

    private final class BrandAggregate {
        private final String brand;
        private final Set<String> asins = new LinkedHashSet<>();
        private final Set<String> titles = new LinkedHashSet<>();
        private int products;
        private double units;
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal priceTotal = BigDecimal.ZERO;
        private int priceCount;
        private BigDecimal ratingTotal = BigDecimal.ZERO;
        private int ratingCount;
        private BigDecimal ratingsTotal = BigDecimal.ZERO;
        private int ratingsCount;
        private BigDecimal variationsTotal = BigDecimal.ZERO;
        private int variationsCount;
        private int fbaProducts;
        private final Map<String, Integer> sellerNations = new LinkedHashMap<>();

        private BrandAggregate(String brand) {
            this.brand = brand;
        }

        private void add(SourceRecord source) {
            JsonNode product = source.item();
            products++;
            String asin = text(product, "asin");
            if (StringUtils.hasText(asin)) {
                asins.add(asin);
            }
            String title = text(product, "title");
            if (StringUtils.hasText(title)) {
                titles.add(title);
            }
            units += longValue(product, "units", "monthlyUnits", "amzUnit");
            revenue = revenue.add(decimalValue(product, "revenue", "monthlyRevenue", "amzSales"));
            BigDecimal price = decimalValue(product, "price", "averagePrice");
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                priceTotal = priceTotal.add(price);
                priceCount++;
            }
            addAverageValue(product, "rating", value -> {
                ratingTotal = ratingTotal.add(value);
                ratingCount++;
            });
            addAverageValue(product, "ratings", value -> {
                ratingsTotal = ratingsTotal.add(value);
                ratingsCount++;
            });
            addAverageValue(product, "variations", value -> {
                variationsTotal = variationsTotal.add(value);
                variationsCount++;
            });
            if ("FBA".equalsIgnoreCase(text(product, "fulfillment"))) {
                fbaProducts++;
            }
            String sellerNation = text(product, "sellerNation");
            if (StringUtils.hasText(sellerNation)) {
                sellerNations.merge(sellerNation, 1, Integer::sum);
            }
        }

        private double units() {
            return units;
        }

        private BigDecimal revenue() {
            return revenue;
        }

        private String representativeAsin() {
            return asins.stream().findFirst().orElse("");
        }

        private int asinCount() {
            return asins.size();
        }

        private String productLineClues() {
            return String.join(" | ", titles.stream().limit(3).toList());
        }

        private BigDecimal averagePrice() {
            return priceCount == 0
                    ? BigDecimal.ZERO
                    : priceTotal.divide(BigDecimal.valueOf(priceCount), 2, RoundingMode.HALF_UP);
        }

        private BigDecimal averageRating() {
            return average(ratingTotal, ratingCount);
        }

        private BigDecimal averageRatings() {
            return average(ratingsTotal, ratingsCount);
        }

        private BigDecimal averageVariations() {
            return average(variationsTotal, variationsCount);
        }

        private BigDecimal average(BigDecimal total, int count) {
            return count == 0
                    ? BigDecimal.ZERO
                    : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        private String mainSellerNation() {
            return sellerNations.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
        }

        private void addAverageValue(
                JsonNode product,
                String field,
                java.util.function.Consumer<BigDecimal> consumer) {
            JsonNode value = first(product, field);
            if (!missing(value)) {
                consumer.accept(decimalValue(product, field));
            }
        }
    }

    private final class VocAggregate {
        private final String asin;
        private int count;
        private double starTotal;
        private int positiveCount;
        private int neutralCount;
        private int negativeCount;
        private int verifiedCount;
        private int mediaCount;
        private long likesTotal;
        private int starCount;
        private String positiveSample = "";
        private String negativeSample = "";

        private VocAggregate(String asin) {
            this.asin = asin;
        }

        private void add(SourceRecord source) {
            JsonNode review = source.item();
            count++;
            double star = decimalValue(review, "star", "rating").doubleValue();
            if (star > 0D) {
                starTotal += star;
                starCount++;
            }
            String sample = String.join(" | ", List.of(text(review, "title"), text(review, "content"))).trim();
            if (star >= 4D) {
                positiveCount++;
                if (positiveSample.isBlank()) {
                    positiveSample = sample;
                }
            } else if (star == 3D) {
                neutralCount++;
            } else if (star > 0D && star <= 2D) {
                negativeCount++;
                if (negativeSample.isBlank()) {
                    negativeSample = sample;
                }
            }
            if (booleanValue(review, "verified")) {
                verifiedCount++;
            }
            if (hasMedia(review)) {
                mediaCount++;
            }
            likesTotal += longValue(review, "likes");
        }

        private BigDecimal averageStar() {
            return starCount == 0 ? null : BigDecimal.valueOf(starTotal)
                    .divide(BigDecimal.valueOf(starCount), DECIMAL_SCALE, RoundingMode.HALF_UP);
        }

        private BigDecimal sampleRatio(int sampleCount) {
            return ratio(BigDecimal.valueOf(sampleCount), BigDecimal.valueOf(count));
        }

        private BigDecimal averageLikes() {
            return count == 0 ? null : BigDecimal.valueOf(likesTotal)
                    .divide(BigDecimal.valueOf(count), DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
    }

    private boolean booleanValue(JsonNode source, String field) {
        JsonNode value = first(source, field);
        return !missing(value) && (value.isBoolean() ? value.booleanValue() : Boolean.parseBoolean(value.asText()));
    }

    private boolean hasMedia(JsonNode review) {
        JsonNode images = review.get("images");
        JsonNode videos = review.get("videos");
        return (images != null && images.isArray() && !images.isEmpty())
                || (videos != null && videos.isArray() && !videos.isEmpty())
                || booleanValue(review, "image")
                || booleanValue(review, "video");
    }
}
