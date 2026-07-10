package com.yuanbaomao.sellersprite.system.auth.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;
import com.yuanbaomao.sellersprite.system.auth.config.AuthProperties;
import com.yuanbaomao.sellersprite.system.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthProperties authProperties;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<AuthLoginVo> login(@Valid @RequestBody AuthLoginRequest request, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        AuthLoginVo loginVo = authService.login(request, servletRequest.getRemoteAddr(),
                servletRequest.getHeader("User-Agent"));
        writeRefreshCookie(servletResponse, loginVo.getRefreshToken(),
                Duration.ofDays(authProperties.getRefreshTokenExpireDays()));
        return Result.success(loginVo);
    }

    @Operation(summary = "刷新会话")
    @PostMapping("/refresh")
    public Result<AuthLoginVo> refresh(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        AuthLoginVo loginVo = authService.refresh(resolveRefreshToken(servletRequest), servletRequest.getRemoteAddr(),
                servletRequest.getHeader("User-Agent"));
        writeRefreshCookie(servletResponse, loginVo.getRefreshToken(),
                Duration.ofDays(authProperties.getRefreshTokenExpireDays()));
        return Result.success(loginVo);
    }

    @Operation(summary = "退出当前会话")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        authService.logout(resolveRefreshToken(servletRequest));
        writeRefreshCookie(servletResponse, "", Duration.ZERO);
        return Result.success();
    }

    @Operation(summary = "查询当前会话")
    @GetMapping("/session")
    public Result<AuthSessionVo> current() {
        return Result.success(authService.current());
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (authProperties.getRefreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeRefreshCookie(HttpServletResponse response, String value, Duration maxAge) {
        ResponseCookie refreshCookie = ResponseCookie.from(authProperties.getRefreshCookieName(), value)
                .httpOnly(true)
                .secure(authProperties.isRefreshCookieSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
