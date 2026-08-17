package cyou.yuanbaomao.sellersprite.system.dashboard.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.dict.core.DictTemplate;
import cyou.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import cyou.yuanbaomao.sellersprite.db.dao.DeptDao;
import cyou.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import cyou.yuanbaomao.sellersprite.db.dao.OperationLogQueryDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import cyou.yuanbaomao.sellersprite.system.dashboard.model.vo.DashboardOverviewVo;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock UserDao userDao;
    @Mock RoleDao roleDao;
    @Mock DeptDao deptDao;
    @Mock DictTemplate dictTemplate;
    @Mock AiConversationDao aiConversationDao;
    @Mock AiPromptRecordDao aiPromptRecordDao;
    @Mock LoginLogDao loginLogDao;
    @Mock OperationLogQueryDao operationLogQueryDao;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-10T04:00:00Z"), ZoneId.of("Asia/Shanghai"));
        when(dictTemplate.pageTypes(any(), any(), any(), any(), any())).thenReturn(YPage.empty(1, 1));
        service = new DashboardServiceImpl(userDao, roleDao, deptDao, dictTemplate, aiConversationDao,
                aiPromptRecordDao, loginLogDao, operationLogQueryDao, clock);
    }

    @Test
    void shouldReturnZeroMetricsAndEmptyCollectionsForEmptySystem() {
        DashboardOverviewVo result = service.overview();

        assertThat(result.getUserCount()).isZero();
        assertThat(result.getEnabledRoleCount()).isZero();
        assertThat(result.getTodayAiConversationCount()).isZero();
        assertThat(result.getTodayFailedOperationCount()).isZero();
        assertThat(result.getTrends()).isEmpty();
        assertThat(result.getRecentActivities()).isEmpty();
    }

    @Test
    void shouldAggregateRealMetricsTrendAndRecentActivities() {
        long todayStart = LocalDate.of(2026, 7, 10).atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .toInstant().toEpochMilli();
        long tomorrowStart = LocalDate.of(2026, 7, 11).atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .toInstant().toEpochMilli();
        when(userDao.count()).thenReturn(8L);
        when(roleDao.countByStatus(1)).thenReturn(3L);
        when(deptDao.count()).thenReturn(4L);
        when(dictTemplate.pageTypes(any(), any(), any(), any(), any())).thenReturn(YPage.of(1, 1, 5, List.of()));
        when(aiConversationDao.countByCreatedAtRange(anyLong(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(0, Long.class) == todayStart ? 2L : 0L);
        when(aiPromptRecordDao.countByCreatedAtRange(anyLong(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(0, Long.class) == todayStart ? 7L : 0L);
        when(loginLogDao.countByCreatedAtRange(anyLong(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(0, Long.class) == todayStart ? 6L : 0L);
        LoginLog login = new LoginLog();
        login.setUsername("alice"); login.setSuccess(1); login.setCreatedAt(100L); login.setTraceId("login-track");
        when(loginLogDao.listRecent(5)).thenReturn(List.of(login));
        OperationLogEntity operation = new OperationLogEntity();
        operation.setOperationName("更新角色权限"); operation.setSuccess(0); operation.setCreatedAt(200L);
        operation.setTraceId("operation-track");
        when(operationLogQueryDao.countFailedByCreatedAtRange(todayStart, tomorrowStart)).thenReturn(1L);
        when(operationLogQueryDao.listRecent(5)).thenReturn(List.of(operation));

        DashboardOverviewVo result = service.overview();

        assertThat(result.getUserCount()).isEqualTo(8);
        assertThat(result.getEnabledRoleCount()).isEqualTo(3);
        assertThat(result.getDepartmentCount()).isEqualTo(4);
        assertThat(result.getDictTypeCount()).isEqualTo(5);
        assertThat(result.getTodayAiConversationCount()).isEqualTo(2);
        assertThat(result.getTodayFailedOperationCount()).isEqualTo(1);
        assertThat(result.getTrends()).hasSize(1);
        assertThat(result.getRecentActivities()).extracting("type").containsExactly("OPERATION", "LOGIN");
    }
}
