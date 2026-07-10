package com.yuanbaomao.sellersprite.api.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品子类目排名。
 */
@Data
@Schema(description = "商品子类目排名")
public class SubcategoryVo {

    @Schema(description = "Amazon 类目编码")
    private String code;

    @Schema(description = "商品在该子类目中的排名")
    private Integer rank;

    @Schema(description = "Amazon 类目名称")
    private String label;
}
