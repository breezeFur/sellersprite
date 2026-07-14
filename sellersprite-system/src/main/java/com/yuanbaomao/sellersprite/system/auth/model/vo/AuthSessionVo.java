package com.yuanbaomao.sellersprite.system.auth.model.vo;

import com.yuanbaomao.sellersprite.system.permission.model.vo.PermissionMenuVo;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
@Schema(description = "当前认证会话")
public class AuthSessionVo {

    @Schema(description = "用户信息")
    private UserDetailVo user;

    @Schema(description = "权限版本")
    private Long permissionVersion;

    @Schema(description = "启用角色")
    private List<RoleVo> roles = new ArrayList<>();

    @Schema(description = "菜单树")
    private List<PermissionMenuVo> menuTree = new ArrayList<>();

    @Schema(description = "权限标识")
    private Set<String> permissionCodes = new LinkedHashSet<>();
}
