// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.asin.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.yuanbaomao.sellersprite.api.common.model.vo.BadgeVo;
import com.yuanbaomao.sellersprite.api.common.model.vo.SubcategoryVo;
import com.yuanbaomao.sellersprite.api.common.model.vo.VariationVo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * ASIN详情及优惠趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ASIN详情及优惠趋势响应模型")
public class AsinWithCouponTrendVo {

    /** ASIN详情及优惠趋势响应参数：Asin Object */
    @Schema(description = "ASIN详情及优惠趋势响应参数：Asin Object")
    private AsinVo asin;

    /** ASIN详情及优惠趋势响应参数：Coupon Trends */
    @Schema(description = "ASIN详情及优惠趋势响应参数：Coupon Trends")
    private List<CouponTrendsVo> couponTrends;

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

    @Data
    @Schema(description = "ASIN详情及优惠趋势响应参数：Asin Object")
    public static class AsinVo {

        /** ASIN详情及优惠趋势响应参数：asin；B08GHW4TBS */
        @Schema(description = "ASIN详情及优惠趋势响应参数：asin；B08GHW4TBS")
        private String asin;

        /** ASIN详情及优惠趋势响应参数：asin url；https://www.amazon.com/dp/B08GHW4TBS */
        @Schema(description = "ASIN详情及优惠趋势响应参数：asin url；https://www.amazon.com/dp/B08GHW4TBS")
        private String asinUrl;

        /** ASIN详情及优惠趋势响应参数：上架日期；1609059137000 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：上架日期；1609059137000")
        private Long availableDate;

        /** ASIN详情及优惠趋势响应参数：标识；包括了下面 5 个标识 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：标识；包括了下面 5 个标识")
        private BadgeVo badge;

        /** ASIN详情及优惠趋势响应参数：品牌；mermaker */
        @Schema(description = "ASIN详情及优惠趋势响应参数：品牌；mermaker")
        private String brand;

        /** ASIN详情及优惠趋势响应参数：品牌 URL；/stores/Mermaker/page/984A6448-1C68-4CCA-AD5A-D574EA2D65D5?ref_=ast_bln */
        @Schema(description = "ASIN详情及优惠趋势响应参数：品牌 URL；/stores/Mermaker/page/984A6448-1C68-4CCA-AD5A-D574EA2D65D5?ref_=ast_bln")
        private String brandUrl;

        /** ASIN详情及优惠趋势响应参数：bsr id；home-garden */
        @Schema(description = "ASIN详情及优惠趋势响应参数：bsr id；home-garden")
        private String bsrId;

        /** ASIN详情及优惠趋势响应参数：bsr 标签；Home & Kitchen */
        @Schema(description = "ASIN详情及优惠趋势响应参数：bsr 标签；Home & Kitchen")
        private String bsrLabel;

        /** ASIN详情及优惠趋势响应参数：bsr 排名；1006 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：bsr 排名；1006")
        private Integer bsrRank;

        /** ASIN详情及优惠趋势响应参数：创建时间；1606467137000 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：创建时间；1606467137000")
        private Long createdTime;

        /** ASIN详情及优惠趋势响应参数：尺寸；7 x 6 x 0.6 inches */
        @Schema(description = "ASIN详情及优惠趋势响应参数：尺寸；7 x 6 x 0.6 inches")
        private String dimensions;

        /** ASIN详情及优惠趋势响应参数：第一次评论时间；1609059137000 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：第一次评论时间；1609059137000")
        private Long firstRatingDate;

        /** ASIN详情及优惠趋势响应参数：图片链接；https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US200.jpg */
        @Schema(description = "ASIN详情及优惠趋势响应参数：图片链接；https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US200.jpg")
        private String imageUrl;

        /** ASIN详情及优惠趋势响应参数：Listing 页面质量得分；97 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：Listing 页面质量得分；97")
        private Integer lqs;

        /** ASIN详情及优惠趋势响应参数：节点 id；1063280 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：节点 id；1063280")
        private String nodeId;

        /** ASIN详情及优惠趋势响应参数：节点 id 串；1055398:1063252:1063280 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：节点 id 串；1055398:1063252:1063280")
        private String nodeIdPath;

        /** ASIN详情及优惠趋势响应参数：类目名称串；Home & Kitchen:Bedding:Blankets & Throws */
        @Schema(description = "ASIN详情及优惠趋势响应参数：类目名称串；Home & Kitchen:Bedding:Blankets & Throws")
        private String nodeLabelPath;

        /** ASIN详情及优惠趋势响应参数：类目名称串中文；家居厨房用品:床上用品:毯子、盖毯 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：类目名称串中文；家居厨房用品:床上用品:毯子、盖毯")
        private String nodeLabelPathLocale;

        /** ASIN详情及优惠趋势响应参数：父 asin；B07V5GB9B5 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：父 asin；B07V5GB9B5")
        private String parent;

        /** ASIN详情及优惠趋势响应参数：价格；21.99 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：价格；21.99")
        private BigDecimal price;

        /** ASIN详情及优惠趋势响应参数：问题数量；5 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：问题数量；5")
        private Integer questions;

        /** ASIN详情及优惠趋势响应参数：评分；4.8 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：评分；4.8")
        private BigDecimal rating;

        /** ASIN详情及优惠趋势响应参数：评分数；29229 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：评分数；29229")
        private Integer ratings;

        /** ASIN详情及优惠趋势响应参数：评论数；9229 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：评论数；9229")
        private Integer reviews;

        /** ASIN详情及优惠趋势响应参数：子体评分数；12454 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：子体评分数；12454")
        private Integer variantRatings;

        /** ASIN详情及优惠趋势响应参数：子体评论数；3211 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：子体评论数；3211")
        private Integer variantReviews;

        /** ASIN详情及优惠趋势响应参数：卖家 id；A13AJ1GXFINAZ */
        @Schema(description = "ASIN详情及优惠趋势响应参数：卖家 id；A13AJ1GXFINAZ")
        private String sellerId;

