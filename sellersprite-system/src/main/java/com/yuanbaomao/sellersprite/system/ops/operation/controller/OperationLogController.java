package com.yuanbaomao.sellersprite.system.ops.operation.controller;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;
import com.yuanbaomao.sellersprite.system.ops.operation.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作日志", description = "查询管理操作审计日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs/operation")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public Result<PageResult<OperationLogVo>> page(@Valid OperationLogPageRequest request) {
        return Result.success(operationLogService.page(request));
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{operationLogId}")
    public Result<OperationLogVo> detail(@PathVariable String operationLogId) {
        return Result.success(operationLogService.detail(operationLogId));
    }
}
