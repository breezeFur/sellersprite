// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 选市场-卖家类型分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-卖家类型分布响应模型")
public class MarketSellerTypeVo {

    /** 选市场-卖家类型分布响应参数：类型说明；Amazon自营 */
    @Schema(description = "选市场-卖家类型分布响应参数：类型说明；Amazon自营")
    private String label;

    /** 选市场-卖家类型分布响应参数：ASIN数量；4 */
    @Schema(description = "选市场-卖家类型分布响应参数：ASIN数量；4")
    private Integer asinNum;

    /** 选市场-卖家类型分布响应参数：ASIN数量占比；0.03 */
    @Schema(description = "选市场-卖家类型分布响应参数：ASIN数量占比；0.03")
    private BigDecimal asinRatio;

    /** 选市场-卖家类型分布响应参数：月销量；79875 */
    @Schema(description = "选市场-卖家类型分布响应参数：月销量；79875")
    private Integer units;

    /** 选市场-卖家类型分布响应参数：月销量占比；0.0345 */
    @Schema(description = "选市场-卖家类型分布响应参数：月销量占比；0.0345")
    private BigDecimal unitsRatio;

    /** 选市场-卖家类型分布响应参数：评分数；6607 */
    @Schema(description = "选市场-卖家类型分布响应参数：评分数；6607")
    private Integer ratings;

    /** 选市场-卖家类型分布响应参数：评分值；4.7 */
    @Schema(description = "选市场-卖家类型分布响应参数：评分值；4.7")
    private BigDecimal rating;

    /** 选市场-卖家类型分布响应参数：商品总数；3 */
    @Schema(description = "选市场-卖家类型分布响应参数：商品总数；3")
    private Integer productNum;

}
