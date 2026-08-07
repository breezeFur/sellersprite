package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ResearchInputServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResearchInputService service = new ResearchInputService(objectMapper);

    @Test
    void shouldRestorePersistedCollectionConfigWithoutUsingRuntimeDefaults() throws Exception {
        CollectionGraphConfig config = new CollectionGraphConfig();
        config.getCollectMarketSalesTrend().setMonthCount(12);
        config.getCollectReviews().getPagination().setTargetCountPerAsin(100);
        MarketResearchJob job = job(objectMapper.writeValueAsString(config));

        ResearchInput input = service.from(job);

        assertThat(input.getCollectionConfig().getCollectMarketSalesTrend().getMonthCount())
                .isEqualTo(12);
        assertThat(input.getCollectionConfig()
                        .getCollectReviews()
                        .getPagination()
                        .getTargetCountPerAsin())
                .isEqualTo(100);
    }

    @Test
    void shouldFailWhenNewTaskHasNoCollectionConfig() {
        assertThatThrownBy(() -> service.from(job(null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("任务采集配置不能为空");
    }

    private MarketResearchJob job(String collectionConfig) {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId("job-input-001");
        job.setMarketplace("US");
        job.setNodeIdPath("1055398:1063306");
        job.setResearchMonth("2026-07");
        job.setKeyword("food storage container");
        job.setSeedAsins("[\"B0TEST0001\"]");
        job.setCollectionConfig(collectionConfig);
        return job;
    }
}
