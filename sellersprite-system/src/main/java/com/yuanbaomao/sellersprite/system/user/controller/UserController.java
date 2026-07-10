package com.yuanbaomao.sellersprite.system.user.controller;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPageRequest;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import com.yuanbaomao.sellersprite.system.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}
