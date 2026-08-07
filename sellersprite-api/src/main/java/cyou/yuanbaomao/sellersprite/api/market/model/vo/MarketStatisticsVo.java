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
 * 选市场-统计响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "选市场-统计响应模型")
public class MarketStatisticsVo {

    /** 选市场-统计响应参数：市场标志；US */
    @Schema(description = "选市场-统计响应参数：市场标志；US")
    private String marketplace;

    /** 选市场-统计响应参数：该市场的货币类型；USD */
    @Schema(description = "选市场-统计响应参数：该市场的货币类型；USD")
    private String currency;

    /** 选市场-统计响应参数：节点ID路径；1064954:1069242:1069784:1069820:1069838:1069828 */
    @Schema(description = "选市场-统计响应参数：节点ID路径；1064954:1069242:1069784:1069820:1069838:1069828")
    private String nodeIdPath;

    /** 选市场-统计响应参数：节点名称路径；Office Products:Office & School Supplies:Writing & Correction Supplies:Pens & Refills:Rollerball Pens:Gel Ink Rollerball Pens */
    @Schema(description = "选市场-统计响应参数：节点名称路径；Office Products:Office & School Supplies:Writing & Correction Supplies:Pens & Refills:Rollerball Pens:Gel Ink Rollerball Pens")
    private String nodeLabelPath;

    /** 选市场-统计响应参数：节点名称翻译；办公产品:办公室:写作:钢笔:滚珠笔:中性笔 */
    @Schema(description = "选市场-统计响应参数：节点名称翻译；办公产品:办公室:写作:钢笔:滚珠笔:中性笔")
    private String nodeLabelLocale;

    /** 选市场-统计响应参数：国家二简码；US */
    @Schema(description = "选市场-统计响应参数：国家二简码；US")
    private String countryCode;

    /** 选市场-统计响应参数：商品总数；5127 */
    @Schema(description = "选市场-统计响应参数：商品总数；5127")
    private Integer totalProducts;

    /** 选市场-统计响应参数：样品商品数；100 */
    @Schema(description = "选市场-统计响应参数：样品商品数；100")
    private Integer products;

    /** 选市场-统计响应参数：品牌数；4 */
    @Schema(description = "选市场-统计响应参数：品牌数；4")
    private Integer brands;

    /** 选市场-统计响应参数：卖家数；58 */
    @Schema(description = "选市场-统计响应参数：卖家数；58")
    private Integer sellers;

    /** 选市场-统计响应参数：平均BSR；41970 */
    @Schema(description = "选市场-统计响应参数：平均BSR；41970")
    private Integer avgBsr;

    /** 选市场-统计响应参数：平均体积(cm³)；819942.68 */
    @Schema(description = "选市场-统计响应参数：平均体积(cm³)；819942.68")
    private BigDecimal baseAvgVolume;

    /** 选市场-统计响应参数：平均体积(in³)；50035.97 */
    @Schema(description = "选市场-统计响应参数：平均体积(in³)；50035.97")
    private BigDecimal avgVolume;

    /** 选市场-统计响应参数：平均重量(g)；2460.95 */
    @Schema(description = "选市场-统计响应参数：平均重量(g)；2460.95")
    private BigDecimal baseAvgWeight;

    /** 选市场-统计响应参数：平均重量(pound)；5.4255 */
    @Schema(description = "选市场-统计响应参数：平均重量(pound)；5.4255")
    private BigDecimal avgWeight;

    /** 选市场-统计响应参数：平均利润率；66.03 */
    @Schema(description = "选市场-统计响应参数：平均利润率；66.03")
    private BigDecimal avgProfit;

    /** 选市场-统计响应参数：月均销量；26255 */
    @Schema(description = "选市场-统计响应参数：月均销量；26255")
    private Integer avgUnits;

    /** 选市场-统计响应参数：月均销售额；344369 */
    @Schema(description = "选市场-统计响应参数：月均销售额；344369")
    private BigDecimal avgRevenue;

    /** 选市场-统计响应参数：平均价格；13.91 */
    @Schema(description = "选市场-统计响应参数：平均价格；13.91")
    private BigDecimal avgPrice;

