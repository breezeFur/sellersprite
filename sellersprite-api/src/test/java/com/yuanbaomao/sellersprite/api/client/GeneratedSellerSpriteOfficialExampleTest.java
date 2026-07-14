package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
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
            byte[] json = input.readAllBytes();
            JsonNode documentedResponse = objectMapper.readTree(json);
            SellerSpriteResponse<?> response = objectMapper.readValue(json, responseType);

            assertThat(response.getCode()).isEqualTo("OK");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).isInstanceOf(dataType.getRawClass());
            assertJsonPreserved(documentedResponse, objectMapper.valueToTree(response), "$");
        }
    }

    private void assertJsonPreserved(JsonNode expected, JsonNode actual, String path) {
        assertThat(actual).as("缺失官方返回字段 %s", path).isNotNull();
        if (expected.isObject()) {
            for (Map.Entry<String, JsonNode> field : expected.properties()) {
                assertJsonPreserved(field.getValue(), actual.get(field.getKey()), path + "." + field.getKey());
            }
            return;
        }
        if (expected.isArray()) {
            assertThat(actual.isArray()).as("官方返回字段 %s 应为数组", path).isTrue();
            assertThat(actual.size()).as("官方返回数组 %s 长度", path).isEqualTo(expected.size());
            for (int index = 0; index < expected.size(); index++) {
                assertJsonPreserved(expected.get(index), actual.get(index), path + "[" + index + "]");
            }
            return;
        }
        if (expected.isNumber() && actual.isNumber()) {
            assertThat(actual.decimalValue()).as("官方返回数值 %s", path)
                    .isEqualByComparingTo(expected.decimalValue());
            return;
        }
        if (isNumericTextPair(expected, actual)) {
            assertThat(new BigDecimal(actual.asText())).as("官方返回数值 %s", path)
                    .isEqualByComparingTo(new BigDecimal(expected.asText()));
            return;
        }
        assertThat(actual).as("官方返回值 %s", path).isEqualTo(expected);
    }

    private boolean isNumericTextPair(JsonNode expected, JsonNode actual) {
        if (!(expected.isNumber() && actual.isTextual())
                && !(expected.isTextual() && actual.isNumber())) {
            return false;
        }
        try {
            new BigDecimal(expected.asText());
            new BigDecimal(actual.asText());
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static Stream<GeneratedSellerSpriteContractIndex.OfficialExample> officialExamples() {
        assertThat(GeneratedSellerSpriteContractIndex.getOfficialExamples()).hasSize(38);
        return GeneratedSellerSpriteContractIndex.getOfficialExamples().stream();
    }
}
