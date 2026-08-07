// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.product.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 查竞品请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "查竞品请求模型")
public class CompetitorLookupRequest {

    /** 查竞品请求参数：市场编码；见表 1.2 */
    @NotNull
    @Schema(description = "查竞品请求参数：市场编码；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 查竞品请求参数：查询月份；格式：yyyyMM，示例：202507，见表 1.1 */
    @Schema(description = "查竞品请求参数：查询月份；格式：yyyyMM，示例：202507，见表 1.1")
    private String month;

    /** 查竞品请求参数：品牌；WWDOLL */
    @Schema(description = "查竞品请求参数：品牌；WWDOLL")
    private String brand;

    /** 查竞品请求参数：卖家；Apple */
    @Schema(description = "查竞品请求参数：卖家；Apple")
    private String sellerName;

    /** 查竞品请求参数：asin 的 list 字符串；最多支持40个ASIN */
    @Size(max = 40, message = "asins 最多允许 40 项")
    @Schema(description = "查竞品请求参数：asin 的 list 字符串；最多支持40个ASIN")
    private List<String> asins;

    /** 查竞品请求参数：类目节点字符串；见查产品类目 */
    @Schema(description = "查竞品请求参数：类目节点字符串；见查产品类目")
    private String nodeIdPath;

    /** 查竞品请求参数：类目节点查询方式；true: 为类目精确查询, false: 为查询当前及子类目; 默认：false */
    @Schema(description = "查竞品请求参数：类目节点查询方式；true: 为类目精确查询, false: 为查询当前及子类目; 默认：false")
    private Boolean nodeIdPathEqual;

    /** 查竞品请求参数：关键字 */
    @Schema(description = "查竞品请求参数：关键字")
    private String keyword;

    /** 查竞品请求参数：关键词匹配方式；1：词组匹配，2：模糊匹配，3：精准匹配；默认：2 */
    @Schema(description = "查竞品请求参数：关键词匹配方式；1：词组匹配，2：模糊匹配，3：精准匹配；默认：2")
    private Integer matchType;

    /** 查竞品请求参数：是否查询变体ASIN；N: 含变体, Y: 不含变体 */
    @Schema(description = "查竞品请求参数：是否查询变体ASIN；N: 含变体, Y: 不含变体")
    private String variation;

    /** 查竞品请求参数：页码；Default: 1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "查竞品请求参数：页码；Default: 1")
    private Integer page = 1;

    /** 查竞品请求参数：每页条数；Default：50，Max: 100 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 100, message = "size 不能大于 100")
    @Schema(description = "查竞品请求参数：每页条数；Default：50，Max: 100")
    private Integer size = 50;

    /** 查竞品请求参数：排序对象 */
    @Schema(description = "查竞品请求参数：排序对象")
    private SortOrder order;

}
