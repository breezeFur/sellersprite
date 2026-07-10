package com.yuanbaomao.sellersprite.system.constants;

public final class SystemBusinessConstants {

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    public static final int YES = 1;
    public static final int NO = 0;
    public static final String DEFAULT_ROLE_TYPE = "BUSINESS";
    public static final String DEFAULT_CLIENT_TYPE = "WEB";
    public static final String TOKEN_TYPE_BEARER = "BEARER";
    public static final String PASSWORD_LOGIN_FAILURE = "用户名或密码错误";
    public static final String TOKEN_REVOKE_REASON_ROTATED = "ROTATED";
    public static final String TOKEN_REVOKE_REASON_REUSED = "REUSED";
    public static final String TOKEN_REVOKE_REASON_EXPIRED = "EXPIRED";
    public static final String TOKEN_REVOKE_REASON_LOGOUT = "LOGOUT";
    public static final String TOKEN_REVOKE_REASON_USER_SECURITY_CHANGED = "USER_SECURITY_CHANGED";

    private SystemBusinessConstants() {
    }
}
