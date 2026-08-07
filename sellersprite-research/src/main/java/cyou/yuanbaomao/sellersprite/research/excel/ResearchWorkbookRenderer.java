package cyou.yuanbaomao.sellersprite.research.excel;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** 从持久化证据数据集生成指定阶段的正式工作簿。 */
@Component
@RequiredArgsConstructor
public class ResearchWorkbookRenderer {

    private final ResearchDatasetService datasetService;
    private final ResearchWorkbookValidator workbookValidator;
    private final ResearchEvidenceWorkbookWriter evidenceWorkbookWriter;
    private final ResearchScreeningSummaryWorkbookAppender screeningSummaryAppender;

    public void render(MarketResearchJob job, Path target) throws IOException {
        render(job, target, ResearchEvidenceCatalog.DEFINITIONS);
        workbookValidator.validate(target);
    }

    public void render(MarketResearchJob job, Path target, EvidenceStage stage) throws IOException {
        render(job, target, ResearchEvidenceCatalog.definitions(stage));
        workbookValidator.validate(target, stage);
    }

    private void render(
            MarketResearchJob job,
            Path target,
            List<ResearchEvidenceCatalog.Definition> definitions) throws IOException {
        Path writing = target.resolveSibling(target.getFileName() + ".writing");
        Files.deleteIfExists(writing);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            List<MarketResearchDataset> datasets = datasetService.listByJobId(job.getJobId());
            for (ResearchEvidenceCatalog.Definition definition : definitions) {
                MarketResearchDataset dataset = requireDataset(datasets, definition.datasetCode());
                JsonNode payload = datasetService.readPayload(dataset);
                evidenceWorkbookWriter.writeEvidenceTable(
                        workbook,
                        definition,
                        columns(payload, definition.datasetCode()),
                        items(payload, definition.datasetCode()));
            }
            if (definitions.equals(ResearchEvidenceCatalog.SCREENING_DEFINITIONS)) {
                screeningSummaryAppender.append(workbook, job.getJobId());
            }
            workbook.setActiveSheet(0);
            workbook.setForceFormulaRecalculation(true);
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            try (OutputStream outputStream = Files.newOutputStream(writing)) {
                workbook.write(outputStream);
            }
        } catch (Exception exception) {
            Files.deleteIfExists(writing);
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("生成市场调研证据Excel失败", exception);
        }
        moveAtomically(writing, target);
    }

    private MarketResearchDataset requireDataset(
            List<MarketResearchDataset> datasets, String datasetCode) {
        return datasets.stream()
                .filter(dataset -> datasetCode.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("生成Excel缺少证据数据集: " + datasetCode));
    }

    private List<JsonNode> items(JsonNode payload, String datasetCode) {
        JsonNode items = payload == null ? null : payload.get("items");
        if (items == null || !items.isArray()) {
            throw new IllegalStateException("证据数据集缺少可写入记录: " + datasetCode);
        }
        List<JsonNode> records = new ArrayList<>();
        items.forEach(records::add);
        return records;
    }

    private List<String> columns(JsonNode payload, String datasetCode) {
        JsonNode columns = payload == null ? null : payload.get("columns");
        if (columns == null || !columns.isArray() || columns.isEmpty()) {
            throw new IllegalStateException("证据数据集缺少可写入表头: " + datasetCode);
        }
        List<String> result = new ArrayList<>();
        columns.forEach(column -> {
            if (!column.isTextual() || column.asText().isBlank()) {
                throw new IllegalStateException("证据数据集包含无效表头: " + datasetCode);
            }
            result.add(column.asText());
        });
        return List.copyOf(result);
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
