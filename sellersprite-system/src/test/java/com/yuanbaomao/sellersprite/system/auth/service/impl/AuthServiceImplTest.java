package com.yuanbaomao.sellersprite.system.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.id.IdGenerator;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.LoginLog;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.sellersprite.framework.security.TokenHasher;
import com.yuanbaomao.sellersprite.system.auth.constants.AuthConstants;
import com.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.PermissionMenuVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import com.yuanbaomao.sellersprite.system.ops.login.service.LoginLogRecorder;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserDao userDao;
    @Mock
    private UserTokenDao userTokenDao;
    @Mock
    private LoginLogRecorder loginLogRecorder;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private TokenHasher tokenHasher;
    @Mock
    private PermissionContextService permissionContextService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDao, userTokenDao, loginLogRecorder, passwordEncoder, idGenerator,
                tokenHasher, permissionContextService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldCreateSeparateAccessAndRefreshExpirationsForEnabledUser() {
        User user = enabledUser();
        when(userDao.findByUsername("yuanbao")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", user.getPasswordHash())).thenReturn(true);
        when(idGenerator.nextId()).thenAnswer(invocation -> UUID.randomUUID().toString());
        when(tokenHasher.sha256(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
        when(permissionContextService.getByUserId("user-1")).thenReturn(permissionContext());

        AuthLoginVo result = authService.login(loginRequest(), "127.0.0.1", "JUnit");

        ArgumentCaptor<UserToken> tokenCaptor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenDao).save(tokenCaptor.capture());
        UserToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getSessionFamilyId()).isNotBlank();
        assertThat(savedToken.getRefreshExpiresAt()).isGreaterThan(savedToken.getExpiresAt());
        assertThat(savedToken.getRefreshExpiresAt() - savedToken.getExpiresAt())
                .isEqualTo(AuthConstants.REFRESH_TOKEN_TTL.minus(AuthConstants.ACCESS_TOKEN_TTL).toMillis());
        assertThat(savedToken.getRefreshTokenHash()).isEqualTo("hash:" + result.getRefreshToken());
        assertThat(result.getAccessToken()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getRoles()).extracting("roleCode").containsExactly("admin");
        assertThat(result.getMenuTree()).extracting("functionId").containsExactly("menu-system");
        assertThat(result.getPermissionCodes()).containsExactly("system:user:view");
        assertThat(result.getPermissionVersion()).isEqualTo(7L);
        ArgumentCaptor<LoginLog> loginLogCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRecorder).record(loginLogCaptor.capture());
        assertThat(loginLogCaptor.getValue())
                .extracting("username", "loginType", "success", "errorCode")
                .containsExactly("yuanbao", "PASSWORD", 1, "");
    }

    @Test
    void shouldRejectDisabledUserWithoutCreatingToken() {
        User user = enabledUser();
        user.setStatus(0);
        when(userDao.findByUsername("yuanbao")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest(), "127.0.0.1", "JUnit"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.UNAUTHORIZED));
        verify(userTokenDao, never()).save(any());
        ArgumentCaptor<LoginLog> loginLogCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRecorder).record(loginLogCaptor.capture());
        assertThat(loginLogCaptor.getValue())
                .extracting("loginType", "success", "errorCode", "failureReason")
                .containsExactly("PASSWORD", 0, "A401", "账号已停用");
    }

    @Test
    void shouldRecordBadCredentialsWithoutPersistingPassword() {
        User user = enabledUser();
        when(userDao.findByUsername("yuanbao")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest(), "127.0.0.1", "JUnit"))
                .isInstanceOf(BizException.class);

        ArgumentCaptor<LoginLog> loginLogCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRecorder).record(loginLogCaptor.capture());
        LoginLog loginLog = loginLogCaptor.getValue();
        assertThat(loginLog)
                .extracting("success", "errorCode", "failureReason")
                .containsExactly(0, "A401", "用户名或密码错误");
        assertThat(loginLog.toString()).doesNotContain("correct-password");
    }

    @Test
    void shouldRotateRefreshTokenAndLinkPreviousToken() {
        UserToken previousToken = validRefreshToken();
        User user = enabledUser();
        when(tokenHasher.sha256(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
        when(userTokenDao.findByRefreshTokenHash("hash:refresh-old")).thenReturn(Optional.of(previousToken));
        when(userDao.getById("user-1")).thenReturn(user);
        when(idGenerator.nextId()).thenAnswer(invocation -> UUID.randomUUID().toString());
        when(userTokenDao.rotateRefreshToken(anyString(), anyString(), any(Long.class), anyString()))
                .thenReturn(true);
        when(permissionContextService.getByUserId("user-1")).thenReturn(permissionContext());

        AuthLoginVo result = authService.refresh("refresh-old", "127.0.0.1", "JUnit");

        ArgumentCaptor<UserToken> newTokenCaptor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenDao).save(newTokenCaptor.capture());
        UserToken newToken = newTokenCaptor.getValue();
        assertThat(newToken.getUserTokenId()).isNotBlank();
        assertThat(newToken.getSessionFamilyId()).isEqualTo("family-1");
        assertThat(newToken.getRefreshTokenHash()).isEqualTo("hash:" + result.getRefreshToken());
        assertThat(previousToken.getStatus()).isZero();
        assertThat(previousToken.getReplacedByTokenId()).isEqualTo(newToken.getUserTokenId());
        assertThat(previousToken.getLastUsedAt()).isNotNull();
        verify(userTokenDao).rotateRefreshToken(previousToken.getUserTokenId(), newToken.getUserTokenId(),
                previousToken.getLastUsedAt(), "ROTATED");
        assertThat(result.getPermissionCodes()).containsExactly("system:user:view");
    }

    @Test
    void shouldRejectExpiredRefreshTokenAndRevokeIt() {
        UserToken expiredToken = validRefreshToken();
        expiredToken.setRefreshExpiresAt(System.currentTimeMillis() - 1);
        when(tokenHasher.sha256("refresh-old")).thenReturn("hash:refresh-old");
        when(userTokenDao.findByRefreshTokenHash("hash:refresh-old")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refresh("refresh-old", "127.0.0.1", "JUnit"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.SESSION_EXPIRED));
        assertThat(expiredToken.getStatus()).isZero();
        assertThat(expiredToken.getRevokedAt()).isNotNull();
        verify(userTokenDao).updateById(expiredToken);
        verify(userTokenDao, never()).save(any());
        ArgumentCaptor<LoginLog> loginLogCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRecorder).record(loginLogCaptor.capture());
        assertThat(loginLogCaptor.getValue())
                .extracting("userId", "loginType", "success", "errorCode")
                .containsExactly("user-1", "REFRESH", 0, "A401");
    }

    @Test
    void shouldRevokeSessionFamilyWhenRotatedTokenIsReused() {
        UserToken reusedToken = validRefreshToken();
        reusedToken.setStatus(0);
        reusedToken.setReplacedByTokenId("replacement-id");
        when(tokenHasher.sha256("refresh-old")).thenReturn("hash:refresh-old");
        when(userTokenDao.findByRefreshTokenHash("hash:refresh-old")).thenReturn(Optional.of(reusedToken));

        assertThatThrownBy(() -> authService.refresh("refresh-old", "127.0.0.1", "JUnit"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.REFRESH_TOKEN_REUSED));
        verify(userTokenDao, times(1)).revokeFamily(anyString(), any(Long.class), anyString());
        verify(userTokenDao, never()).save(any());
    }

    @Test
    void shouldRejectSecondConcurrentRefreshWhenAtomicClaimFails() {
        UserToken previousToken = validRefreshToken();
        User user = enabledUser();
        when(tokenHasher.sha256(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
        when(userTokenDao.findByRefreshTokenHash("hash:refresh-old")).thenReturn(Optional.of(previousToken));
        when(userDao.getById("user-1")).thenReturn(user);
        when(idGenerator.nextId()).thenAnswer(invocation -> UUID.randomUUID().toString());
        when(userTokenDao.rotateRefreshToken(anyString(), anyString(), any(Long.class), anyString()))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("refresh-old", "127.0.0.1", "JUnit"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.REFRESH_TOKEN_REUSED));
        verify(userTokenDao).revokeFamily(anyString(), any(Long.class), anyString());
        verify(userTokenDao, never()).save(any());
    }

    @Test
    void shouldRevokeWholeSessionFamilyOnLogout() {
        UserToken token = validRefreshToken();
        when(tokenHasher.sha256("refresh-old")).thenReturn("hash:refresh-old");
        when(userTokenDao.findByRefreshTokenHash("hash:refresh-old")).thenReturn(Optional.of(token));

        authService.logout("refresh-old");

        verify(userTokenDao).revokeFamily(anyString(), any(Long.class), anyString());
    }

    @Test
    void shouldReturnCurrentSessionWithPermissionContext() {
        User user = enabledUser();
        when(userDao.getById("user-1")).thenReturn(user);
        when(permissionContextService.getByUserId("user-1")).thenReturn(permissionContext());
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());

        AuthSessionVo session = authService.current();

        assertThat(session.getUser().getUserId()).isEqualTo("user-1");
        assertThat(session.getRoles()).extracting("roleCode").containsExactly("admin");
        assertThat(session.getMenuTree()).extracting("functionId").containsExactly("menu-system");
        assertThat(session.getPermissionCodes()).containsExactly("system:user:view");
        assertThat(session.getPermissionVersion()).isEqualTo(7L);
    }

    private User enabledUser() {
        User user = new User();
        user.setUserId("user-1");
        user.setUsername("yuanbao");
        user.setPasswordHash("encoded-password");
        user.setStatus(1);
        user.setPermissionVersion(7L);
        return user;
    }

    private UserPermissionContextVo permissionContext() {
        UserPermissionContextVo context = new UserPermissionContextVo();
        RoleVo role = new RoleVo();
        role.setRoleId("role-1");
        role.setRoleCode("admin");
        context.setRoles(List.of(role));
        PermissionMenuVo menu = new PermissionMenuVo();
        menu.setFunctionId("menu-system");
        context.setMenuTree(List.of(menu));
        context.setPermissionCodes(Set.of("system:user:view"));
        return context;
    }

    private AuthLoginRequest loginRequest() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("yuanbao");
        request.setPassword("correct-password");
        request.setClientType("WEB");
        return request;
    }

    private UserToken validRefreshToken() {
        UserToken token = new UserToken();
        token.setUserTokenId("token-1");
        token.setUserId("user-1");
        token.setSessionFamilyId("family-1");
        token.setRefreshTokenHash("hash:refresh-old");
        token.setRefreshExpiresAt(System.currentTimeMillis() + 60_000);
        token.setDeviceName("JUnit");
        token.setClientType("WEB");
        token.setStatus(1);
        return token;
    }
}
