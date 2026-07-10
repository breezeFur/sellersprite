package com.yuanbaomao.sellersprite.system.auth.enums;

import lombok.Getter;

@Getter
public enum LoginType {

    PASSWORD("PASSWORD", "密码登录"),
    TOKEN("TOKEN", "令牌登录");

    private final String code;
    private final String description;

    LoginType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
