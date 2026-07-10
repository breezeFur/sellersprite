package com.yuanbaomao.sellersprite.api.client;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * SellerSprite Open API 连接配置。
 *
 * <p>密钥只允许从环境变量或本地配置注入，禁止写入受 Git 管理的配置文件。</p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sellersprite.api")
public class SellerSpriteProperties {

    /** 是否启用 SellerSprite 外部接口。 */
    private boolean enabled = true;

    /** SellerSprite Open API 网关地址，不包含接口相对路径。 */
    private String baseUrl = "https://api.sellersprite.com";

    /** SellerSprite API 密钥，默认留空并在实际调用前校验。 */
    private String secretKey = "";

    /** 建立外部连接的最长等待时间，默认 5 秒。 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取外部响应的最长等待时间，默认 30 秒。 */
    private Duration readTimeout = Duration.ofSeconds(30);
}
