package com.yuanbaomao.sellersprite.research.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import com.yuanbaomao.sellersprite.api.review.service.ReviewService;
import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import com.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import com.yuanbaomao.sellersprite.research.provider.RemoteResearchDataProvider;
import com.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;

import tools.jackson.databind.ObjectMapper;

class ResearchProviderConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ResearchProviderConfiguration.class)
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void shouldUseMockProviderByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResearchDataProvider.class);
            assertThat(context.getBean(ResearchDataProvider.class))
                    .isInstanceOf(MockResearchDataProvider.class);
            assertThat(context.getBean(ResearchDataProvider.class).sourceMode())
                    .isEqualTo(ResearchSourceMode.MOCK);
            assertThat(context.getBean(ResearchProperties.class).getSourceMode())
                    .isEqualTo(ResearchSourceMode.MOCK);
            assertThat(context.getBean(ResearchProperties.class).isRecoveryEnabled()).isTrue();
            assertThat(context.getBean(ResearchProperties.class).getTemplateLocation())
                    .isEqualTo("classpath:research/templates/market-research-v1.xlsx");
            assertThat(context.getBean(ResearchProperties.class).getOutputDirectory())
                    .isEqualTo("./data/market-research");
        });
    }

    @Test
    void shouldUseRemoteProviderWhenConfigured() {
        contextRunner
                .withPropertyValues("sellersprite.research.source-mode=REMOTE")
                .withBean(AccountService.class, () -> mock(AccountService.class))
                .withBean(ProductService.class, () -> mock(ProductService.class))
                .withBean(KeywordService.class, () -> mock(KeywordService.class))
                .withBean(ReviewService.class, () -> mock(ReviewService.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ResearchDataProvider.class);
                    assertThat(context.getBean(ResearchDataProvider.class))
                            .isInstanceOf(RemoteResearchDataProvider.class);
                    assertThat(context.getBean(ResearchDataProvider.class).sourceMode())
                            .isEqualTo(ResearchSourceMode.REMOTE);
                    assertThat(context.getBean(ResearchProperties.class).getSourceMode())
                            .isEqualTo(ResearchSourceMode.REMOTE);
                });
    }
}
