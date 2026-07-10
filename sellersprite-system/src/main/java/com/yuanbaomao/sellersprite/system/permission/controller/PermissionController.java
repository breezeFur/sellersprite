package com.yuanbaomao.sellersprite.system.permission.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionService;
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

@Tag(name = "权限管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "创建系统功能")
    @PostMapping("/functions")
    public Result<SysFunctionVo> createFunction(@Valid @RequestBody SysFunctionCreateRequest request) {
        return Result.success(permissionService.createFunction(request));
    }

    @Operation(summary = "查询系统功能")
    @GetMapping("/functions")
    public Result<List<SysFunctionVo>> listFunctions() {
        return Result.success(permissionService.listFunctions());
    }

    @Operation(summary = "创建系统接口")
    @PostMapping("/apis")
    public Result<SysApiVo> createApi(@Valid @RequestBody SysApiCreateRequest request) {
        return Result.success(permissionService.createApi(request));
    }

    @Operation(summary = "查询公开接口")
    @GetMapping("/apis/public")
    public Result<List<SysApiVo>> listPublicApis() {
        return Result.success(permissionService.listPublicApis());
    }
}
