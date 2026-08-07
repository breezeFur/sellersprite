// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * ASIN 销量预测响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "ASIN 销量预测响应模型")
public class AsinSalesPredictionVo {

    /** ASIN 销量预测响应参数：asin明细 */
    @Schema(description = "ASIN 销量预测响应参数：asin明细")
    private AsinDetailVo asinDetail;

    /** ASIN 销量预测响应参数：日销量预测明细 */
    @Schema(description = "ASIN 销量预测响应参数：日销量预测明细")
    private List<DailyItemListVo> dailyItemList;

    /** ASIN 销量预测响应参数：月销量预测明细 */
    @Schema(description = "ASIN 销量预测响应参数：月销量预测明细")
    private List<MonthItemListVo> monthItemList;

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                getClass().getName(), name, value);
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

    @Slf4j

    @Data
    @Schema(description = "ASIN 销量预测响应参数：asin明细")
    public static class AsinDetailVo {

        /** ASIN 销量预测响应参数：asin；B00CFM8DI2 */
        @Schema(description = "ASIN 销量预测响应参数：asin；B00CFM8DI2")
        private String asin;

        /** ASIN 销量预测响应参数：标题；Boot Bananas Original Shoe Deodorizer */
        @Schema(description = "ASIN 销量预测响应参数：标题；Boot Bananas Original Shoe Deodorizer")
        private String title;

        /** ASIN 销量预测响应参数：平台；Boot Bananas */
        @Schema(description = "ASIN 销量预测响应参数：平台；Boot Bananas")
        private String brand;

        /** ASIN 销量预测响应参数：上架时间；1397001600000 */
        @Schema(description = "ASIN 销量预测响应参数：上架时间；1397001600000")
        private Long availableDate;

        /** ASIN 销量预测响应参数：类目名称；Clothing, Shoes & Jewelry */
        @Schema(description = "ASIN 销量预测响应参数：类目名称；Clothing, Shoes & Jewelry")
        private String category;

        /** ASIN 销量预测响应参数：类目id；7141123011 */
        @Schema(description = "ASIN 销量预测响应参数：类目id；7141123011")
        private String categoryId;

        /** ASIN 销量预测响应参数：图片URL；https://images-na.ssl-images-amazon.com/images/I/41AGxmiW-vL._AC_US600_.jpg */
        @Schema(description = "ASIN 销量预测响应参数：图片URL；https://images-na.ssl-images-amazon.com/images/I/41AGxmiW-vL._AC_US600_.jpg")
        private String imageUrl;

        /** ASIN 销量预测响应参数：评分数；32004 */
        @Schema(description = "ASIN 销量预测响应参数：评分数；32004")
        private Integer ratings;

        /** ASIN 销量预测响应参数：评分值；4.6 */
        @Schema(description = "ASIN 销量预测响应参数：评分值；4.6")
        private BigDecimal rating;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "ASIN 销量预测响应参数：日销量预测明细")
    public static class DailyItemListVo {

        /** ASIN 销量预测响应参数：日期；45035 */
        @Schema(description = "ASIN 销量预测响应参数：日期；45035")
        private String date;

        /** ASIN 销量预测响应参数：bsr；48614 */
        @Schema(description = "ASIN 销量预测响应参数：bsr；48614")
        private Integer bsr;

        /** ASIN 销量预测响应参数：销量；14 */
        @Schema(description = "ASIN 销量预测响应参数：销量；14")
        private Integer sales;

        /** ASIN 销量预测响应参数：销售额；200 */
        @Schema(description = "ASIN 销量预测响应参数：销售额；200")
        private BigDecimal amount;

        /** ASIN 销量预测响应参数：单价；20 */
        @Schema(description = "ASIN 销量预测响应参数：单价；20")
        private BigDecimal price;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "ASIN 销量预测响应参数：月销量预测明细")
    public static class MonthItemListVo {

        /** ASIN 销量预测响应参数：日期；45017 */
        @Schema(description = "ASIN 销量预测响应参数：日期；45017")
        private String date;

        /** ASIN 销量预测响应参数：销量；14 */
        @Schema(description = "ASIN 销量预测响应参数：销量；14")
        private Integer sales;

        /** ASIN 销量预测响应参数：销售额；200 */
        @Schema(description = "ASIN 销量预测响应参数：销售额；200")
        private BigDecimal amount;

        /** ASIN 销量预测响应参数：单价；20 */
        @Schema(description = "ASIN 销量预测响应参数：单价；20")
        private BigDecimal price;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

}
