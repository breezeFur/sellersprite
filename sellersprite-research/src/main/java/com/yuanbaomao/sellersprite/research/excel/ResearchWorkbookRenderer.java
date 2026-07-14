package com.yuanbaomao.sellersprite.research.excel;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.research.config.ResearchProperties;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.service.ResearchSnapshotService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * 将数据库快照确定性写入 11 个模板页，并追加 5 个原始全字段页。
 */
@Component
@RequiredArgsConstructor
public class ResearchWorkbookRenderer {

    private static final int MAX_PRODUCTS = 100;
    private static final int MAX_CONCENTRATION_PRODUCTS = 20;
    private static final int MAX_KEYWORDS = 50;
    private static final int MAX_SEGMENTS = 25;
    private static final int MAX_REVIEWS = 8;
    private static final int MAX_BRANDS = 6;
    private static final int REVIEW_HEADER_ROW_INDEX = 32;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ResearchProperties properties;
    private final ResourceLoader resourceLoader;
    private final ResearchSnapshotService snapshotService;
    private final ResearchWorkbookValidator workbookValidator;
    private final ResearchRawWorkbookWriter rawWorkbookWriter;

    public void render(MarketResearchJob job, Path target) throws IOException {
        Resource template = resourceLoader.getResource(properties.getTemplateLocation());
        if (!template.exists()) {
            throw new IllegalStateException("市场调研Excel模板不存在: " + properties.getTemplateLocation());
        }
        Path writing = target.resolveSibling(target.getFileName() + ".writing");
        Files.deleteIfExists(writing);
        try (InputStream inputStream = template.getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            validateTemplate(workbook);
            List<MarketResearchSnapshot> snapshots = snapshotService.listByJobId(job.getJobId());
            List<JsonNode> products = items(snapshotByKey(snapshots, "products"));
            List<JsonNode> keywords = items(snapshotByKey(snapshots, "keywords"));
            List<ReviewRow> reviews = reviewRows(snapshots);

            writeSummary(workbook, job, snapshots, products, keywords, reviews);
            writeProducts(workbook.getSheet("US"), products, MAX_PRODUCTS);
            writeNote(workbook.getSheet("行业销售趋势"), 1, "当前数据源未返回月度行业趋势，模板区域已保留。 ");
            writeNote(workbook.getSheet("行业需求及趋势"), 1, "当前数据源未返回月度关键词趋势，模板区域已保留。 ");
            writeKeywords(workbook.getSheet("细分市场现状"), keywords, MAX_SEGMENTS);
            writeNote(workbook.getSheet("细分市场退货率"), 1, "第一版不启用AI归因，请结合原始评论人工补充。 ");
            writeBrands(workbook.getSheet("竞品品牌"), products);
            writeProducts(workbook.getSheet("商品集中度"), products, MAX_CONCENTRATION_PRODUCTS);
            writeReviews(workbook.getSheet("评价"), reviews);
            writeNote(workbook.getSheet("VOC"), 0, "第一版未启用AI，VOC分析区域暂不自动生成。 ");
            writeKeywords(workbook.getSheet("keywords"), keywords, MAX_KEYWORDS);
            rawWorkbookWriter.append(workbook, snapshots, snapshotService::readPayload);
            workbook.setActiveSheet(0);

            Files.createDirectories(target.getParent());
            try (OutputStream outputStream = Files.newOutputStream(writing)) {
                workbook.write(outputStream);
            }
        } catch (Exception exception) {
            Files.deleteIfExists(writing);
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("生成市场调研Excel失败", exception);
        }
        moveAtomically(writing, target);
        workbookValidator.validate(target);
    }

    private void writeSummary(
            Workbook workbook,
            MarketResearchJob job,
            List<MarketResearchSnapshot> snapshots,
            List<JsonNode> products,
            List<JsonNode> keywords,
            List<ReviewRow> reviews) {
        Sheet sheet = workbook.getSheet("市场调研总结");
        CellStyle titleStyle = titleStyle(workbook);
        CellStyle labelStyle = labelStyle(workbook);
        Row titleRow = row(sheet, 0);
        Cell title = cell(titleRow, 0);
        title.setCellValue(safeText(job.getReportName()));
        title.setCellStyle(titleStyle);
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 36 * 256);

