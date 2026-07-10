// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 关键词反查(流量词列表)请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关键词反查(流量词列表)请求模型")
public class TrafficKeywordRequest {

    /** 关键词反查(流量词列表)请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "关键词反查(流量词列表)请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 关键词反查(流量词列表)请求参数：asin；B07Z82895W */
    @NotBlank
    @Schema(description = "关键词反查(流量词列表)请求参数：asin；B07Z82895W")
    private String asin;

    /** 关键词反查(流量词列表)请求参数：关键词；phone stand */
    @Schema(description = "关键词反查(流量词列表)请求参数：关键词；phone stand")
    private String keyword;

    /** 关键词反查(流量词列表)请求参数：历史月份，不传默认最近30天；202308 */
    @Schema(description = "关键词反查(流量词列表)请求参数：历史月份，不传默认最近30天；202308")
    private String month;

    /** 关键词反查(流量词列表)请求参数：流量词类型；见表1.10 */
    @Schema(description = "关键词反查(流量词列表)请求参数：流量词类型；见表1.10")
    private List<String> badges;

    /** 关键词反查(流量词列表)请求参数：流量占比类型；见表2.0 */
    @Schema(description = "关键词反查(流量词列表)请求参数：流量占比类型；见表2.0")
    private List<String> trafficKeywordTypes;

    /** 关键词反查(流量词列表)请求参数：流量转化类型；见表2.1 */
    @Schema(description = "关键词反查(流量词列表)请求参数：流量转化类型；见表2.1")
    private List<String> conversionKeywordTypes;

    /** 关键词反查(流量词列表)请求参数：当前页；默认1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "关键词反查(流量词列表)请求参数：当前页；默认1")
    private Integer page = 1;

    /** 关键词反查(流量词列表)请求参数：每页显示多少条；默认50，最大100，最多查询2000条数据 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 100, message = "size 不能大于 100")
    @Schema(description = "关键词反查(流量词列表)请求参数：每页显示多少条；默认50，最大100，最多查询2000条数据")
    private Integer size = 50;

    /** 关键词反查(流量词列表)请求参数：排序 */
    @Schema(description = "关键词反查(流量词列表)请求参数：排序")
    private SortOrder order;

}
