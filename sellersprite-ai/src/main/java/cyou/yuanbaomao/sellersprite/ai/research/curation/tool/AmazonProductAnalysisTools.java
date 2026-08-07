package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import cyou.yuanbaomao.sellersprite.ai.research.curation.react.SheetAnalysisResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ImageAsset;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheet;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductSheetRow;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.RawSheet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmazonProductAnalysisTools {

    private final AmazonSelectionToolContext toolContext;

    @Tool(
            name = "inspectAmazonWorkbook",
            description = "观察本次市场调研 evidence 工作簿，返回任务文件名、sheet 数、每个 sheet 的字段和行数")
    public String inspectAmazonWorkbook(@ToolParam(description = "会话 ID") String conversationId) {
        var result = toolContext.getRequired(conversationId);
        ProductWorkbook workbook = result.getWorkbook();
        if (result.getSheetAnalyses().isEmpty()) {
            workbook.getSheets().stream()
                    .map(this::analyzeSheet)
                    .forEach(result.getSheetAnalyses()::add);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("文件：")
                .append(workbook.getFileName())
                .append("，共 ")
                .append(workbook.getSheets().size())
                .append(" 个 sheet。\n");
        for (SheetAnalysisResult sheetAnalysis : result.getSheetAnalyses()) {
            builder.append("- ")
                    .append(sheetAnalysis.getSheetName())
                    .append("：结构化行 ")
                    .append(sheetAnalysis.getRowCount())
                    .append("，非空单元格 ")
                    .append(sheetAnalysis.getRawCellCount())
                    .append("，图片 ")
                    .append(sheetAnalysis.getImageAssetCount())
                    .append('\n');
        }
        return builder.toString();
    }

    @Tool(name = "inspectAmazonSheet", description = "按 sheet 名观察业务数据，用于理解不固定结构的多 sheet 市场调研表")
    public String inspectAmazonSheet(@ToolParam(description = "会话 ID") String conversationId,
            @ToolParam(description = "sheet 名称") String sheetName) {
        ProductSheet sheet = findSheet(toolContext.getRequired(conversationId).getWorkbook(), sheetName);
        StringBuilder builder = new StringBuilder();
        builder.append("Sheet「")
                .append(sheet.getSheetName())
                .append("」")
                .append('\n');
        List<String> observationHeaders = sheet.getHeaders();
        if (!observationHeaders.isEmpty()) {
            builder.append("可选结构化字段：")
                    .append(String.join("、", observationHeaders))
                    .append('\n');
        }
        builder.append("业务数据行：\n");
        for (ProductSheetRow row : sheet.getRows()) {
            String cells = formatCells(row);
            if (cells.isBlank()) {
                continue;
            }
            builder.append("第 ")
                    .append(row.getRowIndex())
                    .append(" 行：")
                    .append(cells)
                    .append('\n');
        }
        appendImageAssets(builder, sheet.getRawSheet());
        return builder.toString();
    }

    private SheetAnalysisResult analyzeSheet(ProductSheet sheet) {
        String observation = "Sheet「" + sheet.getSheetName() + "」原始提取结果："
                + rawSheetSummary(sheet.getRawSheet()) + "；可选结构化行 " + sheet.getRows().size() + " 行";
        String action = "把原始 Sheet 文本、结构化字段和图片资产元数据交给模型判断趋势、品牌集中度、VOC、关键词或商品信号。";
        return SheetAnalysisResult.builder()
                .sheetName(sheet.getSheetName())
                .sheetIndex(sheet.getSheetIndex())
                .rowCount(sheet.getRows().size())
                .headers(sheet.getHeaders())
                .rawCellCount(rawCellCount(sheet.getRawSheet()))
                .imageAssetCount(imageAssetCount(sheet.getRawSheet()))
                .observation(observation)
                .action(action)
                .summary(observation + "；" + action)
                .build();
    }

    private ProductSheet findSheet(ProductWorkbook workbook, String sheetName) {
        return workbook.getSheets().stream()
                .filter(sheet -> sheet.getSheetName().equalsIgnoreCase(sheetName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到 sheet：" + sheetName));
    }

    private String formatCells(ProductSheetRow row) {
        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : row.getCells().entrySet()) {
            pairs.add(entry.getKey() + "=" + entry.getValue());
        }
        return String.join("；", pairs);
    }

    private void appendImageAssets(StringBuilder builder, RawSheet rawSheet) {
        if (rawSheet == null || rawSheet.getImageAssets().isEmpty()) {
            return;
        }
        builder.append("图片资产：\n");
        for (ImageAsset imageAsset : rawSheet.getImageAssets()) {
            builder.append("- sourceType=")
                    .append(imageAsset.getSourceType())
                    .append(", cell=")
                    .append(imageAsset.getCellAddress())
                    .append(", mimeType=")
                    .append(nullToEmpty(imageAsset.getMimeType()))
                    .append(", reference=")
                    .append(nullToEmpty(imageAsset.getReference()))
                    .append(", formulaId=")
                    .append(nullToEmpty(imageAsset.getFormulaId()))
                    .append('\n');
        }
    }

    private String rawSheetSummary(RawSheet rawSheet) {
        if (rawSheet == null) {
            return "";
        }
        return "；原始非空单元格 " + rawCellCount(rawSheet) + " 个，图片资产 " + imageAssetCount(rawSheet) + " 个";
    }

    private int rawCellCount(RawSheet rawSheet) {
        return rawSheet == null ? 0 : rawSheet.getRawCells().size();
    }

    private int imageAssetCount(RawSheet rawSheet) {
        return rawSheet == null ? 0 : rawSheet.getImageAssets().size();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