        /** ASIN详情及优惠趋势响应参数：卖家名称；Mermaker */
        @Schema(description = "ASIN详情及优惠趋势响应参数：卖家名称；Mermaker")
        private String sellerName;

        /** ASIN详情及优惠趋势响应参数：配送方式；FBA */
        @Schema(description = "ASIN详情及优惠趋势响应参数：配送方式；FBA")
        private String fulfillment;

        /** ASIN详情及优惠趋势响应参数：卖家数；1 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：卖家数；1")
        private Integer sellers;

        /** ASIN详情及优惠趋势响应参数：sku；["Color: Beige","Size: 47 inches"] */
        @Schema(description = "ASIN详情及优惠趋势响应参数：sku；[\"Color: Beige\",\"Size: 47 inches\"]")
        private List<String> skuList;

        /** ASIN详情及优惠趋势响应参数：String；见表 1.2 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：String；见表 1.2")
        private String marketplace;

        /** ASIN详情及优惠趋势响应参数：标题；mermaker Burritos Tortilla Blanket 2.0 Double Sided 47 inches for Adult and Kids,Giant Funny Realistic Food Throw Blanket,285 GSM Novelty Soft Flannel Taco Blanket (Yellow Blanket-Double Sided) */
        @Schema(description = "ASIN详情及优惠趋势响应参数：标题；mermaker Burritos Tortilla Blanket 2.0 Double Sided 47 inches for Adult and Kids,Giant Funny Realistic Food Throw Blanket,285 GSM Novelty Soft Flannel Taco Blanket (Yellow Blanket-Double Sided)")
        private String title;

        /** ASIN详情及优惠趋势响应参数：五点描述 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：五点描述")
        private List<String> features;

        /** ASIN详情及优惠趋势响应参数：详情，json格式字符串 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：详情，json格式字符串")
        private String overviews;

        /** ASIN详情及优惠趋势响应参数：更新时间；1609059137000 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：更新时间；1609059137000")
        private Long updatedTime;

        /** ASIN详情及优惠趋势响应参数：变体；[{"asin":"B07V5GB9B5","attribute":"Beige"},{"asin":"B08H86SSSF","attribute":"Cookie"}] */
        @Schema(description = "ASIN详情及优惠趋势响应参数：变体；[{\"asin\":\"B07V5GB9B5\",\"attribute\":\"Beige\"},{\"asin\":\"B08H86SSSF\",\"attribute\":\"Cookie\"}]")
        private List<VariationVo> variationList;

        /** ASIN详情及优惠趋势响应参数：变体数量；14 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：变体数量；14")
        private Integer variations;

        /** ASIN详情及优惠趋势响应参数：重量；15.2 ounces */
        @Schema(description = "ASIN详情及优惠趋势响应参数：重量；15.2 ounces")
        private String weight;

        /** ASIN详情及优惠趋势响应参数：大图 URL；https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US600.jpg */
        @Schema(description = "ASIN详情及优惠趋势响应参数：大图 URL；https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US600.jpg")
        private String zoomImageUrl;

        /** ASIN详情及优惠趋势响应参数：子类目信息 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：子类目信息")
        private List<SubcategoryVo> subcategories;

        /** ASIN详情及优惠趋势响应参数：卖家运费,-1表示没有；4 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：卖家运费,-1表示没有；4")
        private BigDecimal deliveryPrice;

        /** ASIN详情及优惠趋势响应参数：prime价格，-1表示没有；42 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：prime价格，-1表示没有；42")
        private BigDecimal primePrice;

        /** ASIN详情及优惠趋势响应参数：优惠卷；[save $20] */
        @Schema(description = "ASIN详情及优惠趋势响应参数：优惠卷；[save $20]")
        private String coupon;

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

    @Data
    @Schema(description = "ASIN详情及优惠趋势响应参数：Coupon Trends")
    public static class CouponTrendsVo {

        /** ASIN详情及优惠趋势响应参数：marketplace；见表 1.2 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：marketplace；见表 1.2")
        private String marketplace;

        /** ASIN详情及优惠趋势响应参数：asin；B08GHW4TBS */
        @Schema(description = "ASIN详情及优惠趋势响应参数：asin；B08GHW4TBS")
        private String asin;

        /** ASIN详情及优惠趋势响应参数：日期 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：日期")
        private String date;

        /** ASIN详情及优惠趋势响应参数：优惠类型；M: 减免金额, P: 百分比折扣 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：优惠类型；M: 减免金额, P: 百分比折扣")
        private String type;

        /** ASIN详情及优惠趋势响应参数：ASIN价格 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：ASIN价格")
        private BigDecimal asinPrice;

        /** ASIN详情及优惠趋势响应参数：优惠金额 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：优惠金额")
        private BigDecimal couponPrice;

        /** ASIN详情及优惠趋势响应参数：实际价格 */
        @Schema(description = "ASIN详情及优惠趋势响应参数：实际价格")
        private BigDecimal finalPrice;

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

}
