package com.yuanbaomao.sellersprite.research.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;

import lombok.Data;
import tools.jackson.databind.ObjectMapper;

/**
 * 从版本化 classpath fixture 读取数据的离线 Provider。
 */
public class MockResearchDataProvider implements ResearchDataProvider {

    private final MockFixture fixture;

    public MockResearchDataProvider(ObjectMapper objectMapper, Resource fixtureResource) {
        Assert.notNull(objectMapper, "objectMapper 不能为空");
        Assert.notNull(fixtureResource, "fixtureResource 不能为空");
        this.fixture = readFixture(objectMapper, fixtureResource);
    }

    @Override
    public ResearchSourceMode sourceMode() {
        return ResearchSourceMode.MOCK;
    }

    @Override
    public List<ResearchDataset> checkQuota(ResearchInput input) {
        validateInput(input);
        return copyDatasets(fixture.getQuota());
    }

    @Override
    public List<ResearchDataset> collectMarketAndProducts(ResearchInput input) {
        validateInput(input);
        return copyDatasets(fixture.getMarketAndProducts());
    }

    @Override
    public List<ResearchDataset> collectKeywords(ResearchInput input) {
        validateInput(input);
        return copyDatasets(fixture.getKeywords());
    }

    @Override
    public List<ResearchDataset> collectReviews(ResearchInput input) {
        validateInput(input);
        return copyDatasets(fixture.getReviews());
    }

    private MockFixture readFixture(ObjectMapper objectMapper, Resource fixtureResource) {
        if (!fixtureResource.exists()) {
            throw new IllegalStateException("市场调研 Mock fixture 不存在: " + fixtureResource.getDescription());
        }
        try (InputStream inputStream = fixtureResource.getInputStream()) {
            MockFixture loadedFixture = objectMapper.readValue(inputStream, MockFixture.class);
            validateFixture(loadedFixture, fixtureResource);
            return loadedFixture;
        } catch (IOException exception) {
            throw new IllegalStateException("读取市场调研 Mock fixture 失败: "
                    + fixtureResource.getDescription(), exception);
        }
    }

    private void validateFixture(MockFixture loadedFixture, Resource fixtureResource) {
        if (loadedFixture == null
                || !StringUtils.hasText(loadedFixture.getVersion())
                || loadedFixture.getQuota() == null
                || loadedFixture.getQuota().isEmpty()
                || loadedFixture.getMarketAndProducts() == null
                || loadedFixture.getMarketAndProducts().isEmpty()
                || loadedFixture.getKeywords() == null
                || loadedFixture.getKeywords().isEmpty()
                || loadedFixture.getReviews() == null
                || loadedFixture.getReviews().isEmpty()) {
            throw new IllegalStateException("市场调研 Mock fixture 结构不完整: "
                    + fixtureResource.getDescription());
        }
        loadedFixture.getQuota().forEach(this::validateDataset);
        loadedFixture.getMarketAndProducts().forEach(this::validateDataset);
        loadedFixture.getKeywords().forEach(this::validateDataset);
        loadedFixture.getReviews().forEach(this::validateDataset);
    }

    private void validateDataset(ResearchDataset dataset) {
        if (dataset == null
                || !StringUtils.hasText(dataset.getDatasetCode())
                || !StringUtils.hasText(dataset.getOperation())
                || dataset.getPayload() == null
                || dataset.getRecordCount() == null
                || dataset.getRecordCount() < 0) {
            throw new IllegalStateException("市场调研 Mock fixture 包含无效数据集");
        }
    }

    private List<ResearchDataset> copyDatasets(List<ResearchDataset> source) {
        return source.stream()
                .map(dataset -> new ResearchDataset(
                        dataset.getDatasetCode(),
                        dataset.getOperation(),
                        dataset.getPayload().deepCopy(),
                        dataset.getRecordCount()))
                .toList();
    }

    private void validateInput(ResearchInput input) {
        if (input == null
                || !StringUtils.hasText(input.getJobId())
                || !StringUtils.hasText(input.getMarketplace())
                || !StringUtils.hasText(input.getKeyword())) {
            throw new IllegalArgumentException("市场调研输入的 jobId、marketplace 和 keyword 不能为空");
        }
    }

    @Data
    private static class MockFixture {
        private String version;
        private List<ResearchDataset> quota;
        private List<ResearchDataset> marketAndProducts;
        private List<ResearchDataset> keywords;
        private List<ResearchDataset> reviews;
    }
}
