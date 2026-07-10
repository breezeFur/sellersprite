// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 选市场-上架趋势分布请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "选市场-上架趋势分布请求模型")
public class MarketShelfTrendRequest {

    /** 选市场-上架趋势分布请求参数：站点编码；见表 1.2 */
    @Schema(description = "选市场-上架趋势分布请求参数：站点编码；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 选市场-上架趋势分布请求参数：筛选日期,默认最近30天；见表 1.1 */
    @Schema(description = "选市场-上架趋势分布请求参数：筛选日期,默认最近30天；见表 1.1")
    private String month;

    /** 选市场-上架趋势分布请求参数：头部Listing数量；10 */
    @Schema(description = "选市场-上架趋势分布请求参数：头部Listing数量；10")
    private Integer topN;

    /** 选市场-上架趋势分布请求参数：新品定义；6 */
    @Schema(description = "选市场-上架趋势分布请求参数：新品定义；6")
    private Integer newProduct;

    /** 选市场-上架趋势分布请求参数：节点 id 路径字符串；1064954:1069242:1069784:1069820:1069838:1069828 */
    @Schema(description = "选市场-上架趋势分布请求参数：节点 id 路径字符串；1064954:1069242:1069784:1069820:1069838:1069828")
    private String nodeIdPath;

}
