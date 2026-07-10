// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 出单词反查明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "出单词反查明细响应模型")
public class KeywordOrderItemVo {

    /** 出单词反查明细响应参数：市场，见表 1.2；US */
    @Schema(description = "出单词反查明细响应参数：市场，见表 1.2；US")
    private String marketplace;

    /** 出单词反查明细响应参数：关键词；phone stand for recording */
    @Schema(description = "出单词反查明细响应参数：关键词；phone stand for recording")
    private String keyword;

    /** 出单词反查明细响应参数：关键词中文翻译；用于录音的电话支架 */
    @Schema(description = "出单词反查明细响应参数：关键词中文翻译；用于录音的电话支架")
    private String keywordCn;

    /** 出单词反查明细响应参数：关键词英文翻译；録音用電話スタンド */
    @Schema(description = "出单词反查明细响应参数：关键词英文翻译；録音用電話スタンド")
    private String keywordJp;

    /** 出单词反查明细响应参数：所属asin；B0D1FZW65X */
    @Schema(description = "出单词反查明细响应参数：所属asin；B0D1FZW65X")
    private String asin;

    /** 出单词反查明细响应参数：搜索量；21582 */
    @Schema(description = "出单词反查明细响应参数：搜索量；21582")
    private Integer searches;

    /** 出单词反查明细响应参数：点击垄断率；0.3 */
    @Schema(description = "出单词反查明细响应参数：点击垄断率；0.3")
    private BigDecimal monopolyClickRate;

    /** 出单词反查明细响应参数：转化共享率；0.3084 */
    @Schema(description = "出单词反查明细响应参数：转化共享率；0.3084")
    private BigDecimal cvsShareRate;

    /** 出单词反查明细响应参数：搜索排名；17910 */
    @Schema(description = "出单词反查明细响应参数：搜索排名；17910")
    private Integer searchRank;

    /** 出单词反查明细响应参数：月变化量；5343 */
    @Schema(description = "出单词反查明细响应参数：月变化量；5343")
    private Integer searchRankGv;

    /** 出单词反查明细响应参数：月变化率；0.3 */
    @Schema(description = "出单词反查明细响应参数：月变化率；0.3")
    private BigDecimal searchRankGr;

    /** 出单词反查明细响应参数：前三点击；0.0813 */
    @Schema(description = "出单词反查明细响应参数：前三点击；0.0813")
    private BigDecimal top3ClickingRate;

    /** 出单词反查明细响应参数：前三转化；0.2011 */
    @Schema(description = "出单词反查明细响应参数：前三转化；0.2011")
    private BigDecimal top3ConversionRate;

    /** 出单词反查明细响应参数：转化类型：E：转化优质词，S：转化平稳词，L：转化流失词，I：无效曝光词；E */
    @Schema(description = "出单词反查明细响应参数：转化类型：E：转化优质词，S：转化平稳词，L：转化流失词，I：无效曝光词；E")
    private String conversionType;

}
