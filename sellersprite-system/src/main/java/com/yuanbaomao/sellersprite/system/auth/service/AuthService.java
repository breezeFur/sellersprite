package com.yuanbaomao.sellersprite.system.auth.service;

import com.yuanbaomao.sellersprite.system.auth.model.dto.AuthLoginRequest;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthLoginVo;
import com.yuanbaomao.sellersprite.system.auth.model.vo.AuthSessionVo;

public interface AuthService {

    AuthLoginVo login(AuthLoginRequest request, String loginIp, String userAgent);

    AuthLoginVo refresh(String refreshToken, String loginIp, String userAgent);

    void logout(String refreshToken);

    AuthSessionVo current();
}
