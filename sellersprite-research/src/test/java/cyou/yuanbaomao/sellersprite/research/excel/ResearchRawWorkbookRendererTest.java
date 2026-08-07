package cyou.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.service.ResearchDatasetService;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ResearchRawWorkbookRendererTest {

    private static final String JOB_ID = "job-raw-stage-001";

    @Mock
    private ResearchDatasetService datasetService;

    @TempDir
    private Path temporaryDirectory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRenderOnlyScreeningCollectionNodes() throws Exception {
        MarketResearchDataset products = dataset(
                "products", "PRODUCT_RESEARCH", ResearchPhase.COLLECT_PRODUCTS);
        MarketResearchDataset reviews = dataset(
                "reviews.B0TEST0001", "REVIEW_LIST", ResearchPhase.COLLECT_REVIEWS);
        MarketResearchDataset quota = dataset(
                "quota.visits", "ACCOUNT_VISITS", ResearchPhase.CHECK_QUOTA);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(products, reviews, quota));
        when(datasetService.readPayload(products)).thenReturn(objectMapper.readTree(
                "{\"items\":[{\"asin\":\"B0TEST0001\"}]}"));
        Path target = temporaryDirectory.resolve("screening-raw.xlsx");

        renderer().render(job(), target, EvidenceStage.SCREENING);

        assertThat(sheetNames(target)).containsExactly("PRODUCT_RESEARCH");
        verify(datasetService, never()).readPayload(reviews);
        verify(datasetService, never()).readPayload(quota);
    }

    @Test
    void shouldRenderOnlyDeepDiveCollectionNodes() throws Exception {
        MarketResearchDataset products = dataset(
                "products", "PRODUCT_RESEARCH", ResearchPhase.COLLECT_PRODUCTS);
        MarketResearchDataset reviews = dataset(
                "reviews.B0TEST0001", "REVIEW_LIST", ResearchPhase.COLLECT_REVIEWS);
        when(datasetService.listByJobId(JOB_ID)).thenReturn(List.of(products, reviews));
        when(datasetService.readPayload(reviews)).thenReturn(objectMapper.readTree(
                "{\"items\":[{\"asin\":\"B0TEST0001\",\"content\":\"good\"}]}"));
        Path target = temporaryDirectory.resolve("deep-dive-raw.xlsx");

        renderer().render(job(), target, EvidenceStage.DEEP_DIVE);

        assertThat(sheetNames(target)).containsExactly("REVIEW_LIST");
        verify(datasetService, never()).readPayload(products);
    }

    private ResearchRawWorkbookRenderer renderer() {
        return new ResearchRawWorkbookRenderer(datasetService, new ResearchRawWorkbookWriter());
    }

    private MarketResearchDataset dataset(
            String datasetCode, String operation, ResearchPhase phase) {
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetId(datasetCode);
        dataset.setJobId(JOB_ID);
        dataset.setDatasetCode(datasetCode);
        dataset.setOperation(operation);
        dataset.setNodeCode(phase.getNodeCode());
        return dataset;
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        return job;
    }

    private List<String> sheetNames(Path path) throws Exception {
        try (InputStream inputStream = Files.newInputStream(path);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            List<String> names = new ArrayList<>();
            for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                names.add(workbook.getSheetName(index));
            }
            return names;
        }
    }
}
