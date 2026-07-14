package com.yuanbaomao.sellersprite.research.service;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * 从任务主表构造采集输入，避免把完整业务参数放入 Batch 参数。
 */
@Service
@RequiredArgsConstructor
public class ResearchInputService {

    private final ObjectMapper objectMapper;

    public ResearchInput from(MarketResearchJob job) {
        return ResearchInput.builder()
                .jobId(job.getJobId())
                .marketplace(job.getMarketplace())
                .keyword(job.getKeyword())
                .seedAsins(readSeedAsins(job.getSeedAsins()))
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
}
