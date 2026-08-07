package cyou.yuanbaomao.sellersprite.api.common.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * SellerSprite 商品研究通用明细。
 */
@Data
@Slf4j
@Schema(description = "SellerSprite 商品研究通用明细")
public class ProductSummaryVo {

    @Schema(description = "Amazon 标准商品编码 ASIN")
    private String asin;

    @Schema(description = "品牌名称")
    private String brand;

    @Schema(description = "Amazon 品牌搜索地址")
    private String brandUrl;

    @Schema(description = "商品主图地址")
    private String imageUrl;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "父体 ASIN")
    private String parent;

    @Schema(description = "主类目节点 ID")
    private Long nodeId;

    @Schema(description = "类目节点 ID 路径，以冒号分隔")
    private String nodeIdPath;

    @Schema(description = "类目名称路径")
    private String nodeLabelPath;

    @Schema(description = "畅销标识")
    private String symbol;

    @Schema(description = "BSR 类目编码")
    private String bsrId;

    @Schema(description = "BSR 排名")
    private Integer bsr;

    @Schema(description = "近 7 天 BSR 增长率")
    private BigDecimal bsrCr;

    @Schema(description = "近 7 天 BSR 增长数")
    private Integer bsrCv;

    @Schema(description = "月销量，父体口径")
    private Integer units;

    @Schema(description = "月销量增长率，父体口径")
    private BigDecimal unitsGr;

    @Schema(description = "子体近 30 日销量")
    private Integer amzUnit;

    @Schema(description = "子体近 30 日销售额")
    private BigDecimal amzSales;

    @Schema(description = "子体销量更新时间，Unix 毫秒时间戳")
    private Long amzUnitDate;

    @Schema(description = "月销售额，父体口径")
    private BigDecimal revenue;

    @Schema(description = "当前价格")
    private BigDecimal price;

    @Schema(description = "统计周期平均价格")
    private BigDecimal averagePrice;

    @Schema(description = "Prime 价格，-1 表示没有")
    private BigDecimal primePrice;

    @Schema(description = "毛利率")
    private BigDecimal profit;

    @Schema(description = "FBA 运费")
    private BigDecimal fba;

    @Schema(description = "评分数")
    private Integer ratings;

    @Schema(description = "留评率")
    private BigDecimal ratingsRate;

    @Schema(description = "评分值")
    private BigDecimal rating;

    @Schema(description = "月新增评分数")
    private Integer ratingsCv;

    @Schema(description = "近 30 天新增评论数")
    private Integer ratingDelta;

    @Schema(description = "Listing 质量得分")
    private BigDecimal lqs;

    @Schema(description = "上架时间，Unix 毫秒时间戳")
    private Long availableDate;

    @Schema(description = "配送方式，例如 AMZ、FBA 或 FBM")
    private String fulfillment;

    @Schema(description = "变体数量")
    private Integer variations;

    @Schema(description = "卖家数量")
    private Integer sellers;

    @Schema(description = "Buy Box 卖家 ID")
    private String sellerId;

    @Schema(description = "Buy Box 卖家名称")
    private String sellerName;

    @Schema(description = "Buy Box 卖家国籍编码")
    private String sellerNation;

    @Schema(description = "商品徽章")
    private BadgeVo badge;

    @Schema(description = "商品重量及单位")
    private String weight;

    @Schema(description = "商品尺寸及单位")
    private String dimension;

    @Schema(description = "商品尺寸类型")
    private String dimensionsType;

    @Schema(description = "包装尺寸及单位")
    private String pkgDimensions;

    @Schema(description = "包装尺寸类型")
    private String pkgDimensionType;

    @Schema(description = "包装重量及单位")
    private String pkgWeight;

    @Schema(description = "商品 SKU 属性描述")
    private String sku;

    @Schema(description = "子类目排名列表")
    private List<SubcategoryVo> subcategories;

    @Schema(description = "卖家运费，-1 表示没有")
    private BigDecimal deliveryPrice;

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
