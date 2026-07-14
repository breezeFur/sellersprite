package com.yuanbaomao.sellersprite.system.role.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RolePageRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RolePermissionReplaceRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleUpdateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleUserPageRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import com.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.List;

public interface RoleService {

    RoleVo create(RoleCreateRequest request);

    void bindUserRole(UserRoleBindRequest request);

    PageResult<RoleVo> page(RolePageRequest request);

    List<RoleVo> listEnabled();

    RoleVo detail(String roleId);

    RoleVo update(String roleId, RoleUpdateRequest request);

    void updateStatus(String roleId, Integer status);

    PageResult<UserDetailVo> listUsers(String roleId, RoleUserPageRequest request);

    void unbindUser(String roleId, String userId);

    RolePermissionVo getPermissions(String roleId);

    RolePermissionVo replacePermissions(String roleId, RolePermissionReplaceRequest request);

    void delete(String roleId);
}
