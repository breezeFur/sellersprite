package com.yuanbaomao.sellersprite.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuanbaomao.dict.core.DictTemplate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 在 SellerSprite Client 边界把稳定字典标签转换为官方接口参数值。
 */
@Component
public class SellerSpriteDictionaryResolver {

    /** 官方逗号分隔多值参数使用的分隔符。 */
    private static final String MULTI_VALUE_SEPARATOR = ",";

    private static final Set<String> APPENDIX_DICT_TYPES = Set.of(
            "MARKET", "LISTING_DATE", "PRODUCT_SIZE_US", "PRODUCT_SIZE_JP", "PRODUCT_SIZE_CA",
            "PRODUCT_SIZE_EU", "SELLER_NATIONALITY", "PRODUCT_SORT_FIELD", "MARKET_PERIOD",
            "KEYWORD_RESEARCH_SORT_FIELD", "KEYWORD_RESEARCH_TREND_SORT_FIELD",
            "REVERSE_ASIN_EXPOSURE_POSITION", "REVERSE_ASIN_SHARE_TYPE", "REVERSE_ASIN_CONVERSION_TYPE",
            "RELATED_PRODUCT_ASSOCIATION_TYPE", "REVERSE_ASIN_SORT_FIELD", "ABA_SORT_FIELD",
            "REVERSE_MULTIPLE_ASIN_SORT_FIELD", "KEYWORD_EXPLORER_SORT_FIELD", "PRODUCT_WEIGHT_UNIT");

    private final DictTemplate dictTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SellerSpriteDictionaryResolver(DictTemplate dictTemplate) {
        this.dictTemplate = dictTemplate;
    }

    public Object resolveRequest(Object request) {
        Object payload = objectMapper.convertValue(request, Object.class);
        return replaceLabels(payload);
    }

    private Object replaceLabels(Object value) {
        if (value instanceof String label) {
            return resolveLabel(label);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> resolved = new LinkedHashMap<>();
            map.forEach((key, child) -> resolved.put(String.valueOf(key), replaceLabels(child)));
            return resolved;
        }
        if (value instanceof List<?> list) {
            List<Object> resolved = new ArrayList<>(list.size());
            list.forEach(child -> resolved.add(replaceLabels(child)));
            return resolved;
        }
        return value;
    }

    private Object resolveLabel(String label) {
        String[] labels = label.split(MULTI_VALUE_SEPARATOR);
        if (labels.length <= 1) {
            return isAppendixLabel(label) ? dictTemplate.getValueByLabel(label) : label;
        }
        List<String> normalizedLabels = new ArrayList<>(labels.length);
        for (String item : labels) {
            String normalized = item.trim();
            if (!isAppendixLabel(normalized)) {
                return label;
            }
            normalizedLabels.add(normalized);
        }
        List<String> resolvedValues = new ArrayList<>(normalizedLabels.size());
        for (String normalizedLabel : normalizedLabels) {
            String resolvedValue = dictTemplate.getValueByLabel(normalizedLabel);
            if (resolvedValue != null) {
                resolvedValues.add(resolvedValue);
            }
        }
        return String.join(MULTI_VALUE_SEPARATOR, resolvedValues);
    }

    private boolean isAppendixLabel(String label) {
        return APPENDIX_DICT_TYPES.stream().anyMatch(dictType -> label.startsWith(dictType + "_"));
    }
}
