package com.yuanbaomao.sellersprite.api.common.model.vo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SellerSprite 数值趋势点，对应官方 PairNumberDto。
 */
@Data
@Schema(description = "SellerSprite 数值趋势点")
public class NumericTrendPointVo {

    /** 毫秒时间戳。 */
    @Schema(description = "毫秒时间戳")
    private Long timePoint;

    /** 该时间点的数值。 */
    @Schema(description = "该时间点的数值")
    private BigDecimal value;
}
