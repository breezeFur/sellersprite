package com.yuanbaomao.sellersprite.system.permission.security;

import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.framework.security.ApiResourceMatcher;
import com.yuanbaomao.sellersprite.system.permission.model.vo.EffectiveApiPermissionVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiPermissionInterceptor implements HandlerInterceptor {

    /** 该角色编码拥有全部接口访问权限，包括尚未登记的接口。 */
    private static final String SUPER_ADMIN_ROLE_CODE = "admin";

    private final ApiResourceMatcher apiResourceMatcher;
    private final PermissionContextService permissionContextService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (apiResourceMatcher.isStaticPublic(request)) {
            return true;
        }
        SysApi matchedApi = apiResourceMatcher.match(request).orElse(null);
        if (apiResourceMatcher.isPublic(matchedApi)) {
            return true;
        }
        String userId = RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
        UserPermissionContextVo permissionContext = permissionContextService.getByUserId(userId);
        boolean superAdmin = permissionContext.getRoles().stream()
                .anyMatch(role -> SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode()));
        if (superAdmin) {
            return true;
        }
        if (matchedApi == null) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        boolean authorized = permissionContext.getEffectiveApis().stream()
                .map(EffectiveApiPermissionVo::getSysApiId)
                .anyMatch(matchedApi.getSysApiId()::equals);
        if (!authorized) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return true;
    }
}
