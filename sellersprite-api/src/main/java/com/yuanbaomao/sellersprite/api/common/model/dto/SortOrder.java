package com.yuanbaomao.sellersprite.api.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SellerSprite 排序条件。
 */
@Data
@Schema(description = "SellerSprite 排序条件")
public class SortOrder {

    /** 官方排序字段，例如 total_units 表示月销量。 */
    @Schema(description = "官方排序字段，例如 total_units 表示月销量")
    private String field;

    /** 是否降序排列，默认降序。 */
    @Schema(description = "是否降序排列，默认 true")
    private Boolean desc = true;
}
