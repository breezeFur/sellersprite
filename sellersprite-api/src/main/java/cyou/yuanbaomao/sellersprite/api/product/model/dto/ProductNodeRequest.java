// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.product.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查产品类目请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "查产品类目请求模型")
public class ProductNodeRequest {

    /** 查产品类目请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "查产品类目请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** 查产品类目请求参数：类目节点 id 字符串；2619525011:3741271:3741281 */
    @Schema(description = "查产品类目请求参数：类目节点 id 字符串；2619525011:3741271:3741281")
    private String nodeIdPath;

    /** 查产品类目请求参数：搜索关键字，nodeId或类目名称；Books 或者 4053 */
    @Schema(description = "查产品类目请求参数：搜索关键字，nodeId或类目名称；Books 或者 4053")
    private String keyword;

    /** 查产品类目请求参数：查询历史月份类目，格式yyyyMM；202502 */
    @Schema(description = "查产品类目请求参数：查询历史月份类目，格式yyyyMM；202502")
    private String month;

}
