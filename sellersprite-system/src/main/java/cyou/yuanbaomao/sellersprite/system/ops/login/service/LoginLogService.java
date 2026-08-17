package cyou.yuanbaomao.sellersprite.system.ops.login.service;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;

public interface LoginLogService {

    YPage<LoginLogVo> page(YPage<LoginLogVo> page, LoginLogPageRequest request);

    LoginLogVo detail(String loginLogId);
}
