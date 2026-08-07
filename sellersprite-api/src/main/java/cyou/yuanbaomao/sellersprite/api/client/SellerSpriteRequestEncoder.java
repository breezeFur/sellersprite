package cyou.yuanbaomao.sellersprite.api.client;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;

/**
 * SellerSprite 强类型请求对象编码器。
 *
 * <p>该类只在 Client 边界内转换查询参数和 multipart，不向 Controller 暴露 Map 契约。</p>
 */
public final class SellerSpriteRequestEncoder {

    private static final String CLASS_PROPERTY = "class";

    private SellerSpriteRequestEncoder() {
    }

    /**
     * 将 GET 请求 DTO 转为多值查询参数。
     *
     * @param request 强类型请求对象
     * @param excludedFields 已用于路径变量、不得重复进入 query 的字段
     * @return 已过滤空值和文件字段的查询参数
     */
    public static MultiValueMap<String, String> toQuery(Object request, Set<String> excludedFields) {
        LinkedMultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (request == null) {
            return result;
        }
        Set<String> excluded = excludedFields == null ? Set.of() : excludedFields;
        visit(request, (name, value) -> {
            if (excluded.contains(name) || value instanceof MultipartFile) {
                return;
            }
            addValues(result, externalName(request, name), value);
        });
        return result;
    }

    /**
     * 将 multipart 请求 DTO 转为 Spring multipart parts。
     *
     * @param request 强类型请求对象
     * @return 包含普通字段、列表字段和文件 Resource 的 multipart parts
     */
    public static MultiValueMap<String, Object> toMultipart(Object request) {
        LinkedMultiValueMap<String, Object> result = new LinkedMultiValueMap<>();
        if (request == null) {
            return result;
        }
        visit(request, (name, value) -> {
            String partName = externalName(request, name);
            if (value instanceof MultipartFile multipartFile) {
                if (!multipartFile.isEmpty()) {
                    result.add(partName, multipartFile.getResource());
                }
                return;
            }
            if (value instanceof Collection<?> collection) {
                for (Object item : collection) {
                    if (item != null) {
                        result.add(partName, pathValue(item));
                    }
                }
                return;
            }
            result.add(partName, pathValue(value));
        });
        return result;
    }

    /**
     * 将路径或查询值转换为 SellerSprite 官方编码。
     *
     * @param value 市场枚举或普通标量值
     * @return 可写入 URL 或表单的字符串
     */
    public static String pathValue(Object value) {
        if (value instanceof SellerSpriteMarketplace marketplace) {
            return marketplace.getCode();
        }
        return String.valueOf(value);
    }

    private static void addValues(MultiValueMap<String, String> result, String name, Object value) {
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item != null) {
                    result.add(name, pathValue(item));
                }
            }
            return;
        }
        result.add(name, pathValue(value));
    }

    private static void visit(Object request, PropertyConsumer consumer) {
        BeanWrapper wrapper = new BeanWrapperImpl(request);
        for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
            String name = descriptor.getName();
            if (CLASS_PROPERTY.equals(name) || !wrapper.isReadableProperty(name)) {
                continue;
            }
            Object value = wrapper.getPropertyValue(name);
            if (value != null) {
                consumer.accept(name, value);
            }
        }
    }

    private static String externalName(Object request, String propertyName) {
        Field field = ReflectionUtils.findField(request.getClass(), propertyName);
        JsonProperty jsonProperty = field == null
                ? null
                : AnnotatedElementUtils.findMergedAnnotation(field, JsonProperty.class);
        return jsonProperty != null && StringUtils.hasText(jsonProperty.value())
                ? jsonProperty.value()
                : propertyName;
    }

    @FunctionalInterface
    private interface PropertyConsumer {
        void accept(String name, Object value);
    }
}
