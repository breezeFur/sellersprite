// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.product.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查产品类目响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "查产品类目响应模型")
public class ProductNodeVo {

    /** 查产品类目响应参数：类目 id 字符串，即 nodeIdPath；2619525011:3741271 */
    @Schema(description = "查产品类目响应参数：类目 id 字符串，即 nodeIdPath；2619525011:3741271")
    private String nodeIdPath;

    /** 查产品类目响应参数：类目名称；Appliances:Dishwashers */
    @Schema(description = "查产品类目响应参数：类目名称；Appliances:Dishwashers")
    private String nodeLabelPath;

    /** 查产品类目响应参数：类目下产品数；42 */
    @Schema(description = "查产品类目响应参数：类目下产品数；42")
    private Integer products;

    /** 查产品类目响应参数：类目节点名称中文；洗碗机 */
    @Schema(description = "查产品类目响应参数：类目节点名称中文；洗碗机")
    private String nodeLabelLocale;

    /** 查产品类目响应参数：类目所属所有节点名称中文；大家电:洗碗机 */
    @Schema(description = "查产品类目响应参数：类目所属所有节点名称中文；大家电:洗碗机")
    private String nodeLabelPathLocale;

}
