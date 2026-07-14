package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.media.Schema;

class GeneratedSellerSpriteContractIndexTest {

    @Test
    void shouldCoverEveryDocumentedBusinessOperationAndField() {
        assertThat(GeneratedSellerSpriteContractIndex.getOperations())
                .hasSize(44)
                .doesNotHaveDuplicates();
        assertThat(GeneratedSellerSpriteContractIndex.getDocumentedRequestFieldCount()).isEqualTo(529);
        assertThat(GeneratedSellerSpriteContractIndex.getDocumentedResponseFieldCount()).isEqualTo(1197);
        assertThat(GeneratedSellerSpriteContractIndex.getDocumentedEndpoints()).allSatisfy(endpoint -> {
            assertThat(endpoint.operation().getMethod().name()).isEqualTo(endpoint.method());
            assertThat(endpoint.operation().getPath()).isEqualTo(endpoint.path());
        });
    }

    @Test
    void shouldAddChineseSchemaToEveryGeneratedPublicField() {
        assertThat(GeneratedSellerSpriteContractIndex.getModelTypes()).isNotEmpty();

        for (Class<?> modelType : GeneratedSellerSpriteContractIndex.getModelTypes()) {
            Schema typeSchema = modelType.getAnnotation(Schema.class);
            assertThat(typeSchema)
                    .as("类型 %s 缺少 @Schema", modelType.getName())
                    .isNotNull();
            assertThat(typeSchema.description())
                    .as("类型 %s 缺少中文说明", modelType.getName())
                    .containsPattern("[\\u4e00-\\u9fff]");

            for (Field field : modelType.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                Schema fieldSchema = field.getAnnotation(Schema.class);
                assertThat(fieldSchema)
                        .as("字段 %s#%s 缺少 @Schema", modelType.getName(), field.getName())
                        .isNotNull();
                assertThat(fieldSchema.description())
                        .as("字段 %s#%s 缺少中文说明", modelType.getName(), field.getName())
                        .containsPattern("[\\u4e00-\\u9fff]");
            }
        }
    }
}
