package com.yuanbaomao.sellersprite.system.dashboard.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "首页概览")
public class DashboardOverviewVo {
    private Long userCount = 0L;
    private Long enabledRoleCount = 0L;
    private Long departmentCount = 0L;
    private Long dictTypeCount = 0L;
    private Long todayAiConversationCount = 0L;
    private Long todayFailedOperationCount = 0L;
    private List<DashboardTrendPointVo> trends = new ArrayList<>();
    private List<DashboardActivityVo> recentActivities = new ArrayList<>();
}
