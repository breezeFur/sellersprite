package com.yuanbaomao.sellersprite.system.permission.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.entity.FunctionApi;
import com.yuanbaomao.sellersprite.system.permission.model.dto.ApiEndpointRefRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingItemRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingSyncRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.ApiCatalogSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.MenuApiBindingSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@ExtendWith(MockitoExtension.class)
class ApiCatalogServiceImplTest {

    @Mock
    private RequestMappingHandlerMapping handlerMapping;
    @Mock
    private SysApiDao sysApiDao;
    @Mock
    private SysFunctionDao sysFunctionDao;
    @Mock
    private FunctionApiDao functionApiDao;
    @Mock
    private RolePermissionService rolePermissionService;

    private ApiCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApiCatalogServiceImpl(handlerMapping, sysApiDao, sysFunctionDao,
                functionApiDao, rolePermissionService);
    }

    @Test
    void shouldLoadApiMappingsFromSpringMvcAndIgnoreNonApiPaths() throws Exception {
        Method listUsers = SampleController.class.getDeclaredMethod("listUsers");
        Method health = SampleController.class.getDeclaredMethod("health");
        RequestMappingInfo userMapping = RequestMappingInfo.paths("/api/users")
                .methods(org.springframework.web.bind.annotation.RequestMethod.GET).build();
        RequestMappingInfo healthMapping = RequestMappingInfo.paths("/actuator/health")
                .methods(org.springframework.web.bind.annotation.RequestMethod.GET).build();
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(
                userMapping, new HandlerMethod(new SampleController(), listUsers),
                healthMapping, new HandlerMethod(new SampleController(), health)));
        when(sysApiDao.list()).thenReturn(List.of());
        doAnswer(invocation -> {
            SysApi entity = invocation.getArgument(0);
            entity.setSysApiId("api-created");
            return true;
        }).when(sysApiDao).save(any(SysApi.class));

        ApiCatalogSyncResultVo result = service.syncCatalog();

        assertThat(result.getScanned()).isEqualTo(1);
        assertThat(result.getCreated()).isEqualTo(1);
        ArgumentCaptor<SysApi> captor = ArgumentCaptor.forClass(SysApi.class);
        verify(sysApiDao).save(captor.capture());
        assertThat(captor.getValue()).satisfies(api -> {
            assertThat(api.getApiName()).isEqualTo("查询用户列表");
            assertThat(api.getHttpMethod()).isEqualTo(HttpMethod.GET.name());
            assertThat(api.getPathPattern()).isEqualTo("/api/users");
            assertThat(api.getApiType()).isEqualTo("PERMISSION");
        });
    }

    @Test
    void shouldReplaceMenuBindingsAndPromoteApisSharedByMultipleMenus() {
        SysFunction users = menu("function-users", "system.user");
        SysFunction roles = menu("function-roles", "system.role");
        SysApi shared = api("api-depts", "GET", "/api/depts/tree");
        SysApi userOnly = api("api-users", "GET", "/api/users");
        when(sysFunctionDao.listAll()).thenReturn(List.of(users, roles));
        when(sysApiDao.list()).thenReturn(List.of(shared, userOnly));
        when(functionApiDao.list()).thenReturn(List.of(
                functionApi("function-users", "api-depts"),
                functionApi("function-users", "api-users"),
                functionApi("function-roles", "api-depts")));

        MenuApiBindingSyncResultVo result = service.syncMenuBindings(syncRequest(
                binding("system.user", endpoint("GET", "/api/depts/tree"), endpoint("GET", "/api/users")),
                binding("system.role", endpoint("GET", "/api/depts/tree"))));

        verify(functionApiDao).replaceByFunctionId("function-users", List.of("api-depts", "api-users"));
        verify(functionApiDao).replaceByFunctionId("function-roles", List.of("api-depts"));
        verify(sysApiDao).updateById(argThat(api -> "api-depts".equals(api.getSysApiId())
                && "PUBLIC".equals(api.getApiType())));
        verify(sysApiDao).updateById(argThat(api -> "api-users".equals(api.getSysApiId())
                && "PERMISSION".equals(api.getApiType())));
        verify(rolePermissionService).recalculateRolesForFunction("function-users");
        verify(rolePermissionService).recalculateRolesForFunction("function-roles");
        assertThat(result.getFunctionCount()).isEqualTo(2);
        assertThat(result.getBindingCount()).isEqualTo(3);
        assertThat(result.getPublicApiCount()).isEqualTo(1);
    }

    @Test
    void shouldRejectWholeBindingSyncWhenAnyEndpointIsMissing() {
        when(sysFunctionDao.listAll()).thenReturn(List.of(menu("function-users", "system.user")));
        when(sysApiDao.list()).thenReturn(List.of());

        assertThatThrownBy(() -> service.syncMenuBindings(syncRequest(
                binding("system.user", endpoint("GET", "/api/missing")))))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.API_NOT_FOUND));

        verify(functionApiDao, never()).replaceByFunctionId(any(), any());
        verify(sysApiDao, never()).updateById(any());
    }

    @Test
    void shouldIncludeExistingCustomMenuBindingsWhenClassifyingSharedApis() {
        SysFunction users = menu("function-users", "system.user");
        SysFunction custom = menu("function-custom", "custom.report");
        SysApi shared = api("api-depts", "GET", "/api/depts/tree");
        when(sysFunctionDao.listAll()).thenReturn(List.of(users, custom));
        when(sysApiDao.list()).thenReturn(List.of(shared));
        when(functionApiDao.list()).thenReturn(List.of(
                functionApi("function-users", "api-depts"),
                functionApi("function-custom", "api-depts")));

        service.syncMenuBindings(syncRequest(
                binding("system.user", endpoint("GET", "/api/depts/tree"))));

        verify(sysApiDao).updateById(argThat(api -> "api-depts".equals(api.getSysApiId())
                && "PUBLIC".equals(api.getApiType())));
    }

    @Test
    void shouldSelectApplicationHandlerMappingWhenManagementMappingAlsoExists() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class,
                    () -> handlerMapping);
            context.registerBean("controllerEndpointHandlerMapping", RequestMappingHandlerMapping.class,
                    () -> org.mockito.Mockito.mock(RequestMappingHandlerMapping.class));
            context.registerBean(SysApiDao.class, () -> sysApiDao);
            context.registerBean(SysFunctionDao.class, () -> sysFunctionDao);
            context.registerBean(FunctionApiDao.class, () -> functionApiDao);
            context.registerBean(RolePermissionService.class, () -> rolePermissionService);
            context.register(ApiCatalogServiceImpl.class);

            context.refresh();

            assertThat(context.getBean(ApiCatalogServiceImpl.class)).isNotNull();
        }
    }

    private MenuApiBindingSyncRequest syncRequest(MenuApiBindingItemRequest... bindings) {
        MenuApiBindingSyncRequest request = new MenuApiBindingSyncRequest();
        request.setBindings(List.of(bindings));
        return request;
    }

    private MenuApiBindingItemRequest binding(String functionCode, ApiEndpointRefRequest... endpoints) {
        MenuApiBindingItemRequest binding = new MenuApiBindingItemRequest();
        binding.setFunctionCode(functionCode);
        binding.setApis(List.of(endpoints));
        return binding;
    }

    private ApiEndpointRefRequest endpoint(String method, String path) {
        ApiEndpointRefRequest endpoint = new ApiEndpointRefRequest();
        endpoint.setHttpMethod(method);
        endpoint.setPathPattern(path);
        return endpoint;
    }

    private SysFunction menu(String id, String code) {
        SysFunction function = new SysFunction();
        function.setSysFunctionId(id);
        function.setFunctionCode(code);
        function.setFunctionType("MENU");
        function.setStatus(1);
        return function;
    }

    private SysApi api(String id, String method, String path) {
        SysApi api = new SysApi();
        api.setSysApiId(id);
        api.setHttpMethod(method);
        api.setPathPattern(path);
        api.setApiType("PERMISSION");
        api.setStatus(1);
        return api;
    }

    private FunctionApi functionApi(String functionId, String apiId) {
        FunctionApi binding = new FunctionApi();
        binding.setSysFunctionId(functionId);
        binding.setSysApiId(apiId);
        return binding;
    }

    private static class SampleController {
        @Operation(summary = "查询用户列表")
        public void listUsers() {
        }

        public void health() {
        }
    }
}
