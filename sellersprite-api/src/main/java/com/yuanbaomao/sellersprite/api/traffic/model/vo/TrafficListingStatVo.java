// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 关联流量统计响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "关联流量统计响应模型")
public class TrafficListingStatVo {

    /** 关联流量统计响应参数：市场；US */
    @Schema(description = "关联流量统计响应参数：市场；US")
    private String marketplace;

    /** 关联流量统计响应参数：asin；B07Z82895W */
    @Schema(description = "关联流量统计响应参数：asin；B07Z82895W")
    private String asin;

    /** 关联流量统计响应参数：全部流量；1848 */
    @Schema(description = "关联流量统计响应参数：全部流量；1848")
    private Integer relations;

    /** 关联流量统计响应参数：免费流量；1414 */
    @Schema(description = "关联流量统计响应参数：免费流量；1414")
    private Integer freeRelations;

    /** 关联流量统计响应参数：付费流量；286 */
    @Schema(description = "关联流量统计响应参数：付费流量；286")
    private Integer paidRelations;

    /** 关联流量统计响应参数：最近计算时间 */
    @Schema(description = "关联流量统计响应参数：最近计算时间")
    private Long calcTime;

    /** 关联流量统计响应参数：统计概要 */
    @Schema(description = "关联流量统计响应参数：统计概要")
    private List<ItemsVo> items;

    @Data
    @Schema(description = "关联流量统计响应参数：统计概要")
    public static class ItemsVo {

        /** 关联流量统计响应参数：关联类型，见表2.2,忽略大小写；vav */
        @Schema(description = "关联流量统计响应参数：关联类型，见表2.2,忽略大小写；vav")
        private String relation;

        /** 关联流量统计响应参数：数量；3 */
        @Schema(description = "关联流量统计响应参数：数量；3")
        private Integer count;

    }

}
