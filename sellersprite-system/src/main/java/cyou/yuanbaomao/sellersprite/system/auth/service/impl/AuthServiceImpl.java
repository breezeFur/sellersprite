package cyou.yuanbaomao.sellersprite.system.auth.service.impl;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.db.entity.UserToken;
import cyou.yuanbaomao.sellersprite.framework.security.TokenHasher;
import cyou.yuanbaomao.sellersprite.system.auth.constants.AuthConstants;
import cyou.yuanbaomao.sellersprite.system.auth.enums.LoginType;
import cyou.yuanbaomao.sellersprite.system.auth.enums.TokenStatus;
import cyou.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import cyou.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import cyou.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;
import cyou.yuanbaomao.sellersprite.system.auth.service.AuthService;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.convert.SystemConverter;
import cyou.yuanbaomao.sellersprite.system.ops.login.service.LoginLogRecorder;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import cyou.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final UserTokenDao userTokenDao;
    private final LoginLogRecorder loginLogRecorder;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;
    private final TokenHasher tokenHasher;
    private final PermissionContextService permissionContextService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVo login(AuthLoginRequest request, String loginIp, String userAgent) {
        User user = userDao.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            saveLoginLog(user, request.getUsername(), false, ResultCode.UNAUTHORIZED.getCode(),
                    SystemBusinessConstants.PASSWORD_LOGIN_FAILURE, loginIp, userAgent, request);
            throw new BizException(ResultCode.UNAUTHORIZED, SystemBusinessConstants.PASSWORD_LOGIN_FAILURE);
        }
        if (!Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(user.getStatus())) {
            saveLoginLog(user, request.getUsername(), false, ResultCode.UNAUTHORIZED.getCode(),
                    SystemBusinessConstants.ACCOUNT_DISABLED_REASON, loginIp, userAgent, request);
            throw new BizException(ResultCode.UNAUTHORIZED, SystemBusinessConstants.PASSWORD_LOGIN_FAILURE);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            saveLoginLog(user, request.getUsername(), false, ResultCode.UNAUTHORIZED.getCode(),
                    SystemBusinessConstants.PASSWORD_LOGIN_FAILURE, loginIp, userAgent, request);
            throw new BizException(ResultCode.UNAUTHORIZED, SystemBusinessConstants.PASSWORD_LOGIN_FAILURE);
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + AuthConstants.ACCESS_TOKEN_TTL.toMillis();
        long refreshExpiresAt = now + AuthConstants.REFRESH_TOKEN_TTL.toMillis();
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
        applyPermissionContext(vo, user);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVo refresh(String refreshToken, String loginIp, String userAgent) {
        UserToken previousToken = null;
        User user = null;
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new BizException(ResultCode.SESSION_EXPIRED);
            }
            previousToken = userTokenDao.findByRefreshTokenHash(tokenHasher.sha256(refreshToken))
                    .orElseThrow(() -> new BizException(ResultCode.SESSION_EXPIRED));
            long now = System.currentTimeMillis();
            if (!TokenStatus.VALID.getCode().equals(previousToken.getStatus())
                    || previousToken.getRevokedAt() != null) {
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

            user = userDao.getById(previousToken.getUserId());
            if (user == null || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(user.getStatus())) {
                userTokenDao.revokeFamily(previousToken.getSessionFamilyId(), now,
                        SystemBusinessConstants.TOKEN_REVOKE_REASON_EXPIRED);
                throw new BizException(ResultCode.UNAUTHORIZED);
            }

            long expiresAt = now + AuthConstants.ACCESS_TOKEN_TTL.toMillis();
            long refreshExpiresAt = now + AuthConstants.REFRESH_TOKEN_TTL.toMillis();
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
            applyPermissionContext(vo, user);
            return vo;
        } catch (BizException exception) {
            saveRefreshFailureLog(previousToken, user, exception, loginIp, userAgent);
            throw exception;
        }
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
        UserPermissionContextVo permissionContext = permissionContextService.getByUserId(userId);
        session.setRoles(permissionContext.getRoles());
        session.setMenuTree(permissionContext.getMenuTree());
        session.setPermissionCodes(permissionContext.getPermissionCodes());
        return session;
    }

    private void applyPermissionContext(AuthLoginVo login, User user) {
        UserPermissionContextVo permissionContext = permissionContextService.getByUserId(user.getUserId());
        login.setRoles(permissionContext.getRoles());
        login.setMenuTree(permissionContext.getMenuTree());
        login.setPermissionCodes(permissionContext.getPermissionCodes());
        login.setPermissionVersion(user.getPermissionVersion() == null ? 0L : user.getPermissionVersion());
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
        loginLog.setTraceId(RequestContextHolder.currentTraceId());
        loginLogRecorder.record(loginLog);
    }

    private void saveRefreshFailureLog(UserToken token, User user, BizException exception,
                                       String loginIp, String userAgent) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user == null ? token == null ? null : token.getUserId() : user.getUserId());
        loginLog.setUsername(user == null ? "" : defaultString(user.getUsername()));
        loginLog.setLoginType(LoginType.REFRESH.getCode());
        loginLog.setSuccess(SystemBusinessConstants.NO);
        loginLog.setErrorCode(exception.getCode());
        loginLog.setFailureReason(defaultString(exception.getMessage()));
        loginLog.setLoginIp(defaultString(loginIp));
        loginLog.setLoginLocation("");
        loginLog.setUserAgent(defaultString(userAgent));
        loginLog.setDeviceName(token == null ? "" : defaultString(token.getDeviceName()));
        loginLog.setClientType(token == null
                ? SystemBusinessConstants.DEFAULT_CLIENT_TYPE
                : defaultIfBlank(token.getClientType(), SystemBusinessConstants.DEFAULT_CLIENT_TYPE));
        loginLog.setTraceId(RequestContextHolder.currentTraceId());
        loginLogRecorder.record(loginLog);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