    /** 选市场-统计响应参数：月评论平均增长数；0 */
    @Schema(description = "选市场-统计响应参数：月评论平均增长数；0")
    private Integer avgRatingsCv;

    /** 选市场-统计响应参数：平均评分数；19071 */
    @Schema(description = "选市场-统计响应参数：平均评分数；19071")
    private Integer avgRatings;

    /** 选市场-统计响应参数：平均星级；4.7 */
    @Schema(description = "选市场-统计响应参数：平均星级；4.7")
    private BigDecimal avgRating;

    /** 选市场-统计响应参数：平均卖家数；5.2 */
    @Schema(description = "选市场-统计响应参数：平均卖家数；5.2")
    private BigDecimal avgSellers;

    /** 选市场-统计响应参数：头部Listing前N名商品样本数；5 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品样本数；5")
    private Integer hlProducts;

    /** 选市场-统计响应参数：头部Listing前N名商品平均BSR；13126 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品平均BSR；13126")
    private Integer hlAvgBsr;

    /** 选市场-统计响应参数：头部Listing前N名商品月均销量；1123 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品月均销量；1123")
    private Integer hlAvgUnits;

    /** 选市场-统计响应参数：头部Listing前N名商品月均销售额；12342.85 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品月均销售额；12342.85")
    private BigDecimal hlAvgRevenue;

    /** 选市场-统计响应参数：头部Listing前N名商品平均价格；11.77 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品平均价格；11.77")
    private BigDecimal hlAvgPrice;

    /** 选市场-统计响应参数：头部Listing前N名商品月评论平均增长数；0 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品月评论平均增长数；0")
    private Integer hlAvgRatingsCv;

    /** 选市场-统计响应参数：头部Listing前N名商品平均评论数；2794 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品平均评论数；2794")
    private Integer hlAvgRatings;

    /** 选市场-统计响应参数：头部Listing前N名商品平均星级；4.7 */
    @Schema(description = "选市场-统计响应参数：头部Listing前N名商品平均星级；4.7")
    private BigDecimal hlAvgRating;

    /** 选市场-统计响应参数：新品数量；67 */
    @Schema(description = "选市场-统计响应参数：新品数量；67")
    private Integer newProducts;

    /** 选市场-统计响应参数：新品数量占比；67 */
    @Schema(description = "选市场-统计响应参数：新品数量占比；67")
    private BigDecimal newProductProportion;

    /** 选市场-统计响应参数：新品平均价格；14.14 */
    @Schema(description = "选市场-统计响应参数：新品平均价格；14.14")
    private BigDecimal newAvgPrice;

    /** 选市场-统计响应参数：新品平均评分数；24295 */
    @Schema(description = "选市场-统计响应参数：新品平均评分数；24295")
    private Integer newAvgRatings;

    /** 选市场-统计响应参数：最低新品评分数；24 */
    @Schema(description = "选市场-统计响应参数：最低新品评分数；24")
    private Integer minNewRatings;

    /** 选市场-统计响应参数：最高新品评分数；6432 */
    @Schema(description = "选市场-统计响应参数：最高新品评分数；6432")
    private Integer maxNewRatings;

    /** 选市场-统计响应参数：新品平均星级；4.7 */
    @Schema(description = "选市场-统计响应参数：新品平均星级；4.7")
    private BigDecimal newAvgRating;

    /** 选市场-统计响应参数：新品月均销量；26425 */
    @Schema(description = "选市场-统计响应参数：新品月均销量；26425")
    private Integer newAvgUnits;

    /** 选市场-统计响应参数：新品月均销售额；350209.91 */
    @Schema(description = "选市场-统计响应参数：新品月均销售额；350209.91")
    private BigDecimal newAvgRevenue;

    /** 选市场-统计响应参数：商品首次上架日期；2014-10-30 */
    @Schema(description = "选市场-统计响应参数：商品首次上架日期；2014-10-30")
    private String firstShelfDate;

    /** 选市场-统计响应参数：商品最新上架日期；2021-04-28 */
    @Schema(description = "选市场-统计响应参数：商品最新上架日期；2021-04-28")
    private String lastShelfDate;

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
