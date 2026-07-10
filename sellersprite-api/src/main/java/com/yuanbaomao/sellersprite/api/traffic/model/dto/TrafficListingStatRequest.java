// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 关联流量统计请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关联流量统计请求模型")
public class TrafficListingStatRequest {

    /** 关联流量统计请求参数：asin 路径参数；由官方 Http Request URL 定义；官方参数表未单独列出 */
    @NotBlank
    @Schema(description = "关联流量统计请求参数：asin 路径参数；由官方 Http Request URL 定义；官方参数表未单独列出")
    private String asin;

    /** 关联流量统计请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "关联流量统计请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 关联流量统计请求参数：asin列表；["B07Z82895W"] */
    @Schema(description = "关联流量统计请求参数：asin列表；[\"B07Z82895W\"]")
    private List<String> asinList;

}
