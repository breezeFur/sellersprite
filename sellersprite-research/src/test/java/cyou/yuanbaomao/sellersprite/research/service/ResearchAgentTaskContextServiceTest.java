package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ResearchAgentTaskContextServiceTest {

    @Test
    void shouldDescribePersistedReviewSamplingScopeForAgent() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MarketResearchJobDao jobDao = mock(MarketResearchJobDao.class);
        ResearchStageInputService stageInputService = mock(ResearchStageInputService.class);
        ResearchInputService inputService = new ResearchInputService(objectMapper);
        CollectionGraphConfig config = new CollectionGraphConfig();
        config.getCollectReviews().setStarList(List.of("1"));
        config.getCollectReviews().setTypeList(List.of("3"));
        config.getCollectReviews().getPagination().setTargetCountPerAsin(20);
        MarketResearchJob job = job(objectMapper.writeValueAsString(config));
        when(jobDao.getById("job-1")).thenReturn(job);
        when(stageInputService.findSelection("job-1")).thenReturn(Optional.of(
                new ResearchProductSelection(
                        ResearchSelectionDecision.ENTER,
                        List.of("B0TEST0001", "B0TEST0002"))));
        ResearchAgentTaskContextService service = new ResearchAgentTaskContextService(
                jobDao, inputService, stageInputService, objectMapper);

        String context = service.describe("job-1", ResearchStageCode.DEEP_DIVE);

        assertThat(context)
                .contains("当前阶段：DEEP_DIVE")
                .contains("人工选中/种子 ASIN：[B0TEST0001, B0TEST0002]")
                .contains("星级筛选：[1星]")
                .contains("类型筛选：[VP评论]")
                .contains("每个 ASIN 目标评论数：20")
                .contains("不得据此推断总体差评率、总体平均星级或总体满意度")
                .contains("\"starList\":[\"1\"]");
    }

    private MarketResearchJob job(String collectionConfig) {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId("job-1");
        job.setMarketplace("US");
        job.setNodeIdPath("15684181:15718271");
        job.setResearchMonth("2026-07");
        job.setKeyword("car spray");
        job.setSeedAsins("[]");
        job.setCollectionConfig(collectionConfig);
        return job;
    }
}
