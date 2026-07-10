// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查流量来源(关键词流向)请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "查流量来源(关键词流向)请求模型")
public class TrafficSourceRequest {

    /** 查流量来源(关键词流向)请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "查流量来源(关键词流向)请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 查流量来源(关键词流向)请求参数：asin 或者 关键词；B07Z82895W */
    @NotBlank
    @Schema(description = "查流量来源(关键词流向)请求参数：asin 或者 关键词；B07Z82895W")
    private String q;

    /** 查流量来源(关键词流向)请求参数：筛选日期,yyyyMM格式；202203 */
    @NotBlank
    @Schema(description = "查流量来源(关键词流向)请求参数：筛选日期,yyyyMM格式；202203")
    private String month;

    /** 查流量来源(关键词流向)请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "查流量来源(关键词流向)请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 查流量来源(关键词流向)请求参数：每页条数；默认：50最大： 100 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 100, message = "size 不能大于 100")
    @Schema(description = "查流量来源(关键词流向)请求参数：每页条数；默认：50最大： 100")
    private Integer size = 50;

    /** 查流量来源(关键词流向)请求参数：排序 */
    @Schema(description = "查流量来源(关键词流向)请求参数：排序")
    private SortOrder order;

}
