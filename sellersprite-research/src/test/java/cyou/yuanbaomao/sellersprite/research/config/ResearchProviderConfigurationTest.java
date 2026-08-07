package cyou.yuanbaomao.sellersprite.research.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import cyou.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.provider.RemoteResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSourceCacheService;

import tools.jackson.databind.ObjectMapper;

class ResearchProviderConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ResearchProviderConfiguration.class)
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withBean(Validator.class, () -> Validation.buildDefaultValidatorFactory().getValidator());

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
            assertThat(context.getBean(ResearchProperties.class).isDispatcherEnabled()).isTrue();
            assertThat(context.getBean(ResearchProperties.class).getOutputDirectory())
                    .isEqualTo("./data/market-research");
            assertThat(context.getBean(ResearchProperties.class).getRemoteEnrichmentAsinLimit())
                    .isEqualTo(5);
        });
    }

    @Test
    void shouldUseRemoteProviderWhenConfigured() {
        contextRunner
                .withPropertyValues("sellersprite.research.source-mode=REMOTE")
                .withBean(AccountService.class, () -> mock(AccountService.class))
                .withBean(AsinService.class, () -> mock(AsinService.class))
                .withBean(ProductService.class, () -> mock(ProductService.class))
                .withBean(KeywordService.class, () -> mock(KeywordService.class))
                .withBean(MarketService.class, () -> mock(MarketService.class))
                .withBean(ReviewService.class, () -> mock(ReviewService.class))
                .withBean(TrafficService.class, () -> mock(TrafficService.class))
                .withBean(ResearchSourceCacheService.class, () -> mock(ResearchSourceCacheService.class))
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
