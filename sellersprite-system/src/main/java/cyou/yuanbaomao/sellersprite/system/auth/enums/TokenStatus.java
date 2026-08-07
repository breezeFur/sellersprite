package cyou.yuanbaomao.sellersprite.system.auth.enums;

import lombok.Getter;

@Getter
public enum TokenStatus {

    INVALID(0, "失效"),
    VALID(1, "有效");

    private final Integer code;
    private final String description;

    TokenStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
