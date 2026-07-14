package com.yuanbaomao.sellersprite.system.ops.login.service;

import com.yuanbaomao.sellersprite.db.entity.LoginLog;

public interface LoginLogRecorder {

    void record(LoginLog loginLog);
}
