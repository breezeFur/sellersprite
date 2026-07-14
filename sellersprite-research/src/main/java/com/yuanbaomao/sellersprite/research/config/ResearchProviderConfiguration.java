package com.yuanbaomao.sellersprite.research.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import com.yuanbaomao.sellersprite.api.review.service.ReviewService;
import com.yuanbaomao.sellersprite.research.provider.MockResearchDataProvider;
import com.yuanbaomao.sellersprite.research.provider.RemoteResearchDataProvider;
import com.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;

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
            ProductService productService,
            KeywordService keywordService,
            ReviewService reviewService) {
        return new RemoteResearchDataProvider(
                objectMapper, accountService, productService, keywordService, reviewService);
    }
}
