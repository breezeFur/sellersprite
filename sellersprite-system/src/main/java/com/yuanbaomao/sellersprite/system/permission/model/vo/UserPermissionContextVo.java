package com.yuanbaomao.sellersprite.system.permission.model.vo;

import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
@Schema(description = "用户权限上下文")
public class UserPermissionContextVo {

    @Schema(description = "启用角色")
    private List<RoleVo> roles = new ArrayList<>();

    @Schema(description = "菜单树")
    private List<PermissionMenuVo> menuTree = new ArrayList<>();

    @Schema(description = "功能权限标识")
    private Set<String> permissionCodes = new LinkedHashSet<>();

    @Schema(description = "有效接口权限")
    private List<EffectiveApiPermissionVo> effectiveApis = new ArrayList<>();
}
