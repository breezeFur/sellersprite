package com.yuanbaomao.sellersprite.system.permission.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import com.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.entity.FunctionApi;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.RoleApi;
import com.yuanbaomao.sellersprite.db.entity.RoleFunction;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTest {

    @Mock
    private RoleDao roleDao;
    @Mock
    private SysFunctionDao sysFunctionDao;
    @Mock
    private SysApiDao sysApiDao;
    @Mock
    private RoleFunctionDao roleFunctionDao;
    @Mock
    private FunctionApiDao functionApiDao;
    @Mock
    private RoleApiDao roleApiDao;
    @Mock
    private UserRoleDao userRoleDao;
    @Mock
    private UserDao userDao;

    private RolePermissionServiceImpl rolePermissionService;

    @BeforeEach
    void setUp() {
        rolePermissionService = new RolePermissionServiceImpl(roleDao, sysFunctionDao, sysApiDao,
                roleFunctionDao, functionApiDao, roleApiDao, userRoleDao, userDao);
    }

    @Test
    void shouldReplaceRoleFunctionsAndMergeEffectiveApiSources() {
        when(roleDao.getById("role-1")).thenReturn(role("role-1"));
        when(sysFunctionDao.listByIds(Set.of("function-1", "function-2")))
                .thenReturn(List.of(function("function-1"), function("function-2")));
        when(sysApiDao.listByIds(Set.of("api-2"))).thenReturn(List.of(api("api-2")));
        when(functionApiDao.listByFunctionIds(Set.of("function-1", "function-2")))
                .thenReturn(List.of(functionApi("function-1", "api-1"),
                        functionApi("function-2", "api-1"), functionApi("function-2", "api-2")));
        UserRole userRole = new UserRole();
        userRole.setUserId("user-1");
        userRole.setRoleId("role-1");
        when(userRoleDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of(userRole));

        rolePermissionService.replaceRolePermissions("role-1", List.of("function-1", "function-2"),
                List.of("api-2"));

        verify(roleFunctionDao).replaceByRoleId(eq("role-1"),
                argThat(functionIds -> Set.copyOf(functionIds)
                        .equals(Set.of("function-1", "function-2"))));
        Map<String, String> sources = captureRoleApiSources();
        assertThat(sources).containsExactlyInAnyOrderEntriesOf(
                Map.of("api-1", "FUNCTION", "api-2", "BOTH"));
        verify(userDao).incrementPermissionVersion(Set.of("user-1"));
    }

    @Test
    void shouldKeepExtraApisWhenFunctionBindingChanges() {
        RoleFunction roleFunction = roleFunction("role-1", "function-1");
        RoleApi extraApi = roleApi("role-1", "api-extra", "EXTRA");
        when(roleFunctionDao.listByFunctionIds(Set.of("function-1"))).thenReturn(List.of(roleFunction));
        when(roleFunctionDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of(roleFunction));
        when(functionApiDao.listByFunctionIds(Set.of("function-1")))
                .thenReturn(List.of(functionApi("function-1", "api-derived")));
        when(roleApiDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of(extraApi));

        rolePermissionService.recalculateRolesForFunction("function-1");

        Map<String, String> sources = captureRoleApiSources();
        assertThat(sources).containsExactlyInAnyOrderEntriesOf(
                Map.of("api-derived", "FUNCTION", "api-extra", "EXTRA"));
    }

    @Test
    void shouldReturnConfiguredFunctionsExtraApisAndEffectiveSources() {
        when(roleDao.getById("role-1")).thenReturn(role("role-1"));
        when(roleFunctionDao.listByRoleIds(Set.of("role-1")))
                .thenReturn(List.of(roleFunction("role-1", "function-1")));
        when(roleApiDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of(
                roleApi("role-1", "api-derived", "FUNCTION"),
                roleApi("role-1", "api-extra", "EXTRA")));
        SysApi derived = api("api-derived");
        derived.setApiCode("derived");
        SysApi extra = api("api-extra");
        extra.setApiCode("extra");
        when(sysApiDao.listByIds(Set.of("api-derived", "api-extra"))).thenReturn(List.of(derived, extra));

        RolePermissionVo result = rolePermissionService.getRolePermissions("role-1");

        assertThat(result.getFunctionIds()).containsExactly("function-1");
        assertThat(result.getExtraApiIds()).containsExactly("api-extra");
        assertThat(result.getEffectiveApis()).extracting("sysApiId", "grantSource")
                .containsExactly(tuple("api-derived", "FUNCTION"), tuple("api-extra", "EXTRA"));
    }

    @Test
    void shouldReturnEmptyPermissionsWithoutQueryingApisForRoleWithoutGrants() {
        when(roleDao.getById("role-1")).thenReturn(role("role-1"));
        when(roleFunctionDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of());
        when(roleApiDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of());

        RolePermissionVo result = rolePermissionService.getRolePermissions("role-1");

        assertThat(result.getFunctionIds()).isEmpty();
        assertThat(result.getExtraApiIds()).isEmpty();
        assertThat(result.getEffectiveApis()).isEmpty();
        verifyNoInteractions(sysApiDao);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, String> captureRoleApiSources() {
        ArgumentCaptor<Collection<RoleApi>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(roleApiDao).replaceByRoleId(eq("role-1"), captor.capture());
        return captor.getValue().stream()
                .collect(Collectors.toMap(RoleApi::getSysApiId, RoleApi::getGrantSource));
    }

    private Role role(String roleId) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setStatus(1);
        return role;
    }

    private SysFunction function(String functionId) {
        SysFunction function = new SysFunction();
        function.setSysFunctionId(functionId);
        function.setStatus(1);
        return function;
    }

    private SysApi api(String apiId) {
        SysApi api = new SysApi();
        api.setSysApiId(apiId);
        api.setStatus(1);
        return api;
    }

    private FunctionApi functionApi(String functionId, String apiId) {
        FunctionApi functionApi = new FunctionApi();
        functionApi.setSysFunctionId(functionId);
        functionApi.setSysApiId(apiId);
        return functionApi;
    }

    private RoleFunction roleFunction(String roleId, String functionId) {
        RoleFunction roleFunction = new RoleFunction();
        roleFunction.setRoleId(roleId);
        roleFunction.setSysFunctionId(functionId);
        return roleFunction;
    }

    private RoleApi roleApi(String roleId, String apiId, String source) {
        RoleApi roleApi = new RoleApi();
        roleApi.setRoleId(roleId);
        roleApi.setSysApiId(apiId);
        roleApi.setGrantSource(source);
        return roleApi;
    }
}
