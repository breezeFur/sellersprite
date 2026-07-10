// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 选市场-价格分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-价格分布响应模型")
public class MarketPriceDistributionVo {

    /** 选市场-价格分布响应参数：类型说明；5-10 */
    @Schema(description = "选市场-价格分布响应参数：类型说明；5-10")
    private String label;

    /** 选市场-价格分布响应参数：包含的asin列表；["B00P19MFYE"] */
    @Schema(description = "选市场-价格分布响应参数：包含的asin列表；[\"B00P19MFYE\"]")
    private List<String> asins;

    /** 选市场-价格分布响应参数：产品数；3 */
    @Schema(description = "选市场-价格分布响应参数：产品数；3")
    private Integer products;

    /** 选市场-价格分布响应参数：销售额；33058.76 */
    @Schema(description = "选市场-价格分布响应参数：销售额；33058.76")
    private BigDecimal revenue;

    /** 选市场-价格分布响应参数：销量；4024 */
    @Schema(description = "选市场-价格分布响应参数：销量；4024")
    private Integer units;

    /** 选市场-价格分布响应参数：销量占比；0.7165 */
    @Schema(description = "选市场-价格分布响应参数：销量占比；0.7165")
    private BigDecimal unitsRatio;

}
