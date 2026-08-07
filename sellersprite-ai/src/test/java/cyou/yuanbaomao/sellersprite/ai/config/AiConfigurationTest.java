package cyou.yuanbaomao.sellersprite.ai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AiConfigurationTest {

    private static final String EXPECTED_MODEL = "gpt-5.5";

    private static final String EXPECTED_BASE_URL = "https://yuanbaomao.cyou/v1";

    private static final Duration EXPECTED_TIMEOUT = Duration.ofMinutes(5);

    private static final int EXPECTED_MAX_RETRIES = 1;

    @Test
    void shouldBindSpringAiOpenAiProperties() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TestOpenAiPropertiesConfig.class))
                .withPropertyValues(
                        "spring.ai.openai.api-key=test-key",
                        "spring.ai.openai.base-url=" + EXPECTED_BASE_URL,
                        "spring.ai.openai.timeout=300s",
                        "spring.ai.openai.max-retries=1",
                        "spring.ai.openai.chat.model=" + EXPECTED_MODEL)
                .run(context -> {
                    OpenAiCommonProperties commonProperties = context.getBean(OpenAiCommonProperties.class);
                    OpenAiChatProperties chatProperties = context.getBean(OpenAiChatProperties.class);

                    assertThat(commonProperties.getApiKey()).isEqualTo("test-key");
                    assertThat(commonProperties.getBaseUrl()).isEqualTo(EXPECTED_BASE_URL);
                    assertThat(commonProperties.getTimeout()).isEqualTo(EXPECTED_TIMEOUT);
                    assertThat(commonProperties.getMaxRetries()).isEqualTo(EXPECTED_MAX_RETRIES);
                    assertThat(chatProperties.getModel()).isEqualTo(EXPECTED_MODEL);
                });
    }

    @Test
    void shouldRethrowToolExecutionErrorsInsteadOfReturningThemToTheModel() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ToolCallingAutoConfiguration.class))
                .withPropertyValues("spring.ai.tools.throw-exception-on-error=true")
                .run(context -> {
                    ToolCallingProperties properties = context.getBean(ToolCallingProperties.class);
                    ToolExecutionExceptionProcessor processor = context.getBean(
                            ToolExecutionExceptionProcessor.class);
                    ToolDefinition toolDefinition = ToolDefinition.builder()
                            .name("sellersprite_get_asin_detail")
                            .description("测试工具")
                            .inputSchema("{}")
                            .build();
                    ToolExecutionException exception = new ToolExecutionException(
                            toolDefinition, new IllegalStateException("upstream failed"));

                    assertThat(properties.isThrowExceptionOnError()).isTrue();
                    assertThatThrownBy(() -> processor.process(exception)).isSameAs(exception);
                });
    }

    @Configuration
    @EnableConfigurationProperties({OpenAiCommonProperties.class, OpenAiChatProperties.class})
    static class TestOpenAiPropertiesConfig {
    }
}
