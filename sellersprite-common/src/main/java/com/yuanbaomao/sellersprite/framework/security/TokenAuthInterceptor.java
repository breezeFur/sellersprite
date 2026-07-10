package com.yuanbaomao.sellersprite.framework.security;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.base.constants.HttpHeaderConstants;
import com.yuanbaomao.base.constants.SecurityConstants;
import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TokenAuthInterceptor implements HandlerInterceptor {

    private static final int USER_STATUS_ENABLED = 1;

    private static final List<String> PUBLIC_PATH_PATTERNS = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/permissions/apis/public",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/doc.html",
            "/webjars/**",
            "/favicon.ico",
            "/error",
            "/actuator/health");

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final TokenHasher tokenHasher;
    private final UserTokenDao userTokenDao;
    private final UserDao userDao;

    public static List<String> publicPathPatterns() {
        return PUBLIC_PATH_PATTERNS;
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
                .trackId(existingContext == null ? null : existingContext.getTrackId())
                .userId(user.getUserId())
                .username(user.getUsername())
                .build());
        return true;
    }

    private boolean shouldSkip(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String requestPath = resolveRequestPath(request);
        return PUBLIC_PATH_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, requestPath));
    }

    private String resolveRequestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            String path = requestUri.substring(contextPath.length());
            return path.isBlank() ? "/" : path;
        }
        return requestUri;
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
