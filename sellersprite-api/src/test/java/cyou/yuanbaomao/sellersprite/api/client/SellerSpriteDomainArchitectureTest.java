package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import cyou.yuanbaomao.base.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

class SellerSpriteDomainArchitectureTest {

    @Test
    void shouldExposeFortyFiveMethodsThroughNineServicesAndControllers() {
        assertThat(GeneratedSellerSpriteEndpointIndex.getServiceTypes()).hasSize(9);
        assertThat(GeneratedSellerSpriteEndpointIndex.getControllerTypes()).hasSize(9);
        assertThat(GeneratedSellerSpriteEndpointIndex.getOperationCount()).isEqualTo(45);

        int serviceMethodCount = GeneratedSellerSpriteEndpointIndex.getServiceTypes().stream()
                .mapToInt(type -> type.getDeclaredMethods().length)
                .sum();
        int controllerMethodCount = GeneratedSellerSpriteEndpointIndex.getControllerTypes().stream()
                .mapToInt(type -> type.getDeclaredMethods().length)
                .sum();

        assertThat(serviceMethodCount).isEqualTo(45);
        assertThat(controllerMethodCount).isEqualTo(45);
    }

    @Test
    void shouldKeepControllersThinAndDocumented() {
        for (Class<?> controllerType : GeneratedSellerSpriteEndpointIndex.getControllerTypes()) {
            assertThat(controllerType.getAnnotation(Tag.class))
                    .as("Controller %s 缺少中文 @Tag", controllerType.getName())
                    .isNotNull();
            assertThat(controllerType.getAnnotation(RequestMapping.class).value())
                    .singleElement()
                    .asString()
                    .startsWith("/api/sellersprite/");
            assertThat(Arrays.stream(controllerType.getDeclaredFields())
                    .map(field -> field.getType()))
                    .noneMatch(type -> type == SellerSpriteClient.class || type == RestClient.class);
            for (Method method : controllerType.getDeclaredMethods()) {
                assertThat(method.getAnnotation(Operation.class))
                        .as("Controller 方法 %s#%s 缺少 @Operation", controllerType.getName(), method.getName())
                        .isNotNull();
                assertThat(method.getReturnType()).isEqualTo(Result.class);
                assertThat(method.isAnnotationPresent(GetMapping.class)
                        || method.isAnnotationPresent(PostMapping.class)).isTrue();
                for (Parameter parameter : method.getParameters()) {
                    assertThat(parameter.getType()).isNotEqualTo(SellerSpriteProperties.class);
                    assertThat(parameter.isAnnotationPresent(RequestHeader.class)).isFalse();
                    assertThat(parameter.isAnnotationPresent(Valid.class)
                            || parameter.isAnnotationPresent(RequestParam.class)
                            || parameter.isAnnotationPresent(PathVariable.class))
                            .as("Controller 参数 %s#%s 未声明校验或直接查询绑定", controllerType.getName(), method.getName())
                            .isTrue();
                }
            }
        }
    }
}
