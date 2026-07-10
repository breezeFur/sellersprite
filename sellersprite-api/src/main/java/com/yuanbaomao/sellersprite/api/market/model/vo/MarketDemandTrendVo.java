// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 选市场-商品需求趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-商品需求趋势响应模型")
public class MarketDemandTrendVo {

    /** 选市场-商品需求趋势响应参数：asin数量；22187 */
    @Schema(description = "选市场-商品需求趋势响应参数：asin数量；22187")
    private String asinCount;

    /** 选市场-商品需求趋势响应参数：退货率，百分比；1.38 */
    @Schema(description = "选市场-商品需求趋势响应参数：退货率，百分比；1.38")
    private String returnRatio;

    /** 选市场-商品需求趋势响应参数：搜索购买比，千分比；3.17875 */
    @Schema(description = "选市场-商品需求趋势响应参数：搜索购买比，千分比；3.17875")
    private List<String> searchToPurchaseRatio;

    /** 选市场-商品需求趋势响应参数：类目平均退货率，百分比；2.72 */
    @Schema(description = "选市场-商品需求趋势响应参数：类目平均退货率，百分比；2.72")
    private Integer avgReturnRatio;

    /** 选市场-商品需求趋势响应参数：类目平均搜索购买比，千分比；2.6 */
    @Schema(description = "选市场-商品需求趋势响应参数：类目平均搜索购买比，千分比；2.6")
    private BigDecimal avgSearchToPurchaseRatio;

    /** 选市场-商品需求趋势响应参数：月浏览趋势 */
    @Schema(description = "选市场-商品需求趋势响应参数：月浏览趋势")
    private List<ItemsVo> items;

    @Data
    @Schema(description = "选市场-商品需求趋势响应参数：月浏览趋势")
    public static class ItemsVo {

        /** 选市场-商品需求趋势响应参数：时间，yyyy-MM-dd格式；2022-09-10 */
        @Schema(description = "选市场-商品需求趋势响应参数：时间，yyyy-MM-dd格式；2022-09-10")
        private String date;

        /** 选市场-商品需求趋势响应参数：浏览量；2 */
        @Schema(description = "选市场-商品需求趋势响应参数：浏览量；2")
        private Integer glanceViews;

    }

}
