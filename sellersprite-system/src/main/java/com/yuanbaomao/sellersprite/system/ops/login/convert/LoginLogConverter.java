package com.yuanbaomao.sellersprite.system.ops.login.convert;

import com.yuanbaomao.sellersprite.db.entity.LoginLog;
import com.yuanbaomao.sellersprite.framework.security.SensitiveDataMasker;
import com.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;

public final class LoginLogConverter {

    private LoginLogConverter() {
    }

    public static LoginLogVo toVo(LoginLog entity) {
        LoginLogVo vo = new LoginLogVo();
        vo.setLoginLogId(entity.getLoginLogId());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setLoginType(entity.getLoginType());
        vo.setSuccess(entity.getSuccess());
        vo.setErrorCode(entity.getErrorCode());
        vo.setFailureReason(SensitiveDataMasker.mask(entity.getFailureReason()));
        vo.setLoginIp(entity.getLoginIp());
        vo.setLoginLocation(entity.getLoginLocation());
        vo.setUserAgent(SensitiveDataMasker.mask(entity.getUserAgent()));
        vo.setDeviceName(SensitiveDataMasker.mask(entity.getDeviceName()));
        vo.setClientType(entity.getClientType());
        vo.setTrackId(entity.getTrackId());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
