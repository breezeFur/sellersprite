package cyou.yuanbaomao.sellersprite.system.ops.login.service;

import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;

public interface LoginLogRecorder {

    void record(LoginLog loginLog);
}
