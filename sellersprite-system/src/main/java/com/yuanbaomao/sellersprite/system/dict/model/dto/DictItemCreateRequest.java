package com.yuanbaomao.sellersprite.system.dict.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建字典值请求")
public class DictItemCreateRequest {

    @NotBlank(message = "字典类型ID不能为空")
    @Schema(description = "字典类型ID")
    private String dictTypeId;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 128, message = "字典标签不能超过128个字符")
    @Schema(description = "字典标签")
    private String itemLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 128, message = "字典值不能超过128个字符")
    @Schema(description = "字典值")
    private String itemValue;

    @Schema(description = "展示颜色")
    private String color;

    @Schema(description = "是否默认项：1是 0否")
    private Integer defaultItem;

    @Schema(description = "排序值")
    private Integer sortOrder;
}
