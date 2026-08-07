package cyou.yuanbaomao.sellersprite.system.ops.login.controller;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.login.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "登录日志", description = "查询登录与会话刷新审计日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs/login")
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping
    public Result<PageResult<LoginLogVo>> page(@Valid LoginLogPageRequest request) {
        return Result.success(loginLogService.page(request));
    }

    @Operation(summary = "查询登录日志详情")
    @GetMapping("/{loginLogId}")
    public Result<LoginLogVo> detail(@PathVariable String loginLogId) {
        return Result.success(loginLogService.detail(loginLogId));
    }
}
