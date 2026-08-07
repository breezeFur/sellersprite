package cyou.yuanbaomao.sellersprite.system.dashboard.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.dashboard.model.vo.DashboardOverviewVo;
import cyou.yuanbaomao.sellersprite.system.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "首页概览")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "查询首页真实概览")
    @GetMapping("/overview")
    public Result<DashboardOverviewVo> overview() {
        return Result.success(dashboardService.overview());
    }
}
