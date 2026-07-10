// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 选市场-A+视频分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-A+视频分布响应模型")
public class MarketEbcDistributionVo {

    /** 选市场-A+视频分布响应参数：类型说明；有A+有视频 */
    @Schema(description = "选市场-A+视频分布响应参数：类型说明；有A+有视频")
    private String label;

    /** 选市场-A+视频分布响应参数：产品数；1 */
    @Schema(description = "选市场-A+视频分布响应参数：产品数；1")
    private Integer products;

    /** 选市场-A+视频分布响应参数：类目名称产品占比；20 */
    @Schema(description = "选市场-A+视频分布响应参数：类目名称产品占比；20")
    private BigDecimal productsRatio;

    /** 选市场-A+视频分布响应参数：销量；1311 */
    @Schema(description = "选市场-A+视频分布响应参数：销量；1311")
    private Integer units;

    /** 选市场-A+视频分布响应参数：销量占比；23.34 */
    @Schema(description = "选市场-A+视频分布响应参数：销量占比；23.34")
    private BigDecimal unitsRatio;

}
