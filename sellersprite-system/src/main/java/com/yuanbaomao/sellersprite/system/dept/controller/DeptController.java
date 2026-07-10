package com.yuanbaomao.sellersprite.system.dept.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.base.constants.SystemConstants;
import com.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import com.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import com.yuanbaomao.sellersprite.system.dept.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
