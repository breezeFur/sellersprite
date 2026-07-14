package com.yuanbaomao.sellersprite.system.permission.controller;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.permission.model.dto.FunctionApiReplaceRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingSyncRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.PermissionStatusUpdateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiPageRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiUpdateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionUpdateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.ApiCatalogSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.MenuApiBindingSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.service.ApiCatalogService;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "权限管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final ApiCatalogService apiCatalogService;

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

    @GetMapping("/functions/tree")
    public Result<List<SysFunctionVo>> functionTree() { return Result.success(permissionService.functionTree()); }

    @GetMapping("/functions/{functionId}")
    public Result<SysFunctionVo> functionDetail(@PathVariable String functionId) { return Result.success(permissionService.functionDetail(functionId)); }

    @PutMapping("/functions/{functionId}")
    public Result<SysFunctionVo> updateFunction(@PathVariable String functionId, @Valid @RequestBody SysFunctionUpdateRequest request) { return Result.success(permissionService.updateFunction(functionId, request)); }

    @PutMapping("/functions/{functionId}/status")
    public Result<Void> updateFunctionStatus(@PathVariable String functionId, @Valid @RequestBody PermissionStatusUpdateRequest request) { permissionService.updateFunctionStatus(functionId, request.getStatus()); return Result.success(); }

    @GetMapping("/functions/{functionId}/apis")
    public Result<List<String>> getFunctionApis(@PathVariable String functionId) { return Result.success(permissionService.getFunctionApiIds(functionId)); }

    @PutMapping("/functions/{functionId}/apis")
    public Result<Void> replaceFunctionApis(@PathVariable String functionId, @Valid @RequestBody FunctionApiReplaceRequest request) { permissionService.replaceFunctionApis(functionId, request.getApiIds()); return Result.success(); }

    @DeleteMapping("/functions/{functionId}")
    public Result<Void> deleteFunction(@PathVariable String functionId) { permissionService.deleteFunction(functionId); return Result.success(); }

    @Operation(summary = "同步菜单接口绑定")
    @PutMapping("/functions/api-bindings/sync")
    public Result<MenuApiBindingSyncResultVo> syncMenuApiBindings(
            @Valid @RequestBody MenuApiBindingSyncRequest request) {
        return Result.success(apiCatalogService.syncMenuBindings(request));
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

    @GetMapping("/apis")
    public Result<PageResult<SysApiVo>> pageApis(@Valid SysApiPageRequest request) { return Result.success(permissionService.pageApis(request)); }

    @GetMapping("/apis/{apiId}")
    public Result<SysApiVo> apiDetail(@PathVariable String apiId) { return Result.success(permissionService.apiDetail(apiId)); }

    @PutMapping("/apis/{apiId}")
    public Result<SysApiVo> updateApi(@PathVariable String apiId, @Valid @RequestBody SysApiUpdateRequest request) { return Result.success(permissionService.updateApi(apiId, request)); }

    @PutMapping("/apis/{apiId}/status")
    public Result<Void> updateApiStatus(@PathVariable String apiId, @Valid @RequestBody PermissionStatusUpdateRequest request) { permissionService.updateApiStatus(apiId, request.getStatus()); return Result.success(); }

    @DeleteMapping("/apis/{apiId}")
    public Result<Void> deleteApi(@PathVariable String apiId) { permissionService.deleteApi(apiId); return Result.success(); }

    @Operation(summary = "装载后端接口目录")
    @PostMapping("/apis/catalog/sync")
    public Result<ApiCatalogSyncResultVo> syncApiCatalog() {
        return Result.success(apiCatalogService.syncCatalog());
    }
}
