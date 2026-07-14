package com.yuanbaomao.sellersprite.framework.security;

import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UrlPathHelper;

@Component
@RequiredArgsConstructor
public class ApiResourceMatcher {

    private static final String MATCHED_RESOURCE_ATTRIBUTE = ApiResourceMatcher.class.getName() + ".matched";
    private static final Object NO_MATCH = new Object();
    private static final String PUBLIC_API_TYPE = "PUBLIC";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
    private static final List<String> PUBLIC_PATH_PATTERNS = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/permissions/apis/public",
            "/favicon.ico",
            "/error",
            "/actuator/health");

    private final SysApiDao sysApiDao;

    public static List<String> publicPathPatterns() {
        return PUBLIC_PATH_PATTERNS;
    }

    public boolean isPublic(HttpServletRequest request) {
        return isStaticPublic(request) || match(request).filter(this::isPublic).isPresent();
    }

    public boolean isStaticPublic(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = normalizePath(request);
        return PUBLIC_PATH_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    public boolean isPublic(SysApi api) {
        return api != null && PUBLIC_API_TYPE.equalsIgnoreCase(api.getApiType());
    }

    public Optional<SysApi> match(HttpServletRequest request) {
        Object cached = request.getAttribute(MATCHED_RESOURCE_ATTRIBUTE);
        if (cached == NO_MATCH) {
            return Optional.empty();
        }
        if (cached instanceof SysApi api) {
            return Optional.of(api);
        }

        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String path = normalizePath(request);
        Optional<SysApi> matched = sysApiDao.listEnabledByHttpMethod(method).stream()
                .filter(api -> api.getPathPattern() != null && !api.getPathPattern().isBlank())
                .filter(api -> PATH_MATCHER.match(normalizePattern(api.getPathPattern()), path))
                .min((left, right) -> PATH_MATCHER.getPatternComparator(path)
                        .compare(normalizePattern(left.getPathPattern()), normalizePattern(right.getPathPattern())));
        request.setAttribute(MATCHED_RESOURCE_ATTRIBUTE, matched.isPresent() ? matched.get() : NO_MATCH);
        return matched;
    }

    private String normalizePath(HttpServletRequest request) {
        return normalizePattern(URL_PATH_HELPER.getPathWithinApplication(request));
    }

    public static String normalizePattern(String path) {
        String normalized = path == null || path.isBlank() ? "/" : path.trim().replaceAll("/{2,}", "/");
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
