package com.yuanbaomao.sellersprite.system.permission.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.RoleApi;
import com.yuanbaomao.sellersprite.db.entity.RoleFunction;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionContextServiceImplTest {

    @Mock
    private UserRoleDao userRoleDao;
    @Mock
    private RoleDao roleDao;
    @Mock
    private RoleFunctionDao roleFunctionDao;
    @Mock
    private SysFunctionDao sysFunctionDao;
    @Mock
    private RoleApiDao roleApiDao;
    @Mock
    private SysApiDao sysApiDao;
    @Mock
    private UserDao userDao;

    private PermissionContextServiceImpl permissionContextService;

    @BeforeEach
    void setUp() {
        permissionContextService = new PermissionContextServiceImpl(userDao, userRoleDao, roleDao, roleFunctionDao,
                sysFunctionDao, roleApiDao, sysApiDao);
    }

    @Test
    void shouldAggregateMultipleEnabledRolesMenusPermissionsAndApiSources() {
        when(userDao.getById("user-1")).thenReturn(user(1));
        when(userRoleDao.listByUserId("user-1")).thenReturn(List.of(
                userRole("role-1"), userRole("role-2"), userRole("role-disabled")));
        when(roleDao.listByIds(Set.of("role-1", "role-2", "role-disabled"))).thenReturn(List.of(
                role("role-1", "admin", 1), role("role-2", "auditor", 1),
                role("role-disabled", "disabled", 0)));
        when(roleFunctionDao.listByRoleIds(Set.of("role-1", "role-2"))).thenReturn(List.of(
                roleFunction("role-1", "menu-users"), roleFunction("role-1", "button-user-add"),
                roleFunction("role-2", "menu-users"), roleFunction("role-2", "menu-audit")));
        when(sysFunctionDao.listEnabled()).thenReturn(List.of(
                function("root-system", "0", "DIR", "", 1),
                function("menu-users", "root-system", "MENU", "system:user:view", 1),
                function("button-user-add", "menu-users", "BUTTON", "system:user:add", 1),
                function("menu-audit", "root-system", "MENU", "ops:audit:view", 2)));
        when(roleApiDao.listByRoleIds(Set.of("role-1", "role-2"))).thenReturn(List.of(
                roleApi("role-1", "api-users", "FUNCTION"),
                roleApi("role-2", "api-users", "EXTRA"),
                roleApi("role-2", "api-disabled", "EXTRA")));
        when(sysApiDao.listByIds(Set.of("api-users", "api-disabled"))).thenReturn(List.of(
                api("api-users", "GET", "/api/users", 1),
                api("api-disabled", "DELETE", "/api/users/{id}", 0)));

        UserPermissionContextVo context = permissionContextService.getByUserId("user-1");

        assertThat(context.getRoles()).extracting("roleCode").containsExactly("admin", "auditor");
        assertThat(context.getPermissionCodes()).containsExactlyInAnyOrder(
                "system:user:view", "system:user:add", "ops:audit:view");
        assertThat(context.getMenuTree()).hasSize(1);
        assertThat(context.getMenuTree().getFirst().getFunctionId()).isEqualTo("root-system");
        assertThat(context.getMenuTree().getFirst().getChildren())
                .extracting("functionId").containsExactly("menu-users", "menu-audit");
        assertThat(context.getEffectiveApis()).hasSize(1);
        assertThat(context.getEffectiveApis().getFirst().getSysApiId()).isEqualTo("api-users");
        assertThat(context.getEffectiveApis().getFirst().getSources())
                .extracting("roleCode", "grantSource")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("admin", "FUNCTION"),
                        org.assertj.core.groups.Tuple.tuple("auditor", "EXTRA"));
    }

    @Test
    void shouldReturnEmptyContextWhenUserHasNoEnabledRole() {
        when(userDao.getById("user-1")).thenReturn(user(1));
        when(userRoleDao.listByUserId("user-1")).thenReturn(List.of(userRole("role-disabled")));
        when(roleDao.listByIds(Set.of("role-disabled"))).thenReturn(List.of(role("role-disabled", "disabled", 0)));

        UserPermissionContextVo context = permissionContextService.getByUserId("user-1");

        assertThat(context.getRoles()).isEmpty();
        assertThat(context.getMenuTree()).isEmpty();
        assertThat(context.getPermissionCodes()).isEmpty();
        assertThat(context.getEffectiveApis()).isEmpty();
        verify(roleFunctionDao, never()).listByRoleIds(org.mockito.ArgumentMatchers.anyCollection());
        verify(roleApiDao, never()).listByRoleIds(org.mockito.ArgumentMatchers.anyCollection());
    }

    @Test
    void shouldReturnEmptyContextForDisabledUser() {
        when(userDao.getById("user-1")).thenReturn(user(0));

        UserPermissionContextVo context = permissionContextService.getByUserId("user-1");

        assertThat(context.getRoles()).isEmpty();
        assertThat(context.getMenuTree()).isEmpty();
        assertThat(context.getPermissionCodes()).isEmpty();
        assertThat(context.getEffectiveApis()).isEmpty();
        verify(userRoleDao, never()).listByUserId(org.mockito.ArgumentMatchers.anyString());
    }

    private User user(int status) {
        User user = new User();
        user.setUserId("user-1");
        user.setStatus(status);
        return user;
    }

    private UserRole userRole(String roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId("user-1");
        userRole.setRoleId(roleId);
        return userRole;
    }

    private Role role(String roleId, String roleCode, int status) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setSortOrder("admin".equals(roleCode) ? 1 : 2);
        role.setStatus(status);
        return role;
    }

    private RoleFunction roleFunction(String roleId, String functionId) {
        RoleFunction binding = new RoleFunction();
        binding.setRoleId(roleId);
        binding.setSysFunctionId(functionId);
        return binding;
    }

    private SysFunction function(String functionId, String parentId, String type, String permissionCode,
            int sortOrder) {
        SysFunction function = new SysFunction();
        function.setSysFunctionId(functionId);
        function.setParentId(parentId);
        function.setFunctionName(functionId);
        function.setFunctionType(type);
        function.setRoutePath("/" + functionId);
        function.setComponentPath("views/" + functionId);
        function.setPermissionCode(permissionCode);
        function.setIcon("menu");
        function.setVisible(1);
        function.setCacheable(0);
        function.setSortOrder(sortOrder);
        function.setStatus(1);
        return function;
    }

    private RoleApi roleApi(String roleId, String apiId, String source) {
        RoleApi grant = new RoleApi();
        grant.setRoleId(roleId);
        grant.setSysApiId(apiId);
        grant.setGrantSource(source);
        return grant;
    }

    private SysApi api(String apiId, String method, String path, int status) {
        SysApi api = new SysApi();
        api.setSysApiId(apiId);
        api.setApiCode(apiId);
        api.setApiName(apiId);
        api.setHttpMethod(method);
        api.setPathPattern(path);
        api.setPermissionCode(apiId + ":access");
        api.setStatus(status);
        return api;
    }
}
