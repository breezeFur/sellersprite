package com.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单接口绑定同步结果")
public class MenuApiBindingSyncResultVo {

    @Schema(description = "同步菜单数")
    private int functionCount;

    @Schema(description = "写入绑定数")
    private int bindingCount;

    @Schema(description = "多菜单共享接口数")
    private int publicApiCount;

    @Schema(description = "单菜单权限接口数")
    private int permissionApiCount;
}
