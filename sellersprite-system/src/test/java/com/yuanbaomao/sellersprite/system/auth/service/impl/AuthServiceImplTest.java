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
import com.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.sellersprite.framework.security.TokenHasher;
import com.yuanbaomao.sellersprite.system.auth.config.AuthProperties;
import com.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
    private LoginLogDao loginLogDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private TokenHasher tokenHasher;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenExpireMinutes(5L);
        properties.setRefreshTokenExpireDays(7L);
        authService = new AuthServiceImpl(userDao, userTokenDao, loginLogDao, passwordEncoder, idGenerator,
                properties, tokenHasher);
    }

    @Test
    void shouldCreateSeparateAccessAndRefreshExpirationsForEnabledUser() {
        User user = enabledUser();
        when(userDao.findByUsername("yuanbao")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", user.getPasswordHash())).thenReturn(true);
        when(idGenerator.nextId()).thenAnswer(invocation -> UUID.randomUUID().toString());
        when(tokenHasher.sha256(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));

        AuthLoginVo result = authService.login(loginRequest(), "127.0.0.1", "JUnit");

        ArgumentCaptor<UserToken> tokenCaptor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenDao).save(tokenCaptor.capture());
        UserToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getSessionFamilyId()).isNotBlank();
        assertThat(savedToken.getRefreshExpiresAt()).isGreaterThan(savedToken.getExpiresAt());
        assertThat(savedToken.getRefreshTokenHash()).isEqualTo("hash:" + result.getRefreshToken());
        assertThat(result.getAccessToken()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotBlank();
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

    private User enabledUser() {
        User user = new User();
        user.setUserId("user-1");
        user.setUsername("yuanbao");
        user.setPasswordHash("encoded-password");
        user.setStatus(1);
        return user;
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
