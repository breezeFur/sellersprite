package com.yuanbaomao.sellersprite.system.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色响应")
public class RoleVo {

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色类型")
    private String roleType;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
