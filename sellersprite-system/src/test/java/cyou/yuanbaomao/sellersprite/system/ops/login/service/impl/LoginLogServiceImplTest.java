package cyou.yuanbaomao.sellersprite.system.ops.login.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginLogServiceImplTest {

    @Mock
    private LoginLogDao loginLogDao;

    @InjectMocks
    private LoginLogServiceImpl loginLogService;

    @Test
    void shouldPageByUserResultIpAndTimeRange() {
        LoginLogPageRequest request = new LoginLogPageRequest();
        request.setUserId("user-1");
        request.setUsername("yuanbao");
        request.setSuccess(0);
        request.setLoginIp("127.0.0.1");
        request.setStartTime(100L);
        request.setEndTime(200L);
        request.setCurrent(2L);
        request.setSize(10L);
        LoginLog log = loginLog();
        Page<LoginLog> page = Page.of(2, 10, 1);
        page.setRecords(List.of(log));
        when(loginLogDao.page("user-1", "yuanbao", 0, "127.0.0.1", 100L, 200L, 2L, 10L))
                .thenReturn(page);

        cyou.yuanbaomao.base.result.PageResult<LoginLogVo> result = loginLogService.page(request);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).extracting(LoginLogVo::getLoginLogId)
                .containsExactly("login-1");
        verify(loginLogDao).page("user-1", "yuanbao", 0, "127.0.0.1", 100L, 200L, 2L, 10L);
    }

    @Test
    void shouldReturnDetailOrNotFound() {
        when(loginLogDao.findById("login-1")).thenReturn(Optional.of(loginLog()));
        when(loginLogDao.findById("missing")).thenReturn(Optional.empty());

        cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo detail = loginLogService.detail("login-1");
        assertThat(detail.getTrackId()).isEqualTo("track-1");
        assertThat(detail.getFailureReason()).contains("[REDACTED]").doesNotContain("secret");
        assertThatThrownBy(() -> loginLogService.detail("missing"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(ResultCode.RESOURCE_NOT_FOUND.getCode()));
    }

    @Test
    void shouldRejectReversedTimeRange() {
        LoginLogPageRequest request = new LoginLogPageRequest();
        request.setStartTime(200L);
        request.setEndTime(100L);

        assertThatThrownBy(() -> loginLogService.page(request))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(ResultCode.PARAM_INVALID.getCode()));
    }

    private LoginLog loginLog() {
        LoginLog log = new LoginLog();
        log.setLoginLogId("login-1");
        log.setUserId("user-1");
        log.setUsername("yuanbao");
        log.setLoginType("PASSWORD");
        log.setSuccess(0);
        log.setErrorCode("A401");
        log.setFailureReason(" token=secret ");
        log.setLoginIp("127.0.0.1");
        log.setUserAgent("JUnit");
        log.setClientType("WEB");
        log.setTrackId("track-1");
        log.setCreatedAt(150L);
        return log;
    }
}
