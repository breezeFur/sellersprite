package cyou.yuanbaomao.sellersprite.system.ops.login.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;

public interface LoginLogService {

    PageResult<LoginLogVo> page(LoginLogPageRequest request);

    LoginLogVo detail(String loginLogId);
}
