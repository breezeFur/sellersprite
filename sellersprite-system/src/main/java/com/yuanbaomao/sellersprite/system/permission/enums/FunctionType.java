package com.yuanbaomao.sellersprite.system.permission.enums;

import lombok.Getter;

@Getter
public enum FunctionType {

    DIR("DIR", "目录"),
    MENU("MENU", "菜单"),
    BUTTON("BUTTON", "按钮");

    private final String code;
    private final String description;

    FunctionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
