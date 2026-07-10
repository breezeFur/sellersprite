// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * BSR销量预测响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "BSR销量预测响应模型")
public class BsrSalesPredictionVo {

    /** BSR销量预测响应参数：市场；US */
    @Schema(description = "BSR销量预测响应参数：市场；US")
    private String marketplace;

    /** BSR销量预测响应参数：1；B07Z82895W */
    @Schema(description = "BSR销量预测响应参数：1；B07Z82895W")
    private Integer bsr;

    /** BSR销量预测响应参数：类目名称；2685 */
    @Schema(description = "BSR销量预测响应参数：类目名称；2685")
    private String categoryLabel;

    /** BSR销量预测响应参数：预测日销量；99 */
    @Schema(description = "BSR销量预测响应参数：预测日销量；99")
    private Integer estDailySales;

    /** BSR销量预测响应参数：预测30天销量；2965 */
    @Schema(description = "BSR销量预测响应参数：预测30天销量；2965")
    private Integer estMonthSales;

    /** BSR销量预测响应参数：明细 */
    @Schema(description = "BSR销量预测响应参数：明细")
    private List<ItemListVo> itemList;

    @Data
    @Schema(description = "BSR销量预测响应参数：明细")
    public static class ItemListVo {

        /** BSR销量预测响应参数：bsr；1 */
        @Schema(description = "BSR销量预测响应参数：bsr；1")
        private Integer bsr;

        /** BSR销量预测响应参数：预测日销量；99 */
        @Schema(description = "BSR销量预测响应参数：预测日销量；99")
        private Integer estDailySales;

        /** BSR销量预测响应参数：预测30天销量；2965 */
        @Schema(description = "BSR销量预测响应参数：预测30天销量；2965")
        private Integer estMonthSales;

    }

}
