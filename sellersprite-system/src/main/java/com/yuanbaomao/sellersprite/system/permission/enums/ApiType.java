package com.yuanbaomao.sellersprite.system.permission.enums;

import lombok.Getter;

@Getter
public enum ApiType {

    PUBLIC("PUBLIC", "公开接口"),
    PERMISSION("PERMISSION", "权限接口");

    private final String code;
    private final String description;

    ApiType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
