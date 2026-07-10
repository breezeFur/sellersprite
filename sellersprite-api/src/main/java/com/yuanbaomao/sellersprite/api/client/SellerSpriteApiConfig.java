package com.yuanbaomao.sellersprite.api.client;

import java.net.http.HttpClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.yuanbaomao.base.id.IdGenerator;

/**
 * SellerSprite Open API Client 装配配置。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SellerSpriteProperties.class)
public class SellerSpriteApiConfig {

    @Bean
    @ConditionalOnMissingBean(SellerSpriteAuthStrategy.class)
    public SellerSpriteAuthStrategy sellerSpriteAuthStrategy(SellerSpriteProperties properties,
            IdGenerator idGenerator) {
        return new DefaultSellerSpriteAuthStrategy(properties, idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(SellerSpriteClient.class)
    public SellerSpriteClient sellerSpriteClient(SellerSpriteProperties properties,
            SellerSpriteAuthStrategy authStrategy) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
        return new SellerSpriteClient(restClient, authStrategy);
    }
}
