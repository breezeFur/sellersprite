package cyou.yuanbaomao.sellersprite.system.permission.security;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import cyou.yuanbaomao.sellersprite.framework.security.ApiResourceMatcher;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.EffectiveApiPermissionVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import cyou.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

@Component
@RequiredArgsConstructor
public class ApiPermissionInterceptor implements HandlerInterceptor {

    /** 该角色编码拥有全部接口访问权限，包括尚未登记的接口。 */
    private static final String SUPER_ADMIN_ROLE_CODE = "admin";
    private static final String RESEARCH_SSE_STREAM_PATH_PATTERN =
            "/api/market-research/jobs/{jobId}/stream";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    private final ApiResourceMatcher apiResourceMatcher;
    private final PermissionContextService permissionContextService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (shouldSkipResearchSseAsyncRedispatch(request)
                || apiResourceMatcher.isStaticPublic(request)) {
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

    private boolean shouldSkipResearchSseAsyncRedispatch(HttpServletRequest request) {
        return request.getDispatcherType() == DispatcherType.ASYNC
                && HttpMethod.GET.matches(request.getMethod())
                && PATH_MATCHER.match(RESEARCH_SSE_STREAM_PATH_PATTERN,
                        URL_PATH_HELPER.getPathWithinApplication(request));
    }
}
