// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.review.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 查评论明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "查评论明细响应模型")
public class ReviewListItemVo {

    /** 查评论明细响应参数：用户 */
    @Schema(description = "查评论明细响应参数：用户")
    private String author;

    /** 查评论明细响应参数：标题 */
    @Schema(description = "查评论明细响应参数：标题")
    private String title;

    /** 查评论明细响应参数：评论内容 */
    @Schema(description = "查评论明细响应参数：评论内容")
    private String content;

    /** 查评论明细响应参数：日期（时间戳）；1772380800000 */
    @Schema(description = "查评论明细响应参数：日期（时间戳）；1772380800000")
    private Long date;

    /** 查评论明细响应参数：星级 */
    @Schema(description = "查评论明细响应参数：星级")
    private Integer star;

    /** 查评论明细响应参数：评论人标签 */
    @Schema(description = "查评论明细响应参数：评论人标签")
    private List<String> authorLabels;

    /** 查评论明细响应参数：sku信息 */
    @Schema(description = "查评论明细响应参数：sku信息")
    private List<String> skus;

    /** 查评论明细响应参数：图片链接 */
    @Schema(description = "查评论明细响应参数：图片链接")
    private List<String> images;

    /** 查评论明细响应参数：视频链接 */
    @Schema(description = "查评论明细响应参数：视频链接")
    private List<String> videos;

    /** 查评论明细响应参数：点赞数 */
    @Schema(description = "查评论明细响应参数：点赞数")
    private Integer likes;

    /** 查评论明细响应参数：是否图片评论 */
    @Schema(description = "查评论明细响应参数：是否图片评论")
    private Boolean image;

    /** 查评论明细响应参数：是否视频评论 */
    @Schema(description = "查评论明细响应参数：是否视频评论")
    private Boolean video;

    /** 查评论明细响应参数：是否实际购买评论 */
    @Schema(description = "查评论明细响应参数：是否实际购买评论")
    private Boolean verified;

    /** 查评论明细响应参数：是否特邀评论 */
    @Schema(description = "查评论明细响应参数：是否特邀评论")
    private Boolean vine;

    /** 查评论明细响应参数：是否免费评论 */
    @Schema(description = "查评论明细响应参数：是否免费评论")
    private Boolean free;

    /** 查评论明细响应参数：是否抢先体验评论 */
    @Schema(description = "查评论明细响应参数：是否抢先体验评论")
    private Boolean experience;

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

}
