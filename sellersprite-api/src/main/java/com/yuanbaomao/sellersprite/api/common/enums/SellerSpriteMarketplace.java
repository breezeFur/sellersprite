package com.yuanbaomao.sellersprite.api.common.enums;

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
}
