package cyou.yuanbaomao.sellersprite.research.excel;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** 将阶段一持久化逐表分析和整体总结追加为第八张工作表。 */
@Component
@RequiredArgsConstructor
public class ResearchScreeningSummaryWorkbookAppender {

    public static final String SUMMARY_SHEET_NAME = "阶段一总结";
    public static final String SHEET_COLUMN = "证据表";
    public static final String CONCLUSION_COLUMN = "AI分析结论";

    private final MarketResearchJobDao jobDao;
    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final MarketResearchEventDao eventDao;
    private final ObjectMapper objectMapper;

    public void append(XSSFWorkbook workbook, String jobId) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new IllegalStateException("市场调研任务不存在: " + jobId);
        }
        MarketResearchAnalysisRun run = analysisRunDao
                .findLatestByJobIdAndUserIdAndRunType(
                        jobId, job.getUserId(), ResearchStageCode.SCREENING.name())
                .filter(value -> ResearchAnalysisRunStatus.SUCCEEDED.name()
                        .equals(value.getRunStatus()))
                .orElseThrow(() -> new IllegalStateException("阶段一AI分析尚未完成: " + jobId));
        Map<String, String> conclusions = sheetConclusions(jobId, run.getAnalysisRunId());
        Sheet sheet = workbook.createSheet(SUMMARY_SHEET_NAME);
        CellStyle headerStyle = headerStyle(workbook);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(SHEET_COLUMN);
        header.getCell(0).setCellStyle(headerStyle);
        header.createCell(1).setCellValue(CONCLUSION_COLUMN);
        header.getCell(1).setCellStyle(headerStyle);

        int rowIndex = 1;
        for (ResearchEvidenceCatalog.Definition definition
                : ResearchEvidenceCatalog.SCREENING_DEFINITIONS) {
            String conclusion = conclusions.get(definition.sheetName());
            if (conclusion == null || conclusion.isBlank()) {
                throw new IllegalStateException(
                        "阶段一缺少逐表AI结论: " + definition.sheetName());
            }
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(definition.sheetName());
            row.createCell(1).setCellValue(conclusion);
        }
        if (run.getFinalSummary() == null || run.getFinalSummary().isBlank()) {
            throw new IllegalStateException("阶段一缺少整体AI总结: " + jobId);
        }
        Row summary = sheet.createRow(rowIndex);
        summary.createCell(0).setCellValue("阶段一总体结论");
        summary.createCell(1).setCellValue(run.getFinalSummary());
        sheet.createFreezePane(0, 1);
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 100 * 256);
    }

    private Map<String, String> sheetConclusions(String jobId, String analysisRunId) {
        Map<String, String> conclusions = new LinkedHashMap<>();
        for (MarketResearchEvent event : eventDao.listByJobIdAfterSequence(jobId, 0L)) {
            if (!analysisRunId.equals(event.getAnalysisRunId())
                    || !ResearchEventTypes.SHEET_THINK.equals(event.getEventType())
                    || event.getSheetName() == null) {
                continue;
            }
            conclusions.put(event.getSheetName(), payloadData(event));
        }
        return conclusions;
    }

    private String payloadData(MarketResearchEvent event) {
        try {
            JsonNode data = objectMapper.readTree(event.getPayload()).path("data");
            return data.isTextual() ? data.asText() : data.toString();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("阶段一AI事件载荷损坏: " + event.getEventId(), exception);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }
}
