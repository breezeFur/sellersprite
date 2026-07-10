// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.trademark.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 全球商标库-详情请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "全球商标库-详情请求模型")
public class TrademarkDetailRequest {

    /** 全球商标库-详情请求参数：数据范围，见上一个接口；US */
    @NotBlank
    @Schema(description = "全球商标库-详情请求参数：数据范围，见上一个接口；US")
    private String office;

    /** 全球商标库-详情请求参数：id,见列表接口；US502022097612203 */
    @NotBlank
    @Schema(description = "全球商标库-详情请求参数：id,见列表接口；US502022097612203")
    private String brandId;

}