        writeSummaryRow(sheet, 2, "任务ID", job.getJobId(), labelStyle);
        writeSummaryRow(sheet, 3, "站点", job.getMarketplace(), labelStyle);
        writeSummaryRow(sheet, 4, "核心关键词", job.getKeyword(), labelStyle);
        writeSummaryRow(sheet, 5, "数据源", job.getDataSourceMode(), labelStyle);
        writeSummaryRow(sheet, 6, "商品数", products.size(), labelStyle);
        writeSummaryRow(sheet, 7, "关键词数", keywords.size(), labelStyle);
        writeSummaryRow(sheet, 8, "评论数", reviews.size(), labelStyle);
        writeSummaryRow(sheet, 9, "快照数", snapshots.size(), labelStyle);
        writeSummaryRow(sheet, 10, "模板版本", ResearchConstants.TEMPLATE_CODE, labelStyle);
        writeSummaryRow(sheet, 12, "阶段说明", "第一版为确定性工作流，未启用AI分析。", labelStyle);
    }

    private void writeSummaryRow(Sheet sheet, int rowIndex, String label, Object value, CellStyle style) {
        Row row = row(sheet, rowIndex);
        Cell labelCell = cell(row, 0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        writeCell(cell(row, 1), value);
    }

    private void writeProducts(Sheet sheet, List<JsonNode> products, int limit) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalStateException("工作表缺少标题行: " + sheet.getSheetName());
        }
        int count = Math.min(products.size(), limit);
        for (int rowIndex = 0; rowIndex < count; rowIndex++) {
            Row row = row(sheet, rowIndex + 1);
            JsonNode product = products.get(rowIndex);
            for (int columnIndex = 0; columnIndex < header.getLastCellNum(); columnIndex++) {
                String heading = text(header.getCell(columnIndex));
                Object value = productValue(heading, product);
                if (value != null) {
                    writeCell(cell(row, columnIndex), value);
                }
            }
        }
    }

    private void writeKeywords(Sheet sheet, List<JsonNode> keywords, int limit) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalStateException("工作表缺少标题行: " + sheet.getSheetName());
        }
        int count = Math.min(keywords.size(), limit);
        for (int rowIndex = 0; rowIndex < count; rowIndex++) {
            Row row = row(sheet, rowIndex + 1);
            JsonNode keyword = keywords.get(rowIndex);
            for (int columnIndex = 0; columnIndex < header.getLastCellNum(); columnIndex++) {
                Object value = keywordValue(text(header.getCell(columnIndex)), keyword);
                if (value != null) {
                    writeCell(cell(row, columnIndex), value);
                }
            }
        }
    }

    private void writeReviews(Sheet sheet, List<ReviewRow> reviews) {
        Row header = sheet.getRow(REVIEW_HEADER_ROW_INDEX);
        if (header == null) {
            throw new IllegalStateException("评价工作表缺少第33行标题");
        }
        int count = Math.min(reviews.size(), MAX_REVIEWS);
        for (int rowIndex = 0; rowIndex < count; rowIndex++) {
            Row row = row(sheet, REVIEW_HEADER_ROW_INDEX + rowIndex + 1);
            ReviewRow review = reviews.get(rowIndex);
            for (int columnIndex = 0; columnIndex < header.getLastCellNum(); columnIndex++) {
                Object value = reviewValue(text(header.getCell(columnIndex)), review);
                if (value != null) {
                    writeCell(cell(row, columnIndex), value);
                }
            }
        }
        writeNote(sheet, 0, "原始评论明细从第34行开始；第一版未启用AI情感分析。 ");
    }

    private void writeBrands(Sheet sheet, List<JsonNode> products) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalStateException("竞品品牌工作表缺少标题行");
        }
        Map<String, BrandAggregate> grouped = new LinkedHashMap<>();
        for (JsonNode product : products) {
            String brand = stringValue(product, "brand");
            if (brand == null || brand.isBlank()) {
                brand = "未知品牌";
            }
            grouped.computeIfAbsent(brand, BrandAggregate::new).add(product);
        }
        double totalUnits = grouped.values().stream().mapToDouble(BrandAggregate::units).sum();
        List<BrandAggregate> brands = grouped.values().stream()
                .sorted(Comparator.comparingDouble(BrandAggregate::units).reversed())
                .limit(MAX_BRANDS)
                .toList();
        for (int rowIndex = 0; rowIndex < brands.size(); rowIndex++) {
            Row row = row(sheet, rowIndex + 1);
            BrandAggregate brand = brands.get(rowIndex);
            for (int columnIndex = 0; columnIndex < header.getLastCellNum(); columnIndex++) {
                Object value = brandValue(text(header.getCell(columnIndex)), brand, totalUnits);
                if (value != null) {
                    writeCell(cell(row, columnIndex), value);
                }
            }
        }
    }

    private Object productValue(String heading, JsonNode product) {
        String key = normalize(heading);
        if (key.contains("ASIN")) {
            return value(product, "asin");
        }
        if (key.contains("图片")) {
            return value(product, "imageUrl");
        }
        if (key.contains("品牌")) {
            return value(product, "brand");
        }
        if (key.contains("标题")) {
            return value(product, "title");
        }
        if (key.contains("类目")) {
            return firstValue(product, "nodeLabelPath", "category");
        }
        if (key.contains("BSR增长率")) {
            return value(product, "bsrCr");
        }
        if (key.contains("BSR增长")) {
            return value(product, "bsrCv");
        }
        if (key.contains("BSR")) {
            return value(product, "bsr");
        }
        if (key.contains("销售额") && key.contains("增长率")) {
            return null;
        }
        if (key.contains("销量") && key.contains("增长率")) {
            return value(product, "unitsGr");
        }
        if (key.contains("销售额")) {
            return firstValue(product, "revenue", "monthlyRevenue");
        }
        if (key.contains("销量")) {
            return firstValue(product, "units", "monthlyUnits", "amzUnit");
        }
        if (key.contains("价格")) {
            return firstValue(product, "price", "averagePrice");
        }
        if (key.contains("毛利")) {
            return value(product, "profit");
        }
        if (key.contains("月新增评分") || key.contains("月新增评论")) {
            return value(product, "ratingsCv");
        }
        if (key.contains("评分数") || key.contains("评论数")) {
            return value(product, "ratings");
        }
        if (key.contains("留评率")) {
            return value(product, "ratingsRate");
        }
        if (key.contains("评分") || key.contains("星级")) {
            return value(product, "rating");
        }
        if (key.contains("变体")) {
            return value(product, "variations");
        }
        if (key.equals("FBA") || key.contains("FBA费用") || key.contains("FBA运费")) {
            return value(product, "fba");
        }
        if (key.contains("卖家数")) {
            return value(product, "sellers");
        }
        if (key.contains("卖家名称")) {
            return value(product, "sellerName");
        }
        if (key.contains("卖家所属地") || key.contains("卖家国家")) {
            return value(product, "sellerNation");
        }
        if (key.contains("配送")) {
            return value(product, "fulfillment");
        }
        if (key.contains("上架")) {
            return dateValue(product.get("availableDate"));
        }
        if (key.contains("包装重量")) {
            return value(product, "pkgWeight");
        }
        if (key.contains("重量")) {
            return value(product, "weight");
        }
        if (key.contains("包装尺寸")) {
            return value(product, "pkgDimensions");
        }
        if (key.contains("尺寸")) {
            return value(product, "dimension");
        }
        if (key.contains("父体")) {
            return value(product, "parent");
        }
        if (key.contains("SKU")) {
            return value(product, "sku");
        }
        return null;
    }

    private Object keywordValue(String heading, JsonNode keyword) {
        String key = normalize(heading);
        if (key.contains("翻译")) {
            return null;
        }
        if (key.contains("关键词") || key.contains("流量词")) {
            return firstValue(keyword, "keywords", "keyword");
        }
        if (key.contains("月搜索量") || key.equals("搜索量")) {
            return firstValue(keyword, "searches", "monthlySearches");
        }
        if (key.contains("商品数")) {
            return value(keyword, "products");
        }
        if (key.contains("购买率")) {
            return value(keyword, "purchaseRate");
        }
        if (key.contains("购买量")) {
            return value(keyword, "purchases");
        }
        if (key.contains("点击量")) {
            return value(keyword, "clicks");
        }
        if (key.contains("曝光")) {
            return value(keyword, "impressions");
        }
        if (key.contains("供需比")) {
            return value(keyword, "supplyDemandRatio");
        }
        if (key.contains("建议竞价")) {
            return value(keyword, "bid");
        }
        if (key.contains("PPC")) {
            return firstValue(keyword, "bidMin", "bid");
        }
        if (key.contains("标题密度")) {
            return value(keyword, "titleDensityExact");
        }
        if (key.contains("点击占比")) {
            return value(keyword, "araClickRate");
        }
        if (key.contains("转化占比")) {
            return value(keyword, "araShareRate");
        }
        return null;
    }

    private Object reviewValue(String heading, ReviewRow review) {
        String key = normalize(heading);
        JsonNode item = review.item();
        if (key.contains("ASIN")) {
            return firstNonNull(value(item, "asin"), review.asin());
        }
        if (key.contains("标题") && !key.contains("翻译")) {
            return value(item, "title");
        }
        if (key.contains("内容") && !key.contains("翻译")) {
            return value(item, "content");
        }
        if (key.contains("VP") || key.contains("VERIFIED")) {
            return value(item, "verified");
        }
        if (key.contains("VINE")) {
            return value(item, "vine");
        }
        if (key.contains("型号") || key.contains("SKU")) {
            return value(item, "skus");
        }
        if (key.contains("星级") || key.contains("评分")) {
            return firstValue(item, "star", "rating");
        }
        if (key.contains("赞同") || key.contains("点赞")) {
            return value(item, "likes");
        }
        return null;
    }

    private Object brandValue(String heading, BrandAggregate brand, double totalUnits) {
        String key = normalize(heading);
        if (key.contains("品牌")) {
            return brand.brand();
        }
        if (key.contains("销售额")) {
            return brand.revenue();
        }
        if (key.contains("销量")) {
            return brand.units();
        }
        if (key.contains("均价") || key.contains("平均价格")) {
            return brand.averagePrice();
        }
        if (key.contains("市场份额")) {
            return totalUnits == 0 ? 0D : brand.units() / totalUnits;
        }
        if (key.contains("产品线")) {
            return brand.count();
        }
        if (key.contains("ASIN")) {
            return brand.asins().stream().findFirst().orElse("");
        }
        if (key.contains("定价")) {
            return brand.priceRange();
        }
        if (key.contains("用户定位")) {
            return "待人工补充";
        }
        return null;
    }

    private MarketResearchSnapshot snapshotByKey(
            List<MarketResearchSnapshot> snapshots, String businessKey) {
        return snapshots.stream()
                .filter(snapshot -> businessKey.equals(snapshot.getBusinessKey()))
                .findFirst()
                .orElse(null);
    }

    private List<JsonNode> items(MarketResearchSnapshot snapshot) {
        if (snapshot == null) {
            return List.of();
        }
        JsonNode payload = snapshotService.readPayload(snapshot);
        JsonNode items = payload.get("items");
        if (items == null || !items.isArray()) {
            return List.of();
        }
        List<JsonNode> result = new ArrayList<>();
        items.forEach(result::add);
        return result;
    }

    private List<ReviewRow> reviewRows(List<MarketResearchSnapshot> snapshots) {
        List<ReviewRow> result = new ArrayList<>();
        snapshots.stream()
                .filter(snapshot -> snapshot.getBusinessKey() != null
                        && snapshot.getBusinessKey().startsWith("reviews"))
                .forEach(snapshot -> {
                    JsonNode payload = snapshotService.readPayload(snapshot);
                    JsonNode items = payload.get("items");
                    if (items == null && payload.isArray()) {
                        items = payload;
                    }
                    if (items != null && items.isArray()) {
                        String asin = snapshot.getBusinessKey().startsWith("reviews.")
                                ? snapshot.getBusinessKey().substring("reviews.".length())
                                : "";
                        items.forEach(item -> result.add(new ReviewRow(asin, item)));
                    }
                });
        return result;
    }

    private void validateTemplate(Workbook workbook) {
        if (workbook.getNumberOfSheets() != ResearchConstants.TEMPLATE_SHEETS.size()) {
            throw new IllegalStateException("市场调研模板页签数量不正确");
        }
        for (int index = 0; index < ResearchConstants.TEMPLATE_SHEETS.size(); index++) {
            String expected = ResearchConstants.TEMPLATE_SHEETS.get(index);
            if (!expected.equals(workbook.getSheetName(index))) {
                throw new IllegalStateException("市场调研模板页签顺序错误，期望: " + expected);
            }
        }
    }

    private void writeNote(Sheet sheet, int rowIndex, String message) {
        writeCell(cell(row(sheet, rowIndex), 0), message.trim());
    }

    private void writeCell(Cell cell, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }
        cell.setCellValue(safeText(String.valueOf(value)));
    }

    private Object firstValue(JsonNode node, String... names) {
        for (String name : names) {
            Object value = value(node, name);
            if (value != null && !Objects.equals(value, "")) {
                return value;
            }
        }
        return null;
    }

    private Object value(JsonNode node, String name) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(name);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isIntegralNumber()) {
            return value.longValue();
        }
        if (value.isFloatingPointNumber()) {
            return value.doubleValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isArray()) {
            List<String> texts = new ArrayList<>();
            value.forEach(item -> texts.add(item.isString() ? item.stringValue() : item.toString()));
            return String.join(", ", texts);
        }
        return value.isString() ? value.stringValue() : value.toString();
    }

    private String stringValue(JsonNode node, String name) {
        Object value = value(node, name);
        return value == null ? null : String.valueOf(value);
    }

    private Object dateValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isNumber()) {
            return value.isString() ? value.stringValue() : value.toString();
        }
        long timestamp = value.longValue();
        if (timestamp > 0 && timestamp < 100_000_000_000L) {
            timestamp *= 1000;
        }
        return DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private String text(Cell cell) {
        return cell == null ? "" : cell.toString();
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.replaceAll("[\\s\\r\\n()（）%]", "").toUpperCase(Locale.ROOT);
    }

    private String safeText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' ? "'" + value : value;
    }

    private Object firstNonNull(Object first, Object second) {
        return first == null ? second : first;
    }

    private Row row(Sheet sheet, int index) {
        Row row = sheet.getRow(index);
        return row == null ? sheet.createRow(index) : row;
    }

    private Cell cell(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? row.createCell(index) : cell;
    }

    private CellStyle titleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle labelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record ReviewRow(String asin, JsonNode item) {
    }

    private static final class BrandAggregate {

        private final String brand;
        private final List<String> asins = new ArrayList<>();
        private double units;
        private double revenue;
        private double priceSum;
        private int priceCount;

        private BrandAggregate(String brand) {
            this.brand = brand;
        }

        private void add(JsonNode product) {
            addAsin(product.get("asin"));
            units += number(product, "units", "monthlyUnits", "amzUnit");
            revenue += number(product, "revenue", "monthlyRevenue");
            double price = number(product, "price", "averagePrice");
            if (price > 0) {
                priceSum += price;
                priceCount++;
            }
        }

        private void addAsin(JsonNode asin) {
            if (asin != null && asin.isString() && !asin.stringValue().isBlank()) {
                asins.add(asin.stringValue());
            }
        }

        private String brand() {
            return brand;
        }

        private List<String> asins() {
            return asins;
        }

        private int count() {
            return asins.size();
        }

        private double units() {
            return units;
        }

        private double revenue() {
            return revenue;
        }

        private double averagePrice() {
            return priceCount == 0 ? 0D : priceSum / priceCount;
        }

        private String priceRange() {
            return priceCount == 0 ? "" : String.format(Locale.ROOT, "%.2f", averagePrice());
        }

        private static double number(JsonNode product, String... fields) {
            for (String field : fields) {
                JsonNode value = product.get(field);
                if (value != null && value.isNumber()) {
                    return value.doubleValue();
                }
            }
            return 0D;
        }
    }
}
