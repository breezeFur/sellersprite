package com.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口授权来源")
public class PermissionApiSourceVo {

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "授权来源：FUNCTION功能派生 EXTRA直接附加 BOTH双重来源")
    private String grantSource;
}
