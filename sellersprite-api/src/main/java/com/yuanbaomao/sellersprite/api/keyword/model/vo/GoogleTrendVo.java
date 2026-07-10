// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 谷歌趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "谷歌趋势响应模型")
public class GoogleTrendVo {

    /** 谷歌趋势响应参数：市场，见表 1.2；US */
    @Schema(description = "谷歌趋势响应参数：市场，见表 1.2；US")
    private String marketplace;

    /** 谷歌趋势响应参数：关键字；phone stand */
    @Schema(description = "谷歌趋势响应参数：关键字；phone stand")
    private String keyword;

    /** 谷歌趋势响应参数：google trend链接 */
    @Schema(description = "谷歌趋势响应参数：google trend链接")
    private String link;

    /** 谷歌趋势响应参数：明细 */
    @Schema(description = "谷歌趋势响应参数：明细")
    private List<ItemsVo> items;

    @Data
    @Schema(description = "谷歌趋势响应参数：明细")
    public static class ItemsVo {

        /** 谷歌趋势响应参数：时间戳；1555804800000 */
        @Schema(description = "谷歌趋势响应参数：时间戳；1555804800000")
        private Long time;

        /** 谷歌趋势响应参数：值；2 */
        @Schema(description = "谷歌趋势响应参数：值；2")
        private Integer value;

    }

}
