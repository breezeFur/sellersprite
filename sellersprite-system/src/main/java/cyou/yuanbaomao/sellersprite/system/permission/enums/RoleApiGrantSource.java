package cyou.yuanbaomao.sellersprite.system.permission.enums;

import lombok.Getter;

@Getter
public enum RoleApiGrantSource {

    FUNCTION("FUNCTION", "功能派生授权"),
    EXTRA("EXTRA", "直接附加授权"),
    BOTH("BOTH", "功能派生和直接附加授权");

    private final String code;
    private final String description;

    RoleApiGrantSource(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
