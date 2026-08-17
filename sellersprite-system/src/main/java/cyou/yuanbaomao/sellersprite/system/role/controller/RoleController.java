package cyou.yuanbaomao.sellersprite.system.role.controller;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RolePermissionReplaceRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleStatusUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import cyou.yuanbaomao.sellersprite.system.role.service.RoleService;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "分页查询角色")
    @GetMapping
    public Result<YPage<RoleVo>> page(@Valid YPage<RoleVo> page,
            @Parameter(description = "角色名称，支持模糊查询")
            @RequestParam(value = "roleName", required = false) String roleName,
            @Parameter(description = "角色状态：1 启用，0 禁用")
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(roleService.page(page, roleName, status));
    }

    @Operation(summary = "查询启用角色选项")
    @GetMapping("/enabled")
    public Result<List<RoleVo>> listEnabled() {
        return Result.success(roleService.listEnabled());
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/{roleId}")
    public Result<RoleVo> detail(@PathVariable String roleId) {
        return Result.success(roleService.detail(roleId));
    }

    @Operation(summary = "编辑角色")
    @PutMapping("/{roleId}")
    public Result<RoleVo> update(@PathVariable String roleId, @Valid @RequestBody RoleUpdateRequest request) {
        return Result.success(roleService.update(roleId, request));
    }

    @Operation(summary = "更新角色状态")
    @PutMapping("/{roleId}/status")
    public Result<Void> updateStatus(@PathVariable String roleId,
            @Valid @RequestBody RoleStatusUpdateRequest request) {
        roleService.updateStatus(roleId, request.getStatus());
        return Result.success();
    }

    @Operation(summary = "分页查询角色用户")
    @GetMapping("/{roleId}/users")
    public Result<YPage<UserDetailVo>> listUsers(
            @Parameter(description = "角色 ID") @PathVariable String roleId,
            @Valid YPage<UserDetailVo> page,
            @Parameter(description = "用户名，支持模糊查询")
            @RequestParam(value = "username", required = false) String username) {
        return Result.success(roleService.listUsers(roleId, page, username));
    }

    @Operation(summary = "解除用户角色绑定")
    @DeleteMapping("/{roleId}/users/{userId}")
    public Result<Void> unbindUser(@PathVariable String roleId, @PathVariable String userId) {
        roleService.unbindUser(roleId, userId);
        return Result.success();
    }

    @Operation(summary = "查询角色权限和有效接口来源")
    @GetMapping("/{roleId}/permissions")
    public Result<RolePermissionVo> getPermissions(@PathVariable String roleId) {
        return Result.success(roleService.getPermissions(roleId));
    }

    @Operation(summary = "替换角色权限")
    @PutMapping("/{roleId}/permissions")
    public Result<RolePermissionVo> replacePermissions(@PathVariable String roleId,
            @Valid @RequestBody RolePermissionReplaceRequest request) {
        return Result.success(roleService.replacePermissions(roleId, request));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{roleId}")
    public Result<Void> delete(@PathVariable String roleId) {
        roleService.delete(roleId);
        return Result.success();
    }

    @Operation(summary = "绑定用户角色")
    @PostMapping("/user-bindings")
    public Result<Void> bindUserRole(@Valid @RequestBody UserRoleBindRequest request) {
        roleService.bindUserRole(request);
        return Result.success();
    }
}
