package com.yuanbaomao.sellersprite.system.user.controller;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPageRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPasswordResetRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserRoleReplaceRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserStatusUpdateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import com.yuanbaomao.sellersprite.system.user.service.UserService;
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
    public Result<PageResult<UserDetailVo>> page(@Valid UserPageRequest request) {
        return Result.success(userService.page(request));
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
