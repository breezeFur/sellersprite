package com.yuanbaomao.sellersprite.system.ops.login.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import com.yuanbaomao.sellersprite.db.entity.LoginLog;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

class LoginLogRecorderImplTest {

    @Test
    void shouldPersistInRequiresNewTransaction() throws Exception {
        LoginLogDao loginLogDao = mock(LoginLogDao.class);
        LoginLogRecorderImpl recorder = new LoginLogRecorderImpl(loginLogDao);
        LoginLog log = new LoginLog();
        log.setFailureReason(" token=secret ");

        recorder.record(log);

        verify(loginLogDao).save(log);
        assertThat(log.getFailureReason()).contains("[REDACTED]").doesNotContain("secret");
        Method method = LoginLogRecorderImpl.class.getMethod("record", LoginLog.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }
}
