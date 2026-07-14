package com.yuanbaomao.sellersprite.system.auth.constants;

import java.time.Duration;

/**
 * 认证会话固定业务参数。
 */
public final class AuthConstants {

    /** 访问令牌有效期，固定为两天。 */
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofDays(2);

    /** 刷新令牌和刷新 Cookie 有效期，固定为三十天。 */
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    /** 刷新 Cookie 名称后缀，前缀取自 Spring 应用名。 */
    public static final String REFRESH_COOKIE_NAME_SUFFIX = "_refresh_token";

    /** 刷新 Cookie 仅发送到认证接口。 */
    public static final String REFRESH_COOKIE_PATH = "/api/auth";

    /** 刷新 Cookie 的跨站策略。 */
    public static final String REFRESH_COOKIE_SAME_SITE = "Lax";

    private AuthConstants() {
    }
}
