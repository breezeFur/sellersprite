// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.dto;

import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 出单词反查请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "出单词反查请求模型")
public class KeywordOrderRequest {

    /** 出单词反查请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "出单词反查请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 出单词反查请求参数：asin列表，最大20；B07Z82895W */
    @NotEmpty
    @Size(max = 20, message = "asins 最多允许 20 项")
    @Schema(description = "出单词反查请求参数：asin列表，最大20；B07Z82895W")
    private List<String> asins;

    /** 出单词反查请求参数：反查模式 W-周 M-月；W */
    @NotBlank
    @Schema(description = "出单词反查请求参数：反查模式 W-周 M-月；W")
    private String reverseType;

    /** 出单词反查请求参数：查询日期，按周查，格式为yyyMMdd该周最后一天，按月查询yyyyMM；周：20241109月：202411 */
    @Schema(description = "出单词反查请求参数：查询日期，按周查，格式为yyyMMdd该周最后一天，按月查询yyyyMM；周：20241109月：202411")
    private String date;

    /** 出单词反查请求参数：转化类型：E：转化优质词，S：转化平稳词，L：转化流失词，I：无效曝光词；E */
    @Schema(description = "出单词反查请求参数：转化类型：E：转化优质词，S：转化平稳词，L：转化流失词，I：无效曝光词；E")
    private List<String> conversionType;

    /** 出单词反查请求参数：是否查询变体asin：Y:否 N:是；Y */
    @Schema(description = "出单词反查请求参数：是否查询变体asin：Y:否 N:是；Y")
    private List<String> variation;

    /** 出单词反查请求参数：当前页；默认1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "出单词反查请求参数：当前页；默认1")
    private Integer page = 1;

    /** 出单词反查请求参数：每页显示多少条；固定50 */
    @Min(value = 1, message = "size 不能小于 1")
    @Schema(description = "出单词反查请求参数：每页显示多少条；固定50")
    private Integer size = 50;

    /** 出单词反查请求参数：排序 */
    @Schema(description = "出单词反查请求参数：排序")
    private SortOrder order;

}
