package com.yuanbaomao.sellersprite.system.dict.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典值响应")
public class DictItemVo {

    @Schema(description = "字典值ID")
    private String dictItemId;

    @Schema(description = "字典标签")
    private String itemLabel;

    @Schema(description = "字典值")
    private String itemValue;

    @Schema(description = "展示颜色")
    private String color;

    @Schema(description = "是否默认项：1是 0否")
    private Integer defaultItem;

    @Schema(description = "排序值")
    private Integer sortOrder;
}
