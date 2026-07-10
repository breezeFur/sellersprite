// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ABA 数据选品-关键词趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ABA 数据选品-关键词趋势响应模型")
public class AbaKeywordTrendVo {

    /** ABA 数据选品-关键词趋势响应参数：日期 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：日期")
    private Long date;

    /** ABA 数据选品-关键词趋势响应参数：ABA排名 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：ABA排名")
    private String rank;

    /** ABA 数据选品-关键词趋势响应参数：搜索量 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：搜索量")
    private String searches;

    /** ABA 数据选品-关键词趋势响应参数：日期标签 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：日期标签")
    private Integer label;

}
