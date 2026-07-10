// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.trademark.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 全球商标库-数据范围响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "全球商标库-数据范围响应模型")
public class TrademarkRangeVo {

    /** 全球商标库-数据范围响应参数：简码；AD */
    @Schema(description = "全球商标库-数据范围响应参数：简码；AD")
    private String office;

    /** 全球商标库-数据范围响应参数：中文名称；安道尔 */
    @Schema(description = "全球商标库-数据范围响应参数：中文名称；安道尔")
    private String officeLabel;

}
