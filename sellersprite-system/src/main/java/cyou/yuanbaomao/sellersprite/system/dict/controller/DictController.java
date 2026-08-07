package cyou.yuanbaomao.sellersprite.system.dict.controller;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemPageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictStatusUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypePageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import cyou.yuanbaomao.sellersprite.system.dict.service.DictService;
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

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/types")
    public Result<PageResult<DictTypeVo>> pageTypes(@Valid DictTypePageRequest request) {
        return Result.success(dictService.pageTypes(request));
    }

    @Operation(summary = "查询字典类型详情")
    @GetMapping("/types/{dictTypeId}")
    public Result<DictTypeVo> detailType(@PathVariable String dictTypeId) {
        return Result.success(dictService.detailType(dictTypeId));
    }

    @Operation(summary = "编辑字典类型")
    @PutMapping("/types/{dictTypeId}")
    public Result<DictTypeVo> updateType(@PathVariable String dictTypeId,
            @Valid @RequestBody DictTypeUpdateRequest request) {
        return Result.success(dictService.updateType(dictTypeId, request));
    }

    @Operation(summary = "更新字典类型状态")
    @PutMapping("/types/{dictTypeId}/status")
    public Result<Void> updateTypeStatus(@PathVariable String dictTypeId,
            @Valid @RequestBody DictStatusUpdateRequest request) {
        dictService.updateTypeStatus(dictTypeId, request.getStatus());
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{dictTypeId}")
    public Result<Void> deleteType(@PathVariable String dictTypeId) {
        dictService.deleteType(dictTypeId);
        return Result.success();
    }

    @Operation(summary = "创建字典值")
    @PostMapping("/items")
    public Result<DictItemVo> createItem(@Valid @RequestBody DictItemCreateRequest request) {
        return Result.success(dictService.createItem(request));
    }

    @Operation(summary = "分页查询字典项")
    @GetMapping("/types/{dictTypeId}/items")
    public Result<PageResult<DictItemVo>> pageItems(@PathVariable String dictTypeId,
            @Valid DictItemPageRequest request) {
        return Result.success(dictService.pageItems(dictTypeId, request));
    }

    @Operation(summary = "查询字典项详情")
    @GetMapping("/items/{dictItemId}")
    public Result<DictItemVo> detailItem(@PathVariable String dictItemId) {
        return Result.success(dictService.detailItem(dictItemId));
    }

    @Operation(summary = "编辑字典项")
    @PutMapping("/items/{dictItemId}")
    public Result<DictItemVo> updateItem(@PathVariable String dictItemId,
            @Valid @RequestBody DictItemUpdateRequest request) {
        return Result.success(dictService.updateItem(dictItemId, request));
    }

    @Operation(summary = "更新字典项状态")
    @PutMapping("/items/{dictItemId}/status")
    public Result<Void> updateItemStatus(@PathVariable String dictItemId,
            @Valid @RequestBody DictStatusUpdateRequest request) {
        dictService.updateItemStatus(dictItemId, request.getStatus());
        return Result.success();
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/items/{dictItemId}")
    public Result<Void> deleteItem(@PathVariable String dictItemId) {
        dictService.deleteItem(dictItemId);
        return Result.success();
    }

    @Operation(summary = "按编码查询字典")
    @GetMapping("/{dictCode}")
    public Result<DictTypeVo> detailByCode(@PathVariable String dictCode) {
        return Result.success(dictService.detailByCode(dictCode));
    }
}
