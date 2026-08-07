package cyou.yuanbaomao.sellersprite.system.auth.controller;

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
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import cyou.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import cyou.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.PermissionMenuVo;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import cyou.yuanbaomao.sellersprite.system.auth.service.AuthService;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.List;
import java.util.Set;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    private static final String APPLICATION_NAME = "sellersprite-service";
    private static final String REFRESH_COOKIE_NAME = APPLICATION_NAME + "_refresh_token";

    @Test
    void shouldWriteRefreshTokenToHttpOnlySameSiteCookieAndHideItFromJson() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, APPLICATION_NAME);

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
                .contains(REFRESH_COOKIE_NAME + "=refresh-token", "HttpOnly", "SameSite=Lax", "Path=/api/auth",
                        "Max-Age=2592000")
                .doesNotContain("Secure");
        String json = new ObjectMapper().writeValueAsString(result.getData());
        assertThat(json).contains("access-token").doesNotContain("refresh-token", "refreshToken");
    }

    @Test
    void shouldMarkRefreshCookieSecureForHttpsRequest() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, APPLICATION_NAME);
        AuthLoginRequest loginRequest = new AuthLoginRequest();
        AuthLoginVo loginVo = new AuthLoginVo();
        loginVo.setRefreshToken("refresh-token");
        when(authService.login(loginRequest, "127.0.0.1", "JUnit")).thenReturn(loginVo);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.login(loginRequest, request, response);

        assertThat(response.getHeader("Set-Cookie")).contains("Secure");
    }

    @Test
    void shouldRotateCookieThroughRefreshEndpoint() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, APPLICATION_NAME);
        AuthLoginVo refreshed = new AuthLoginVo();
        refreshed.setAccessToken("new-access-token");
        refreshed.setRefreshToken("new-refresh-token");
        when(authService.refresh("old-refresh-token", "127.0.0.1", "JUnit")).thenReturn(refreshed);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(REFRESH_COOKIE_NAME, "old-refresh-token"))
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
        AuthController controller = new AuthController(authService, APPLICATION_NAME);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie(REFRESH_COOKIE_NAME, "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("Max-Age=0"),
                        org.hamcrest.Matchers.containsString("HttpOnly"))));
        verify(authService).logout("refresh-token");
    }

    @Test
    void shouldReturnCurrentSession() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, APPLICATION_NAME);
        AuthSessionVo session = new AuthSessionVo();
        UserDetailVo user = new UserDetailVo();
        user.setUserId("user-1");
        user.setUsername("yuanbao");
        session.setUser(user);
        RoleVo role = new RoleVo();
        role.setRoleCode("admin");
        session.setRoles(List.of(role));
        PermissionMenuVo menu = new PermissionMenuVo();
        menu.setFunctionId("menu-system");
        session.setMenuTree(List.of(menu));
        session.setPermissionCodes(Set.of("system:user:view"));
        session.setPermissionVersion(7L);
        when(authService.current()).thenReturn(session);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value("user-1"))
                .andExpect(jsonPath("$.data.user.username").value("yuanbao"))
                .andExpect(jsonPath("$.data.roles[0].roleCode").value("admin"))
                .andExpect(jsonPath("$.data.menuTree[0].functionId").value("menu-system"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("system:user:view"))
                .andExpect(jsonPath("$.data.permissionVersion").value(7));
    }

}
