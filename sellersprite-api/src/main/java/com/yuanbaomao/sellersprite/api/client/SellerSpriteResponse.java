package com.yuanbaomao.sellersprite.api.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SellerSprite Open API 外部响应信封。
 *
 * @param <T> 外部接口 data 字段的强类型
 */
@Data
@Schema(description = "SellerSprite Open API 外部响应")
public class SellerSpriteResponse<T> {

    /** 外部业务状态码，`OK` 表示成功。 */
    @Schema(description = "SellerSprite 业务状态码，OK 表示成功")
    private String code;

    /** 外部业务提示。 */
    @Schema(description = "SellerSprite 返回消息")
    private String message;

    /** 外部业务数据。 */
    @Schema(description = "SellerSprite 返回数据")
    private T data;
}
