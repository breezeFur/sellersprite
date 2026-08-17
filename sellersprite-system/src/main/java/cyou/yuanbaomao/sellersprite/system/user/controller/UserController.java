package cyou.yuanbaomao.sellersprite.system.user.controller;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserPasswordResetRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserRoleReplaceRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserStatusUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import cyou.yuanbaomao.sellersprite.system.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<UserDetailVo> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{userId}")
    public Result<UserDetailVo> detail(@PathVariable String userId) {
        return Result.success(userService.detail(userId));
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<YPage<UserDetailVo>> page(@Valid YPage<UserDetailVo> page,
            @Parameter(description = "用户名，支持模糊查询")
            @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "用户状态：1 启用，0 禁用")
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(userService.page(page, username, status));
    }

    @Operation(summary = "编辑用户")
    @PutMapping("/{userId}")
    public Result<UserDetailVo> update(@PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.update(userId, request));
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateStatus(userId, request.getStatus());
        return Result.success();
    }

    @Operation(summary = "替换用户角色")
    @PutMapping("/{userId}/roles")
    public Result<Void> replaceRoles(@PathVariable String userId,
            @Valid @RequestBody UserRoleReplaceRequest request) {
        userService.replaceRoles(userId, request.getRoleIds());
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{userId}/password")
    public Result<Void> resetPassword(@PathVariable String userId,
            @Valid @RequestBody UserPasswordResetRequest request) {
        userService.resetPassword(userId, request.getPassword());
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable String userId) {
        userService.delete(userId);
        return Result.success();
    }
}
