package com.yuanbaomao.sellersprite.system.permission.service;

import com.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import java.util.Collection;

public interface RolePermissionService {

    void replaceRolePermissions(String roleId, Collection<String> functionIds, Collection<String> extraApiIds);

    RolePermissionVo getRolePermissions(String roleId);

    void recalculateRolesForFunction(String functionId);

    void clearRolePermissions(String roleId);
}
