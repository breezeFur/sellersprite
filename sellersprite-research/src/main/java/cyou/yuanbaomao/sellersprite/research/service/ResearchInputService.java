package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * 从任务主表构造采集输入，避免把完整业务参数放入 Graph 状态。
 */
@Service
@RequiredArgsConstructor
public class ResearchInputService {

    private final ObjectMapper objectMapper;

    public ResearchInput from(MarketResearchJob job) {
        return ResearchInput.builder()
                .jobId(job.getJobId())
                .marketplace(job.getMarketplace())
                .nodeIdPath(job.getNodeIdPath())
                .month(job.getResearchMonth())
                .keyword(job.getKeyword())
                .seedAsins(readSeedAsins(job.getSeedAsins()))
                .collectionConfig(readCollectionConfig(job.getCollectionConfig()))
                .build();
    }

    public ResearchInput withSeedAsins(ResearchInput input, List<String> seedAsins) {
        return ResearchInput.builder()
                .jobId(input.getJobId())
                .marketplace(input.getMarketplace())
                .nodeIdPath(input.getNodeIdPath())
                .month(input.getMonth())
                .keyword(input.getKeyword())
                .seedAsins(seedAsins == null ? List.of() : List.copyOf(seedAsins))
                .collectionConfig(input.getCollectionConfig())
                .build();
    }

    private List<String> readSeedAsins(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalStateException("任务种子ASIN数据格式错误", exception);
        }
    }

    private CollectionGraphConfig readCollectionConfig(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("任务采集配置不能为空");
        }
        try {
            return objectMapper.readValue(value, CollectionGraphConfig.class);
        } catch (Exception exception) {
            throw new IllegalStateException("任务采集配置格式错误", exception);
        }
    }
}
