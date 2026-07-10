package com.yuanbaomao.sellersprite.ai.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AiPropertiesTest {

    private static final String EXPECTED_PROVIDER = "openai";

    private static final String EXPECTED_MODEL = "gpt-5.5";

    private static final String EXPECTED_BASE_URL = "https://yuanbaomao.cyou/v1";

    @Test
    void shouldDefaultToOpenAiGptModel() {
        AiProperties properties = new AiProperties();

        assertThat(properties.getChat().getProvider()).isEqualTo(EXPECTED_PROVIDER);
        assertThat(properties.getChat().getModel()).isEqualTo(EXPECTED_MODEL);
        assertThat(properties.getChat().getMemoryWindowSize()).isEqualTo(20);
    }

    @Test
    void shouldBindSpringAiOpenAi2Properties() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TestOpenAiPropertiesConfig.class))
                .withPropertyValues(
                        "spring.ai.openai.api-key=test-key",
                        "spring.ai.openai.base-url=" + EXPECTED_BASE_URL,
                        "spring.ai.openai.chat.model=" + EXPECTED_MODEL)
                .run(context -> {
                    OpenAiCommonProperties commonProperties = context.getBean(OpenAiCommonProperties.class);
                    OpenAiChatProperties chatProperties = context.getBean(OpenAiChatProperties.class);

                    assertThat(commonProperties.getApiKey()).isEqualTo("test-key");
                    assertThat(commonProperties.getBaseUrl()).isEqualTo(EXPECTED_BASE_URL);
                    assertThat(chatProperties.getModel()).isEqualTo(EXPECTED_MODEL);
                });
    }

    @Configuration
    @EnableConfigurationProperties({OpenAiCommonProperties.class, OpenAiChatProperties.class})
    static class TestOpenAiPropertiesConfig {
    }
}
