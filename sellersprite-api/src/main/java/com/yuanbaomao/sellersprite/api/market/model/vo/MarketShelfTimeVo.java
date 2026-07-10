// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 选市场-上架时间分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-上架时间分布响应模型")
public class MarketShelfTimeVo {

    /** 选市场-上架时间分布响应参数：类型说明；3年以上 */
    @Schema(description = "选市场-上架时间分布响应参数：类型说明；3年以上")
    private String label;

    /** 选市场-上架时间分布响应参数：上架时间；3年以上 */
    @Schema(description = "选市场-上架时间分布响应参数：上架时间；3年以上")
    private String shelfTime;

    /** 选市场-上架时间分布响应参数：包含的asin列表；["B00P19MFYE"] */
    @Schema(description = "选市场-上架时间分布响应参数：包含的asin列表；[\"B00P19MFYE\"]")
    private List<String> asins;

    /** 选市场-上架时间分布响应参数：产品数；B07Z82895W */
    @Schema(description = "选市场-上架时间分布响应参数：产品数；B07Z82895W")
    private Integer products;

    /** 选市场-上架时间分布响应参数：销售额；40846.76 */
    @Schema(description = "选市场-上架时间分布响应参数：销售额；40846.76")
    private BigDecimal revenue;

    /** 选市场-上架时间分布响应参数：销量；4684 */
    @Schema(description = "选市场-上架时间分布响应参数：销量；4684")
    private Integer units;

    /** 选市场-上架时间分布响应参数：销量占比；0.834 */
    @Schema(description = "选市场-上架时间分布响应参数：销量占比；0.834")
    private BigDecimal unitsRatio;

}
