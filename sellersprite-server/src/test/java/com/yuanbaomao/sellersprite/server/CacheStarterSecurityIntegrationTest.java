package com.yuanbaomao.sellersprite.server;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.cache.core.CacheTemplate;
import com.yuanbaomao.log.core.OperationLogSink;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.sellersprite.framework.security.TokenHasher;
import com.yuanbaomao.sellersprite.system.permission.model.vo.EffectiveApiPermissionVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CacheStarterSecurityIntegrationTest {

    private static final String ACCESS_TOKEN = "cache-access-token";
    private static final String USER_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd8091";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheTemplate cacheTemplate;

    @Autowired
    private TokenHasher tokenHasher;

    @MockitoBean
    private UserTokenDao userTokenDao;

    @MockitoBean
    private UserDao userDao;

    @MockitoBean
    private SysApiDao sysApiDao;

    @MockitoBean
    private PermissionContextService permissionContextService;

    @MockitoBean
    private OperationLogSink operationLogSink;

    private List<SysApi> cacheApis;

    @BeforeEach
    void setUp() {
        cacheTemplate.clear();
        cacheApis = List.of(
                api("cache-keys", "GET", "/api/cache/keys"),
                api("cache-value", "GET", "/api/cache/value"),
                api("cache-exists", "GET", "/api/cache/exists"),
                api("cache-delete", "DELETE", "/api/cache/key"),
                api("cache-clear", "DELETE", "/api/cache"));
        when(sysApiDao.listEnabledByHttpMethod(anyString())).thenAnswer(invocation -> {
            String method = invocation.getArgument(0, String.class).toUpperCase(Locale.ROOT);
            return cacheApis.stream().filter(api -> method.equals(api.getHttpMethod())).toList();
        });
        when(userTokenDao.findValidByAccessTokenHash(tokenHasher.sha256(ACCESS_TOKEN)))
                .thenReturn(Optional.of(validToken()));
        when(userDao.getById(USER_ID)).thenReturn(enabledUser());
        when(permissionContextService.getByUserId(USER_ID)).thenReturn(permissionContext(cacheApis));
    }

    @AfterEach
    void tearDown() {
        cacheTemplate.clear();
        RequestContextHolder.clear();
    }

    @Test
    void shouldRejectAnonymousAndUnauthorizedCacheRequests() throws Exception {
        mockMvc.perform(get("/api/cache/keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A401"));

        when(permissionContextService.getByUserId(USER_ID)).thenReturn(new UserPermissionContextVo());
        mockMvc.perform(get("/api/cache/keys").header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A403"));
    }

    @Test
    void shouldExposeOnlyTheFiveStarterEndpointsToAuthorizedUsers() throws Exception {
        cacheTemplate.put("beta", "B", Duration.ofMinutes(1));
        cacheTemplate.put("alpha", "A", Duration.ofMinutes(1));

        mockMvc.perform(authorized(get("/api/cache/keys")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data[0]").value("alpha"))
                .andExpect(jsonPath("$.data[1]").value("beta"));

        mockMvc.perform(authorized(get("/api/cache/value").queryParam("key", "alpha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.key").value("alpha"))
                .andExpect(jsonPath("$.data.exists").value(true))
                .andExpect(jsonPath("$.data.value").value("A"));

        mockMvc.perform(authorized(get("/api/cache/exists").queryParam("key", "beta")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(authorized(delete("/api/cache/key").queryParam("key", "alpha")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
        mockMvc.perform(authorized(get("/api/cache/exists").queryParam("key", "alpha")))
                .andExpect(jsonPath("$.data").value(false));

        mockMvc.perform(authorized(delete("/api/cache")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
        mockMvc.perform(authorized(get("/api/cache/keys")))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorized(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + ACCESS_TOKEN);
    }

    private SysApi api(String id, String method, String path) {
        SysApi api = new SysApi();
        api.setSysApiId(id);
        api.setApiCode(id);
        api.setApiType("PERMISSION");
        api.setHttpMethod(method);
        api.setPathPattern(path);
        api.setStatus(1);
        return api;
    }

    private UserToken validToken() {
        UserToken token = new UserToken();
        token.setUserId(USER_ID);
        token.setStatus(1);
        token.setExpiresAt(System.currentTimeMillis() + Duration.ofMinutes(5).toMillis());
        return token;
    }

    private User enabledUser() {
        User user = new User();
        user.setUserId(USER_ID);
        user.setUsername("cache-admin");
        user.setStatus(1);
        return user;
    }

    private UserPermissionContextVo permissionContext(List<SysApi> apis) {
        UserPermissionContextVo context = new UserPermissionContextVo();
        context.setEffectiveApis(apis.stream().map(api -> {
            EffectiveApiPermissionVo permission = new EffectiveApiPermissionVo();
            permission.setSysApiId(api.getSysApiId());
            permission.setHttpMethod(api.getHttpMethod());
            permission.setPathPattern(api.getPathPattern());
            return permission;
        }).toList());
        return context;
    }
}
