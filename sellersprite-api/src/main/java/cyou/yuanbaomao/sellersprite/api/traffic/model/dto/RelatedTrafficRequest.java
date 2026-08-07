// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.traffic.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 关联流量列表请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关联流量列表请求模型")
public class RelatedTrafficRequest {

    /** 关联流量列表请求参数：市场,见表1.2；US */
    @NotNull
    @Schema(description = "关联流量列表请求参数：市场,见表1.2；US")
    private SellerSpriteMarketplace marketplace;

    /** 关联流量列表请求参数：asin列表；["B07Z82895W"] */
    @NotEmpty
    @Schema(description = "关联流量列表请求参数：asin列表；[\"B07Z82895W\"]")
    private List<String> asinList;

    /** 关联流量列表请求参数：关联类型，见表2.2；["vav"] */
    @NotEmpty
    @Schema(description = "关联流量列表请求参数：关联类型，见表2.2；[\"vav\"]")
    private List<String> relations;

    /** 关联流量列表请求参数：是否查询变体；false */
    @Schema(description = "关联流量列表请求参数：是否查询变体；false")
    private Boolean variations;

    /** 关联流量列表请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "关联流量列表请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** 关联流量列表请求参数：每页条数；默认：50 */
    @Min(value = 1, message = "size 不能小于 1")
    @Schema(description = "关联流量列表请求参数：每页条数；默认：50")
    private Integer size = 50;

    /** 关联流量列表请求参数：排序 */
    @Schema(description = "关联流量列表请求参数：排序")
    private SortOrder order;

}
