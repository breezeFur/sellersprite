package cyou.yuanbaomao.sellersprite.system.ops.login.service.impl;

import cyou.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import cyou.yuanbaomao.sellersprite.framework.security.SensitiveDataMasker;
import cyou.yuanbaomao.sellersprite.system.ops.login.service.LoginLogRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginLogRecorderImpl implements LoginLogRecorder {

    private final LoginLogDao loginLogDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(LoginLog loginLog) {
        loginLog.setFailureReason(SensitiveDataMasker.mask(loginLog.getFailureReason()));
        loginLog.setUserAgent(SensitiveDataMasker.mask(loginLog.getUserAgent()));
        loginLog.setDeviceName(SensitiveDataMasker.mask(loginLog.getDeviceName()));
        loginLogDao.save(loginLog);
    }
}
