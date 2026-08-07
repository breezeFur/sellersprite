package cyou.yuanbaomao.sellersprite.common.enums;

import lombok.Getter;

@Getter
public enum EnabledStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    EnabledStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean sameCode(Integer value) {
        return code.equals(value);
    }
}
