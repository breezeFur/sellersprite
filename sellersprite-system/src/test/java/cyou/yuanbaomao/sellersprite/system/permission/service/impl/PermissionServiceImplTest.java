package cyou.yuanbaomao.sellersprite.system.permission.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.SysApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import cyou.yuanbaomao.sellersprite.db.entity.SysFunction;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private SysFunctionDao sysFunctionDao;
    @Mock
    private SysApiDao sysApiDao;
    @Mock
    private FunctionApiDao functionApiDao;
    @Mock
    private RolePermissionService rolePermissionService;
    @Mock
    private RoleApiDao roleApiDao;
    @Mock
    private RoleFunctionDao roleFunctionDao;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(sysFunctionDao, sysApiDao, functionApiDao,
                rolePermissionService, roleApiDao, roleFunctionDao);
    }

    @Test
    void shouldReplaceFunctionApisAndRecalculateAffectedRoles() {
        when(sysFunctionDao.getById("function-1")).thenReturn(function("function-1"));
        when(sysApiDao.listByIds(Set.of("api-1", "api-2")))
                .thenReturn(List.of(api("api-1"), api("api-2")));

        permissionService.replaceFunctionApis("function-1", List.of("api-1", "api-2", "api-1"));

        verify(functionApiDao).replaceByFunctionId(eq("function-1"),
                argThat(apiIds -> Set.copyOf(apiIds).equals(Set.of("api-1", "api-2"))));
        verify(rolePermissionService).recalculateRolesForFunction("function-1");
    }

    @Test
    void shouldKeepFunctionApisWhenAnyRequestedApiIsInvalid() {
        when(sysFunctionDao.getById("function-1")).thenReturn(function("function-1"));
        when(sysApiDao.listByIds(Set.of("api-1", "api-missing"))).thenReturn(List.of(api("api-1")));

        assertThatThrownBy(() -> permissionService.replaceFunctionApis("function-1",
                List.of("api-1", "api-missing")))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.API_NOT_FOUND));
        verify(functionApiDao, never()).replaceByFunctionId(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyCollection());
        verify(rolePermissionService, never()).recalculateRolesForFunction(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectDeletingApiReferencedByFunction() {
        when(sysApiDao.getById("api-1")).thenReturn(api("api-1"));
        when(functionApiDao.existsByApiId("api-1")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.deleteApi("api-1"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("功能");
                });
        verify(sysApiDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectDeletingApiReferencedByRole() {
        when(sysApiDao.getById("api-1")).thenReturn(api("api-1"));
        when(roleApiDao.existsByApiId("api-1")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.deleteApi("api-1"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("角色");
                });
        verify(sysApiDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectDeletingFunctionWithChildren() {
        when(sysFunctionDao.getById("function-1")).thenReturn(function("function-1"));
        when(sysFunctionDao.existsByParentId("function-1")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.deleteFunction("function-1"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("子功能");
                });
        verify(sysFunctionDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectDeletingFunctionReferencedByRole() {
        when(sysFunctionDao.getById("function-1")).thenReturn(function("function-1"));
        when(roleFunctionDao.existsByFunctionId("function-1")).thenReturn(true);

        assertThatThrownBy(() -> permissionService.deleteFunction("function-1"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("角色");
                });
        verify(sysFunctionDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldClearOwnedBindingsWhenDeletingUnreferencedFunction() {
        when(sysFunctionDao.getById("function-1")).thenReturn(function("function-1"));

        permissionService.deleteFunction("function-1");

        verify(functionApiDao).replaceByFunctionId("function-1", Set.of());
        verify(sysFunctionDao).removeById("function-1");
    }

    @Test
    void shouldRejectMenuBelowButton() {
        SysFunction current = function("function-1");
        current.setFunctionType("MENU");
        when(sysFunctionDao.getById("function-1")).thenReturn(current);
        SysFunction parent = function("button-1");
        parent.setFunctionType("BUTTON");
        when(sysFunctionDao.getById("button-1")).thenReturn(parent);
        SysFunctionUpdateRequest request = new SysFunctionUpdateRequest();
        request.setParentId("button-1");
        request.setFunctionCode("user-list");
        request.setFunctionName("用户列表");
        request.setFunctionType("MENU");
        request.setRoutePath("/system/users");
        request.setComponentPath("system/users/index");

        assertThatThrownBy(() -> permissionService.updateFunction("function-1", request))
                .isInstanceOfSatisfying(BizException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ResultCode.PARAM_INVALID));
        verify(sysFunctionDao, never()).updateById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectMovingFunctionBelowItsDescendant() {
        SysFunction current = function("function-1");
        current.setParentId("0");
        current.setFunctionType("DIR");
        SysFunction descendant = function("function-2");
        descendant.setParentId("function-1");
        descendant.setFunctionType("DIR");
        when(sysFunctionDao.getById("function-1")).thenReturn(current);
        when(sysFunctionDao.getById("function-2")).thenReturn(descendant);
        SysFunctionUpdateRequest request = new SysFunctionUpdateRequest();
        request.setParentId("function-2");
        request.setFunctionCode("system");
        request.setFunctionName("系统管理");
        request.setFunctionType("DIR");

        assertThatThrownBy(() -> permissionService.updateFunction("function-1", request))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("循环");
                });
    }

    @Test
    void shouldRejectDuplicateNormalizedMethodAndPath() {
        when(sysApiDao.existsByHttpMethodAndPathPattern("GET", "/api/users", null)).thenReturn(true);
        SysApiCreateRequest request = new SysApiCreateRequest();
        request.setApiCode("user.page");
        request.setApiName("用户分页");
        request.setApiType("PERMISSION");
        request.setHttpMethod("get");
        request.setPathPattern("//api/users/");

        assertThatThrownBy(() -> permissionService.createApi(request))
                .isInstanceOfSatisfying(BizException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ResultCode.API_METHOD_PATH_ALREADY_EXISTS));
        verify(sysApiDao, never()).save(org.mockito.ArgumentMatchers.any());
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
}
