package com.yuanbaomao.sellersprite.system.dict.model.dto;

import com.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型分页查询请求")
public class DictTypePageRequest extends PageQuery {

    @Schema(description = "字典类型关键字")
    private String dictType;

    @Schema(description = "字典名称关键字")
    private String dictName;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
