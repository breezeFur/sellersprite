// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.tool.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图片文字识别响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "图片文字识别响应模型")
public class OcrVo {

    /** 图片文字识别响应参数：识别的文字；卖家精灵 */
    @Schema(description = "图片文字识别响应参数：识别的文字；卖家精灵")
    private String data;

    /** 图片文字识别响应参数：状态码；OK */
    @Schema(description = "图片文字识别响应参数：状态码；OK")
    private Integer code;

    /** 图片文字识别响应参数：状态码描述；成功 */
    @Schema(description = "图片文字识别响应参数：状态码描述；成功")
    private String message;

}
