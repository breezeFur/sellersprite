package com.yuanbaomao.sellersprite.api.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SellerSprite 官方支持的 Amazon 市场编码。
 */
@Getter
@RequiredArgsConstructor
public enum SellerSpriteMarketplace {

    US("US", "美国站", "USD"),
    JP("JP", "日本站", "JPY"),
    UK("UK", "英国站", "GBP"),
    DE("DE", "德国站", "EUR"),
    FR("FR", "法国站", "EUR"),
    IT("IT", "意大利站", "EUR"),
    ES("ES", "西班牙站", "EUR"),
    CA("CA", "加拿大站", "CAD"),
    IN("IN", "印度站", "INR");

    /** SellerSprite 请求使用的市场编码。 */
    @JsonValue
    private final String code;

    /** 市场中文名称。 */
    private final String label;

    /** 市场默认 ISO 4217 货币编码。 */
    private final String currency;

    /**
     * 前后端使用 MARKET_XX 稳定标签，SellerSprite 远端仍接收 XX 市场编码。
     */
    @JsonCreator
    public static SellerSpriteMarketplace fromTransportValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String code = value.startsWith("MARKET_") ? value.substring("MARKET_".length()) : value;
        for (SellerSpriteMarketplace marketplace : values()) {
            if (marketplace.code.equalsIgnoreCase(code)) {
                return marketplace;
            }
        }
        throw new IllegalArgumentException("不支持的市场标签: " + value);
    }
}
