package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchStageDataQueryServiceTest {

    @Mock
    private MarketResearchJobDao jobDao;
    @Mock
    private ResearchDatasetService datasetService;

    private ResearchStageDataQueryService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ResearchStageDataQueryService(jobDao, datasetService);
        RequestContextHolder.set(RequestContext.builder().userId("user-1").build());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldReturnSevenScreeningTablesAndPagePersistedRows() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId("job-1");
        job.setUserId("user-1");
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetCode("evidence.products");
        dataset.setRecordCount(2);
        ObjectNode payload = objectMapper.createObjectNode();
        var items = payload.putArray("items");
        items.addObject().put("ASIN", "B0ONE");
        items.addObject().put("ASIN", "B0TWO");
        List<String> screeningDatasetCodes = ResearchEvidenceCatalog
                .definitions(EvidenceStage.SCREENING)
                .stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        when(jobDao.findByIdAndUserId("job-1", "user-1")).thenReturn(Optional.of(job));
        when(datasetService.listMetadataByJobIdAndDatasetCodes(
                        "job-1", screeningDatasetCodes))
                .thenReturn(List.of(dataset));
        when(datasetService.findPayloadByJobIdAndDatasetCode("job-1", "evidence.products"))
                .thenReturn(Optional.of(dataset));
        when(datasetService.readPayload(dataset)).thenReturn(payload);

        var catalog = service.listEvidence("job-1", EvidenceStage.SCREENING);
        var page = service.pageEvidence("job-1", "evidence.products", 2, 1);

        assertThat(catalog).hasSize(7);
        assertThat(catalog.getFirst().getDatasetCode()).isEqualTo("evidence.products");
        assertThat(catalog.getFirst().getRowCount()).isEqualTo(2);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getRecords()).singleElement()
                .satisfies(row -> assertThat(row.path("ASIN").asText()).isEqualTo("B0TWO"));
        verify(datasetService, never()).listByJobId("job-1");
    }
}
