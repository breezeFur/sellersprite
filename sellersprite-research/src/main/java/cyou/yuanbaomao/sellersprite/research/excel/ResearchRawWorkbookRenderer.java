package cyou.yuanbaomao.sellersprite.research.excel;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.raw.ResearchRawDatasetStagePolicy;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** 把持久化的阶段采集响应直接渲染为多 Sheet 原始工作簿。 */
@Component
@RequiredArgsConstructor
public class ResearchRawWorkbookRenderer {

    private final ResearchDatasetService datasetService;
    private final ResearchRawWorkbookWriter workbookWriter;

    public void render(MarketResearchJob job, Path target) throws IOException {
        render(job, target, collectionDatasets(job.getJobId()));
    }

    public void render(MarketResearchJob job, Path target, EvidenceStage stage) throws IOException {
        render(job, target, collectionDatasets(job.getJobId(), stage));
    }

    private void render(
            MarketResearchJob job, Path target, List<MarketResearchDataset> datasets) throws IOException {
        if (datasets.isEmpty()) {
            throw new IllegalStateException("原始工作簿没有可写入的采集数据集: " + job.getJobId());
        }
        Path writing = target.resolveSibling(target.getFileName() + ".writing");
        Files.deleteIfExists(writing);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbookWriter.append(workbook, datasets, datasetService::readPayload);
            if (workbook.getNumberOfSheets() == 0) {
                workbook.createSheet("empty");
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            try (OutputStream outputStream = Files.newOutputStream(writing)) {
                workbook.write(outputStream);
            }
        } catch (RuntimeException exception) {
            Files.deleteIfExists(writing);
            throw exception;
        }
        moveAtomically(writing, target);
        validate(target);
    }

    public void validate(Path workbookPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(workbookPath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalStateException("原始工作簿不包含Sheet");
            }
        }
    }

    private List<MarketResearchDataset> collectionDatasets(String jobId) {
        return datasetService.listByJobId(jobId).stream()
                .filter(dataset -> ResearchRawDatasetStagePolicy.isCollectionNode(dataset.getNodeCode()))
                .toList();
    }

    private List<MarketResearchDataset> collectionDatasets(
            String jobId, EvidenceStage stage) {
        ResearchStageCode stageCode = switch (stage) {
            case SCREENING -> ResearchStageCode.SCREENING;
            case DEEP_DIVE -> ResearchStageCode.DEEP_DIVE;
        };
        return datasetService.listByJobId(jobId).stream()
                .filter(dataset -> ResearchRawDatasetStagePolicy.allows(stageCode, dataset.getNodeCode()))
                .toList();
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
