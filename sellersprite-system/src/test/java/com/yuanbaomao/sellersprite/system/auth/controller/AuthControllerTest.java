package com.yuanbaomao.sellersprite.system.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.auth.config.AuthProperties;
import com.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;
import com.yuanbaomao.sellersprite.system.auth.service.AuthService;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    @Test
    void shouldWriteRefreshTokenToHttpOnlySameSiteCookieAndHideItFromJson() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthProperties properties = new AuthProperties();
        properties.setRefreshCookieName("sellersprite_refresh_token");
        properties.setRefreshCookieSecure(false);
        properties.setRefreshTokenExpireDays(14L);
        AuthController controller = new AuthController(authService, properties);

        AuthLoginRequest loginRequest = new AuthLoginRequest();
        loginRequest.setUsername("yuanbao");
        loginRequest.setPassword("correct-password");
        AuthLoginVo loginVo = new AuthLoginVo();
        loginVo.setAccessToken("access-token");
        loginVo.setRefreshToken("refresh-token");
        when(authService.login(loginRequest, "127.0.0.1", "JUnit")).thenReturn(loginVo);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Result<AuthLoginVo> result = controller.login(loginRequest, request, response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie)
                .contains("sellersprite_refresh_token=refresh-token", "HttpOnly", "SameSite=Lax", "Path=/api/auth",
                        "Max-Age=1209600")
                .doesNotContain("Secure");
        String json = new ObjectMapper().writeValueAsString(result.getData());
        assertThat(json).contains("access-token").doesNotContain("refresh-token", "refreshToken");
    }

    @Test
    void shouldRotateCookieThroughRefreshEndpoint() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, authProperties());
        AuthLoginVo refreshed = new AuthLoginVo();
        refreshed.setAccessToken("new-access-token");
        refreshed.setRefreshToken("new-refresh-token");
        when(authService.refresh("old-refresh-token", "127.0.0.1", "JUnit")).thenReturn(refreshed);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("sellersprite_refresh_token", "old-refresh-token"))
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("new-refresh-token"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("SameSite=Lax"))));
    }

    @Test
    void shouldRevokeSessionAndClearCookieThroughLogoutEndpoint() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, authProperties());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("sellersprite_refresh_token", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("Max-Age=0"),
                        org.hamcrest.Matchers.containsString("HttpOnly"))));
        verify(authService).logout("refresh-token");
    }

    @Test
    void shouldReturnCurrentSession() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, authProperties());
        AuthSessionVo session = new AuthSessionVo();
        UserDetailVo user = new UserDetailVo();
        user.setUserId("user-1");
        user.setUsername("yuanbao");
        session.setUser(user);
        when(authService.current()).thenReturn(session);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value("user-1"))
                .andExpect(jsonPath("$.data.user.username").value("yuanbao"));
    }

    private AuthProperties authProperties() {
        AuthProperties properties = new AuthProperties();
        properties.setRefreshCookieName("sellersprite_refresh_token");
        properties.setRefreshCookieSecure(false);
        properties.setRefreshTokenExpireDays(14L);
        return properties;
    }
}
