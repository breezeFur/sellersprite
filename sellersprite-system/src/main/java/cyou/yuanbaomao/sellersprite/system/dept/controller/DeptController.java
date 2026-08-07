package cyou.yuanbaomao.sellersprite.system.dept.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.base.constants.SystemConstants;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptStatusUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import cyou.yuanbaomao.sellersprite.system.dept.service.DeptService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "部门管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/depts")
public class DeptController {

    private final DeptService deptService;

    @Operation(summary = "创建部门")
    @PostMapping
    public Result<DeptVo> create(@Valid @RequestBody DeptCreateRequest request) {
        return Result.success(deptService.create(request));
    }

    @Operation(summary = "按父部门查询部门")
    @GetMapping
    public Result<List<DeptVo>> list(@RequestParam(defaultValue = SystemConstants.ROOT_PARENT_ID) String parentId) {
        return Result.success(deptService.listByParentId(parentId));
    }

    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    public Result<List<DeptVo>> tree() {
        return Result.success(deptService.tree());
    }

    @Operation(summary = "查询部门详情")
    @GetMapping("/{deptId}")
    public Result<DeptVo> detail(@PathVariable String deptId) {
        return Result.success(deptService.detail(deptId));
    }

    @Operation(summary = "编辑部门")
    @PutMapping("/{deptId}")
    public Result<DeptVo> update(@PathVariable String deptId,
                                 @Valid @RequestBody DeptUpdateRequest request) {
        return Result.success(deptService.update(deptId, request));
    }

    @Operation(summary = "更新部门状态")
    @PutMapping("/{deptId}/status")
    public Result<Void> updateStatus(@PathVariable String deptId,
                                     @Valid @RequestBody DeptStatusUpdateRequest request) {
        deptService.updateStatus(deptId, request.getStatus());
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{deptId}")
    public Result<Void> delete(@PathVariable String deptId) {
        deptService.delete(deptId);
        return Result.success();
    }
}
