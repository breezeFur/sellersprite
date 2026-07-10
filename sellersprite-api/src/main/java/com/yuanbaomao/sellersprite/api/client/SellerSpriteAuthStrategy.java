package com.yuanbaomao.sellersprite.api.client;

import org.springframework.http.HttpHeaders;

/**
 * SellerSprite 外部请求认证策略。
 *
 * <p>当前官方规则只要求密钥和唯一请求标识；保留该接口用于未来接入官方真实签名规则。</p>
 */
public interface SellerSpriteAuthStrategy {

    /**
     * 将认证信息写入请求头。
     *
     * @param headers 当前外部请求头
     * @return 本次请求生成的唯一请求 ID
     */
    String apply(HttpHeaders headers);
}
