// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.market.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 选市场-商品集中度响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "选市场-商品集中度响应模型")
public class MarketGoodsConcentrationVo {

    /** 选市场-商品集中度响应参数：标题；Pilot G2, Dr. Grip Gel/Ltd, ExecuGel G6, Q7 Rollerball Gel Ink Pen Refills, 0.7mm, Fine Point, Black Ink, 3 Packs of 2 */
    @Schema(description = "选市场-商品集中度响应参数：标题；Pilot G2, Dr. Grip Gel/Ltd, ExecuGel G6, Q7 Rollerball Gel Ink Pen Refills, 0.7mm, Fine Point, Black Ink, 3 Packs of 2")
    private String title;

    /** 选市场-商品集中度响应参数：asin；B00P19MFYE */
    @Schema(description = "选市场-商品集中度响应参数：asin；B00P19MFYE")
    private String asin;

    /** 选市场-商品集中度响应参数：asin链接；https://www.amazon.com/dp/B00P19MFYE */
    @Schema(description = "选市场-商品集中度响应参数：asin链接；https://www.amazon.com/dp/B00P19MFYE")
    private String asinUrl;

    /** 选市场-商品集中度响应参数：图片链接；https://images-na.ssl-images-amazon.com/images/I/51hxvoxGnjL._AC_US200_.jpg */
    @Schema(description = "选市场-商品集中度响应参数：图片链接；https://images-na.ssl-images-amazon.com/images/I/51hxvoxGnjL._AC_US200_.jpg")
    private String imageUrl;

    /** 选市场-商品集中度响应参数：排名；1 */
    @Schema(description = "选市场-商品集中度响应参数：排名；1")
    private Integer ranking;

    /** 选市场-商品集中度响应参数：品牌；PILOT */
    @Schema(description = "选市场-商品集中度响应参数：品牌；PILOT")
    private String brand;

    /** 选市场-商品集中度响应参数：卖家名称；JA Wholesale LLC */
    @Schema(description = "选市场-商品集中度响应参数：卖家名称；JA Wholesale LLC")
    private String sellerName;

    /** 选市场-商品集中度响应参数：卖家类型；FBA */
    @Schema(description = "选市场-商品集中度响应参数：卖家类型；FBA")
    private String sellerType;

    /** 选市场-商品集中度响应参数：价格；6.19 */
    @Schema(description = "选市场-商品集中度响应参数：价格；6.19")
    private BigDecimal price;

    /** 选市场-商品集中度响应参数：上架时间；2014-10-30 */
    @Schema(description = "选市场-商品集中度响应参数：上架时间；2014-10-30")
    private String shelfDate;

    /** 选市场-商品集中度响应参数：评分数；5695 */
    @Schema(description = "选市场-商品集中度响应参数：评分数；5695")
    private Integer ratings;

    /** 选市场-商品集中度响应参数：评论数；133 */
    @Schema(description = "选市场-商品集中度响应参数：评论数；133")
    private Integer reviews;

    /** 选市场-商品集中度响应参数：评论值；4.8 */
    @Schema(description = "选市场-商品集中度响应参数：评论值；4.8")
    private BigDecimal rating;

    /** 选市场-商品集中度响应参数：是否新品 1新品，0非新品；0 */
    @Schema(description = "选市场-商品集中度响应参数：是否新品 1新品，0非新品；0")
    private Integer newFlag;

    /** 选市场-商品集中度响应参数：总销量；2515 */
    @Schema(description = "选市场-商品集中度响应参数：总销量；2515")
    private Integer totalUnits;

    /** 选市场-商品集中度响应参数：总销额；18837.35 */
    @Schema(description = "选市场-商品集中度响应参数：总销额；18837.35")
    private BigDecimal totalRevenue;

    /** 选市场-商品集中度响应参数：总销量占比；0.4478 */
    @Schema(description = "选市场-商品集中度响应参数：总销量占比；0.4478")
    private BigDecimal totalUnitsRatio;

    /** 选市场-商品集中度响应参数：总销额占比；0.3052 */
    @Schema(description = "选市场-商品集中度响应参数：总销额占比；0.3052")
    private BigDecimal totalRevenueRatio;

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
