package com.yuanbaomao.sellersprite.system.auth.service.impl;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.LoginLog;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.id.IdGenerator;
import com.yuanbaomao.sellersprite.framework.security.TokenHasher;
import com.yuanbaomao.sellersprite.system.auth.config.AuthProperties;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.auth.enums.LoginType;
import com.yuanbaomao.sellersprite.system.auth.enums.TokenStatus;
import com.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;
import com.yuanbaomao.sellersprite.system.auth.service.AuthService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final UserTokenDao userTokenDao;
    private final LoginLogDao loginLogDao;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;
    private final AuthProperties authProperties;
    private final TokenHasher tokenHasher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVo login(AuthLoginRequest request, String loginIp, String userAgent) {
        User user = userDao.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(user.getStatus())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            saveLoginLog(user, request.getUsername(), false, ResultCode.UNAUTHORIZED.getCode(),
                    SystemBusinessConstants.PASSWORD_LOGIN_FAILURE, loginIp, userAgent, request);
            throw new BizException(ResultCode.UNAUTHORIZED, SystemBusinessConstants.PASSWORD_LOGIN_FAILURE);
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + Duration.ofMinutes(authProperties.getAccessTokenExpireMinutes()).toMillis();
        long refreshExpiresAt = now + Duration.ofDays(authProperties.getRefreshTokenExpireDays()).toMillis();
        String accessToken = idGenerator.nextId().replace("-", "") + idGenerator.nextId().replace("-", "");
        String refreshToken = idGenerator.nextId().replace("-", "") + idGenerator.nextId().replace("-", "");

        UserToken token = new UserToken();
        token.setUserTokenId(idGenerator.nextId());
        token.setUserId(user.getUserId());
        token.setAccessTokenHash(tokenHasher.sha256(accessToken));
        token.setRefreshTokenHash(tokenHasher.sha256(refreshToken));
        token.setSessionFamilyId(idGenerator.nextId());
        token.setTokenType(SystemBusinessConstants.TOKEN_TYPE_BEARER);
        token.setDeviceId(request.getDeviceId());
        token.setDeviceName(defaultString(request.getDeviceName()));
        token.setClientType(defaultIfBlank(request.getClientType(), SystemBusinessConstants.DEFAULT_CLIENT_TYPE));
        token.setLoginIp(defaultString(loginIp));
        token.setUserAgent(defaultString(userAgent));
        token.setIssuedAt(now);
        token.setExpiresAt(expiresAt);
        token.setRefreshExpiresAt(refreshExpiresAt);
        token.setStatus(TokenStatus.VALID.getCode());
        userTokenDao.save(token);

        user.setLastLoginAt(now);
        userDao.updateById(user);
        saveLoginLog(user, request.getUsername(), true, "", "", loginIp, userAgent, request);

        AuthLoginVo vo = new AuthLoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setTokenType(SystemBusinessConstants.TOKEN_TYPE_BEARER);
        vo.setExpiresAt(expiresAt);
        vo.setUser(SystemConverter.toUserDetailVo(user));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVo refresh(String refreshToken, String loginIp, String userAgent) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BizException(ResultCode.SESSION_EXPIRED);
        }
        UserToken previousToken = userTokenDao.findByRefreshTokenHash(tokenHasher.sha256(refreshToken))
                .orElseThrow(() -> new BizException(ResultCode.SESSION_EXPIRED));
        long now = System.currentTimeMillis();
        if (!TokenStatus.VALID.getCode().equals(previousToken.getStatus()) || previousToken.getRevokedAt() != null) {
            if (previousToken.getReplacedByTokenId() != null && !previousToken.getReplacedByTokenId().isBlank()) {
                userTokenDao.revokeFamily(previousToken.getSessionFamilyId(), now,
                        SystemBusinessConstants.TOKEN_REVOKE_REASON_REUSED);
                throw new BizException(ResultCode.REFRESH_TOKEN_REUSED);
            }
            throw new BizException(ResultCode.SESSION_EXPIRED);
        }
        if (previousToken.getRefreshExpiresAt() == null || previousToken.getRefreshExpiresAt() <= now) {
            revokeToken(previousToken, now, SystemBusinessConstants.TOKEN_REVOKE_REASON_EXPIRED);
            throw new BizException(ResultCode.SESSION_EXPIRED);
        }

        User user = userDao.getById(previousToken.getUserId());
        if (user == null || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(user.getStatus())) {
            userTokenDao.revokeFamily(previousToken.getSessionFamilyId(), now,
                    SystemBusinessConstants.TOKEN_REVOKE_REASON_EXPIRED);
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        long expiresAt = now + Duration.ofMinutes(authProperties.getAccessTokenExpireMinutes()).toMillis();
        long refreshExpiresAt = now + Duration.ofDays(authProperties.getRefreshTokenExpireDays()).toMillis();
        String accessToken = newOpaqueToken();
        String newRefreshToken = newOpaqueToken();
        UserToken newToken = new UserToken();
        newToken.setUserTokenId(idGenerator.nextId());
        newToken.setUserId(user.getUserId());
        newToken.setAccessTokenHash(tokenHasher.sha256(accessToken));
        newToken.setRefreshTokenHash(tokenHasher.sha256(newRefreshToken));
        newToken.setSessionFamilyId(previousToken.getSessionFamilyId());
        newToken.setTokenType(SystemBusinessConstants.TOKEN_TYPE_BEARER);
        newToken.setDeviceId(previousToken.getDeviceId());
        newToken.setDeviceName(defaultString(previousToken.getDeviceName()));
        newToken.setClientType(defaultIfBlank(previousToken.getClientType(),
                SystemBusinessConstants.DEFAULT_CLIENT_TYPE));
        newToken.setLoginIp(defaultString(loginIp));
        newToken.setUserAgent(defaultString(userAgent));
        newToken.setIssuedAt(now);
        newToken.setExpiresAt(expiresAt);
        newToken.setRefreshExpiresAt(refreshExpiresAt);
        newToken.setStatus(TokenStatus.VALID.getCode());
        previousToken.setLastUsedAt(now);
        previousToken.setReplacedByTokenId(newToken.getUserTokenId());
        previousToken.setStatus(TokenStatus.INVALID.getCode());
        previousToken.setRevokedAt(now);
        previousToken.setRevokeReason(SystemBusinessConstants.TOKEN_REVOKE_REASON_ROTATED);
        boolean rotated = userTokenDao.rotateRefreshToken(previousToken.getUserTokenId(), newToken.getUserTokenId(),
                now, SystemBusinessConstants.TOKEN_REVOKE_REASON_ROTATED);
        if (!rotated) {
            userTokenDao.revokeFamily(previousToken.getSessionFamilyId(), now,
                    SystemBusinessConstants.TOKEN_REVOKE_REASON_REUSED);
            throw new BizException(ResultCode.REFRESH_TOKEN_REUSED);
        }
        userTokenDao.save(newToken);

        AuthLoginVo vo = new AuthLoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(newRefreshToken);
        vo.setTokenType(SystemBusinessConstants.TOKEN_TYPE_BEARER);
        vo.setExpiresAt(expiresAt);
        vo.setUser(SystemConverter.toUserDetailVo(user));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        userTokenDao.findByRefreshTokenHash(tokenHasher.sha256(refreshToken))
                .ifPresent(token -> userTokenDao.revokeFamily(token.getSessionFamilyId(), System.currentTimeMillis(),
                        SystemBusinessConstants.TOKEN_REVOKE_REASON_LOGOUT));
    }

    @Override
    public AuthSessionVo current() {
        String userId = RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
        User user = userDao.getById(userId);
        if (user == null || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(user.getStatus())) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        AuthSessionVo session = new AuthSessionVo();
        session.setUser(SystemConverter.toUserDetailVo(user));
        session.setPermissionVersion(user.getPermissionVersion() == null ? 0L : user.getPermissionVersion());
        return session;
    }

    private void revokeToken(UserToken token, long revokedAt, String reason) {
        token.setStatus(TokenStatus.INVALID.getCode());
        token.setRevokedAt(revokedAt);
        token.setRevokeReason(reason);
        userTokenDao.updateById(token);
    }

    private String newOpaqueToken() {
        return idGenerator.nextId().replace("-", "") + idGenerator.nextId().replace("-", "");
    }

    private void saveLoginLog(User user, String username, boolean success, String errorCode, String failureReason,
            String loginIp, String userAgent, AuthLoginRequest request) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user == null ? null : user.getUserId());
        loginLog.setUsername(defaultString(username));
        loginLog.setLoginType(LoginType.PASSWORD.getCode());
        loginLog.setSuccess(success ? SystemBusinessConstants.YES : SystemBusinessConstants.NO);
        loginLog.setErrorCode(defaultString(errorCode));
        loginLog.setFailureReason(defaultString(failureReason));
        loginLog.setLoginIp(defaultString(loginIp));
        loginLog.setLoginLocation("");
        loginLog.setUserAgent(defaultString(userAgent));
        loginLog.setDeviceName(defaultString(request.getDeviceName()));
        loginLog.setClientType(defaultIfBlank(request.getClientType(), SystemBusinessConstants.DEFAULT_CLIENT_TYPE));
        loginLog.setTrackId(RequestContextHolder.currentTrackId());
        loginLogDao.save(loginLog);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
