package cyou.yuanbaomao.sellersprite.research.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import cyou.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.provider.RemoteResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.service.ResearchSourceCacheService;

import jakarta.validation.Validator;
import tools.jackson.databind.ObjectMapper;

/**
 * 根据配置装配唯一的市场调研数据 Provider。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ResearchProperties.class)
public class ResearchProviderConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = ResearchProperties.PREFIX,
            name = "source-mode",
            havingValue = "MOCK",
            matchIfMissing = true)
    ResearchDataProvider mockResearchDataProvider(
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            ResearchProperties properties) {
        return new MockResearchDataProvider(
                objectMapper,
                resourceLoader.getResource(properties.getMockFixtureLocation()));
    }

    @Bean
    @ConditionalOnProperty(
            prefix = ResearchProperties.PREFIX,
            name = "source-mode",
            havingValue = "REMOTE")
    ResearchDataProvider remoteResearchDataProvider(
            ObjectMapper objectMapper,
            AccountService accountService,
            AsinService asinService,
            ProductService productService,
            KeywordService keywordService,
            MarketService marketService,
            ReviewService reviewService,
            TrafficService trafficService,
            ResearchSourceCacheService sourceCacheService,
            Validator validator) {
        return new RemoteResearchDataProvider(
                objectMapper,
                accountService,
                asinService,
                productService,
                keywordService,
                marketService,
                reviewService,
                trafficService,
                sourceCacheService,
                validator);
    }
}
