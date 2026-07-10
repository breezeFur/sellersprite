package com.yuanbaomao.sellersprite.system.role.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "绑定用户角色请求")
public class UserRoleBindRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private String userId;

    @NotBlank(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private String roleId;

    @NotBlank(message = "部门ID不能为空")
    @Schema(description = "部门ID")
    private String deptId;

    @Schema(description = "是否主角色：1是 0否")
    private Integer primaryRole;
}
