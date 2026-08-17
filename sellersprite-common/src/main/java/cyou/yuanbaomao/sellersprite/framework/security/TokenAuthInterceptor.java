package cyou.yuanbaomao.sellersprite.framework.security;

import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.db.entity.UserToken;
import cyou.yuanbaomao.base.constants.HttpHeaderConstants;
import cyou.yuanbaomao.base.constants.SecurityConstants;
import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

@Component
@RequiredArgsConstructor
public class TokenAuthInterceptor implements HandlerInterceptor {

    private static final int USER_STATUS_ENABLED = 1;
    private static final String RESEARCH_SSE_STREAM_PATH_PATTERN =
            "/api/market-research/jobs/{jobId}/stream";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    private final TokenHasher tokenHasher;
    private final UserTokenDao userTokenDao;
    private final UserDao userDao;
    private final ApiResourceMatcher apiResourceMatcher;

    public static List<String> publicPathPatterns() {
        return ApiResourceMatcher.publicPathPatterns();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (shouldSkip(request)) {
            return true;
        }

        String accessToken = resolveAccessToken(request);
        if (accessToken == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        UserToken userToken = userTokenDao.findValidByAccessTokenHash(tokenHasher.sha256(accessToken))
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
        User user = userDao.getById(userToken.getUserId());
        if (user == null || !Integer.valueOf(USER_STATUS_ENABLED).equals(user.getStatus())) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        RequestContext existingContext = RequestContextHolder.get().orElse(null);
        RequestContextHolder.set(RequestContext.builder()
                .traceId(existingContext == null ? null : existingContext.getTraceId())
                .userId(user.getUserId())
                .username(user.getUsername())
                .build());
        return true;
    }

    private boolean shouldSkip(HttpServletRequest request) {
        return shouldSkipResearchSseAsyncRedispatch(request)
                || apiResourceMatcher.isStaticPublic(request);
    }

    private boolean shouldSkipResearchSseAsyncRedispatch(HttpServletRequest request) {
        return request.getDispatcherType() == DispatcherType.ASYNC
                && HttpMethod.GET.matches(request.getMethod())
                && PATH_MATCHER.match(RESEARCH_SSE_STREAM_PATH_PATTERN,
                        URL_PATH_HELPER.getPathWithinApplication(request));
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaderConstants.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String headerValue = authorization.trim();
        if (!headerValue.regionMatches(true, 0, SecurityConstants.BEARER_TOKEN_PREFIX, 0,
                SecurityConstants.BEARER_TOKEN_PREFIX.length())) {
            return null;
        }
        String token = headerValue.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }
}
