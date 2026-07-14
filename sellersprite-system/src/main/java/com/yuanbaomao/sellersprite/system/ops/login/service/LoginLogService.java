package com.yuanbaomao.sellersprite.system.ops.login.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;

public interface LoginLogService {

    PageResult<LoginLogVo> page(LoginLogPageRequest request);

    LoginLogVo detail(String loginLogId);
}
