package com.yuanbaomao.sellersprite.api.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SellerSprite 文本趋势点，对应官方 PairStrDto。
 */
@Data
@Schema(description = "SellerSprite 文本趋势点")
public class StringTrendPointVo {

    /** 毫秒时间戳。 */
    @Schema(description = "毫秒时间戳")
    private Long timePoint;

    /** 该时间点的文本值。 */
    @Schema(description = "该时间点的文本值")
    private String value;
}
