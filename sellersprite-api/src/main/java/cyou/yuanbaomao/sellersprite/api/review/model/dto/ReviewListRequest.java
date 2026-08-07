// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.review.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 查评论请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "查评论请求模型")
public class ReviewListRequest {

    /** 查评论请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "查评论请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 查评论请求参数：ASIN */
    @NotBlank
    @Schema(description = "查评论请求参数：ASIN")
    private String asin;

    /** 查评论请求参数：评论星级；1: 一星, 2: 二星, 3: 三星, 4: 四星, 5: 五星 */
    @Schema(description = "查评论请求参数：评论星级；1: 一星, 2: 二星, 3: 三星, 4: 四星, 5: 五星")
    private List<String> starList;

    /** 查评论请求参数：评论类型；1：图片评论, 2：视频评论, 3：VP评论, 4：vine评论 */
    @Schema(description = "查评论请求参数：评论类型；1：图片评论, 2：视频评论, 3：VP评论, 4：vine评论")
    private List<String> typeList;

    /** 查评论请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "查评论请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 查评论请求参数：每页条数，最大10；默认：5 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 10, message = "size 不能大于 10")
    @Schema(description = "查评论请求参数：每页条数，最大10；默认：5")
    private Integer size = 5;

}
