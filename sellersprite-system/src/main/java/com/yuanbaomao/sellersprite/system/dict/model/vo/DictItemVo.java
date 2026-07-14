package com.yuanbaomao.sellersprite.system.dict.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典值响应")
public class DictItemVo {

    @Schema(description = "字典数据ID")
    private String dictDataId;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "远端接口参数值")
    private String dictValue;

    @Schema(description = "前后端交互和数据库持久化使用的稳定标识")
    private String dictLabel;

    @Schema(description = "前端展示名称")
    private String dictName;

    @Schema(description = "展示颜色")
    private String color;

    @Schema(description = "是否默认项：1是 0否")
    private Integer defaultFlag;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "是否系统内置：1是 0否")
    private Integer systemBuiltin;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
