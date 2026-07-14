package com.yuanbaomao.sellersprite.system.dict.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建字典类型请求")
public class DictTypeCreateRequest {

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 64, message = "字典类型不能超过64个字符")
    @Schema(description = "字典类型")
    private String dictType;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 128, message = "字典名称不能超过128个字符")
    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "是否系统内置：1是 0否")
    private Integer systemBuiltin;

    @Schema(description = "排序值")
    private Integer sortOrder;
}
