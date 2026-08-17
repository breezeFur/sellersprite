package cyou.yuanbaomao.sellersprite.system.dashboard.service.impl;

import cyou.yuanbaomao.dict.core.DictTemplate;
import cyou.yuanbaomao.dict.mybatis.entity.DictTypeEntity;
import cyou.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import cyou.yuanbaomao.sellersprite.db.dao.DeptDao;
import cyou.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import cyou.yuanbaomao.sellersprite.db.dao.OperationLogQueryDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.dashboard.model.vo.DashboardActivityVo;
import cyou.yuanbaomao.sellersprite.system.dashboard.model.vo.DashboardOverviewVo;
import cyou.yuanbaomao.sellersprite.system.dashboard.model.vo.DashboardTrendPointVo;
import cyou.yuanbaomao.sellersprite.system.dashboard.service.DashboardService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final int TREND_DAYS = 7;
    private static final int RECENT_LIMIT = 5;
    private static final int MERGED_ACTIVITY_LIMIT = 8;

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final DeptDao deptDao;
    private final DictTemplate dictTemplate;
    private final AiConversationDao aiConversationDao;
    private final AiPromptRecordDao aiPromptRecordDao;
    private final LoginLogDao loginLogDao;
    private final OperationLogQueryDao operationLogQueryDao;
    private final Clock clock;

    @Override
    public DashboardOverviewVo overview() {
        LocalDate today = LocalDate.now(clock);
        long todayStart = startOfDay(today);
        long tomorrowStart = startOfDay(today.plusDays(1));
        DashboardOverviewVo result = new DashboardOverviewVo();
        result.setUserCount(userDao.count());
        result.setEnabledRoleCount(roleDao.countByStatus(SystemBusinessConstants.STATUS_ENABLED));
        result.setDepartmentCount(deptDao.count());
        result.setDictTypeCount(dictTypeCount());
        result.setTodayAiConversationCount(aiConversationDao.countByCreatedAtRange(todayStart, tomorrowStart));
        result.setTodayFailedOperationCount(failedOperationCount(todayStart, tomorrowStart));
        result.setTrends(buildTrends(today));
        result.setRecentActivities(buildRecentActivities());
        return result;
    }

    private long dictTypeCount() {
        YPage<DictTypeEntity> page = YPage.of(1, 1);
        return dictTemplate.pageTypes(page, null, null, null, DictTypeEntity.class).getTotal();
    }

    private long failedOperationCount(long startTime, long endTime) {
        return operationLogQueryDao.countFailedByCreatedAtRange(startTime, endTime);
    }

    private List<DashboardTrendPointVo> buildTrends(LocalDate today) {
        List<DashboardTrendPointVo> trends = new ArrayList<>();
        for (int offset = TREND_DAYS - 1; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);
            long start = startOfDay(date);
            long end = startOfDay(date.plusDays(1));
            long loginCount = loginLogDao.countByCreatedAtRange(start, end);
            long conversationCount = aiConversationDao.countByCreatedAtRange(start, end);
            long callCount = aiPromptRecordDao.countByCreatedAtRange(start, end);
            if (loginCount + conversationCount + callCount == 0) {
                continue;
            }
            DashboardTrendPointVo point = new DashboardTrendPointVo();
            point.setDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            point.setLoginCount(loginCount);
            point.setAiConversationCount(conversationCount);
            point.setAiCallCount(callCount);
            trends.add(point);
        }
        return trends;
    }

    private List<DashboardActivityVo> buildRecentActivities() {
        List<DashboardActivityVo> activities = new ArrayList<>();
        loginLogDao.listRecent(RECENT_LIMIT).stream().map(this::loginActivity).forEach(activities::add);
        operationLogQueryDao.listRecent(RECENT_LIMIT).stream()
                .map(this::operationActivity).forEach(activities::add);
        return activities.stream()
                .sorted(Comparator.comparing(DashboardActivityVo::getOccurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MERGED_ACTIVITY_LIMIT)
                .toList();
    }

    private DashboardActivityVo loginActivity(LoginLog log) {
        DashboardActivityVo activity = new DashboardActivityVo();
        activity.setType("LOGIN");
        activity.setTitle("用户登录");
        activity.setDescription(log.getUsername());
        activity.setSuccess(log.getSuccess());
        activity.setOccurredAt(log.getCreatedAt());
        activity.setTraceId(log.getTraceId());
        return activity;
    }

    private DashboardActivityVo operationActivity(OperationLogEntity log) {
        DashboardActivityVo activity = new DashboardActivityVo();
        activity.setType("OPERATION");
        activity.setTitle(log.getOperationName());
        activity.setDescription(log.getModuleName());
        activity.setSuccess(log.getSuccess());
        activity.setOccurredAt(log.getCreatedAt());
        activity.setTraceId(log.getTraceId());
        return activity;
    }

    private long startOfDay(LocalDate date) {
        return date.atStartOfDay(clock.getZone()).toInstant().toEpochMilli();
    }
}
