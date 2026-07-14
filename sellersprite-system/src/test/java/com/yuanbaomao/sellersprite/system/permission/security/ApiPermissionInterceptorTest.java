package com.yuanbaomao.sellersprite.system.permission.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.framework.security.ApiResourceMatcher;
import com.yuanbaomao.sellersprite.system.permission.model.vo.EffectiveApiPermissionVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ApiPermissionInterceptorTest {

    @Mock
    private ApiResourceMatcher apiResourceMatcher;
    @Mock
    private PermissionContextService permissionContextService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldAllowExplicitStaticPublicPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        when(apiResourceMatcher.isStaticPublic(request)).thenReturn(true);
        ApiPermissionInterceptor interceptor = interceptor();

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verify(apiResourceMatcher, never()).match(request);
    }

    @Test
    void shouldAllowRegisteredPublicApiWithoutUserContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/ping");
        SysApi api = api("api-public", "PUBLIC");
        when(apiResourceMatcher.match(request)).thenReturn(Optional.of(api));
        when(apiResourceMatcher.isPublic(api)).thenReturn(true);

        assertThat(interceptor().preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verify(permissionContextService, never()).getByUserId(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldDenyUnregisteredProtectedApiByDefault() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/unregistered");
        when(apiResourceMatcher.match(request)).thenReturn(Optional.empty());
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());
        when(permissionContextService.getByUserId("user-1")).thenReturn(new UserPermissionContextVo());

        assertThatThrownBy(() -> interceptor().preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.FORBIDDEN));
    }

    @Test
    void shouldAllowUnregisteredApiForSuperAdmin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/unregistered");
        when(apiResourceMatcher.match(request)).thenReturn(Optional.empty());
        RequestContextHolder.set(RequestContext.builder().userId("admin-user").username("admin").build());
        UserPermissionContextVo context = new UserPermissionContextVo();
        RoleVo superAdminRole = new RoleVo();
        superAdminRole.setRoleCode("admin");
        context.setRoles(List.of(superAdminRole));
        when(permissionContextService.getByUserId("admin-user")).thenReturn(context);

        assertThat(interceptor().preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    void shouldAllowApiIncludedInCurrentUserEffectivePermissions() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/user-1");
        SysApi api = api("api-user-detail", "PERMISSION");
        when(apiResourceMatcher.match(request)).thenReturn(Optional.of(api));
        when(apiResourceMatcher.isPublic(api)).thenReturn(false);
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());
        UserPermissionContextVo context = new UserPermissionContextVo();
        EffectiveApiPermissionVo permission = new EffectiveApiPermissionVo();
        permission.setSysApiId("api-user-detail");
        context.setEffectiveApis(List.of(permission));
        when(permissionContextService.getByUserId("user-1")).thenReturn(context);

        assertThat(interceptor().preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    void shouldDenyRegisteredApiMissingFromCurrentUserPermissions() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/users/user-2");
        SysApi api = api("api-user-delete", "PERMISSION");
        when(apiResourceMatcher.match(request)).thenReturn(Optional.of(api));
        when(apiResourceMatcher.isPublic(api)).thenReturn(false);
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());
        when(permissionContextService.getByUserId("user-1")).thenReturn(new UserPermissionContextVo());

        assertThatThrownBy(() -> interceptor().preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.FORBIDDEN));
    }

    @Test
    void shouldRequireEffectivePermissionForLogQuery() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/logs/login");
        SysApi api = api("api-login-log-page", "PERMISSION");
        when(apiResourceMatcher.match(request)).thenReturn(Optional.of(api));
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());
        UserPermissionContextVo context = new UserPermissionContextVo();
        EffectiveApiPermissionVo permission = new EffectiveApiPermissionVo();
        permission.setSysApiId("api-login-log-page");
        context.setEffectiveApis(List.of(permission));
        when(permissionContextService.getByUserId("user-1")).thenReturn(context);

        assertThat(interceptor().preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    void shouldDenyNextRequestImmediatelyAfterPermissionIsRevoked() throws Exception {
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("PUT", "/api/users/user-2");
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("PUT", "/api/users/user-2");
        SysApi api = api("api-user-update", "PERMISSION");
        when(apiResourceMatcher.match(firstRequest)).thenReturn(Optional.of(api));
        when(apiResourceMatcher.match(secondRequest)).thenReturn(Optional.of(api));
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());
        UserPermissionContextVo allowed = new UserPermissionContextVo();
        EffectiveApiPermissionVo permission = new EffectiveApiPermissionVo();
        permission.setSysApiId("api-user-update");
        allowed.setEffectiveApis(List.of(permission));
        when(permissionContextService.getByUserId("user-1"))
                .thenReturn(allowed, new UserPermissionContextVo());
        ApiPermissionInterceptor interceptor = interceptor();

        assertThat(interceptor.preHandle(firstRequest, new MockHttpServletResponse(), new Object())).isTrue();
        assertThatThrownBy(() -> interceptor.preHandle(secondRequest, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.FORBIDDEN));
    }

    private ApiPermissionInterceptor interceptor() {
        return new ApiPermissionInterceptor(apiResourceMatcher, permissionContextService);
    }

    private SysApi api(String apiId, String apiType) {
        SysApi api = new SysApi();
        api.setSysApiId(apiId);
        api.setApiType(apiType);
        api.setStatus(1);
        return api;
    }
}
