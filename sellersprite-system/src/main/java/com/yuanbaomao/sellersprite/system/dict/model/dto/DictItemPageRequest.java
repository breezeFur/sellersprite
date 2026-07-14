package com.yuanbaomao.sellersprite.system.dict.model.dto;

import com.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典项分页查询请求")
public class DictItemPageRequest extends PageQuery {

    @Schema(description = "稳定标签关键字")
    private String dictLabel;

    @Schema(description = "展示名称关键字")
    private String dictName;

    @Schema(description = "远端参数值关键字")
    private String dictValue;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
