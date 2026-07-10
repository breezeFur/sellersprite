package com.yuanbaomao.sellersprite.system.dict.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "字典类型响应")
public class DictTypeVo {

    @Schema(description = "字典类型ID")
    private String dictTypeId;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "字典值列表")
    private List<DictItemVo> items;
}
