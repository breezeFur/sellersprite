package cyou.yuanbaomao.sellersprite.system.role.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RolePageRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RolePermissionReplaceRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleUserPageRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
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
