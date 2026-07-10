package com.yuanbaomao.sellersprite.system.dict.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import com.yuanbaomao.sellersprite.system.dict.service.DictService;
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

@Tag(name = "系统字典兼容接口")
@RestController("systemDictController")
@RequiredArgsConstructor
@RequestMapping("/api/system/dicts")
public class DictController {

    private final DictService dictService;

    @Operation(summary = "创建字典类型")
    @PostMapping("/types")
    public Result<DictTypeVo> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        return Result.success(dictService.createType(request));
    }

    @Operation(summary = "创建字典值")
    @PostMapping("/items")
    public Result<DictItemVo> createItem(@Valid @RequestBody DictItemCreateRequest request) {
        return Result.success(dictService.createItem(request));
    }

    @Operation(summary = "按编码查询字典")
    @GetMapping("/{dictCode}")
    public Result<DictTypeVo> detailByCode(@PathVariable String dictCode) {
        return Result.success(dictService.detailByCode(dictCode));
    }
}
