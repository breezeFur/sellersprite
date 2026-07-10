package com.yuanbaomao.sellersprite.system.role.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "角色管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleVo> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(roleService.create(request));
    }

    @Operation(summary = "查询启用角色")
    @GetMapping
    public Result<List<RoleVo>> listEnabled() {
        return Result.success(roleService.listEnabled());
    }

    @Operation(summary = "绑定用户角色")
    @PostMapping("/user-bindings")
    public Result<Void> bindUserRole(@Valid @RequestBody UserRoleBindRequest request) {
        roleService.bindUserRole(request);
        return Result.success();
    }
}
