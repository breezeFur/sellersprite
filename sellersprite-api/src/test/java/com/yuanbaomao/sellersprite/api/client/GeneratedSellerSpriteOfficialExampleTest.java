package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

class GeneratedSellerSpriteOfficialExampleTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest(name = "{0}")
    @MethodSource("officialExamples")
    @DisplayName("官方响应示例应完整反序列化为对应强类型响应")
    void shouldDeserializeOfficialResponseExample(
            GeneratedSellerSpriteContractIndex.OfficialExample example) throws IOException {
        JavaType dataType = example.collection()
                ? objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, example.dataType())
                : objectMapper.getTypeFactory().constructType(example.dataType());
        JavaType responseType = objectMapper.getTypeFactory()
                .constructParametricType(SellerSpriteResponse.class, dataType);

        try (InputStream input = getClass().getResourceAsStream(example.resourcePath())) {
            assertThat(input).as("官方示例资源 %s", example.resourcePath()).isNotNull();
            SellerSpriteResponse<?> response = objectMapper.readValue(input, responseType);

            assertThat(response.getCode()).isEqualTo("OK");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).isInstanceOf(dataType.getRawClass());
        }
    }

    private static Stream<GeneratedSellerSpriteContractIndex.OfficialExample> officialExamples() {
        assertThat(GeneratedSellerSpriteContractIndex.getOfficialExamples()).hasSize(38);
        return GeneratedSellerSpriteContractIndex.getOfficialExamples().stream();
    }
}
