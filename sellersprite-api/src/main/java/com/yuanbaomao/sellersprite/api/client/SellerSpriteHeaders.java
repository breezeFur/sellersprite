package com.yuanbaomao.sellersprite.api.client;

/**
 * SellerSprite Open API 请求头常量。
 */
public final class SellerSpriteHeaders {

    /** SellerSprite 分配的 API 密钥请求头，禁止写入日志。 */
    public static final String SECRET_KEY = "secret-key";

    /** 每次外部请求唯一的链路标识请求头，值为 UUIDv7。 */
    public static final String REQUEST_ID = "x-request-id";

    private SellerSpriteHeaders() {
    }
}
