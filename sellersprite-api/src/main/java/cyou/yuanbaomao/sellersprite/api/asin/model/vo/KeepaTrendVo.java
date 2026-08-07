// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.NumericTrendPointVo;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.StringTrendPointVo;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.SubRankTrendVo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 商品趋势详情(keepa)响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "商品趋势详情(keepa)响应模型")
public class KeepaTrendVo {

    /** 商品趋势详情(keepa)响应参数：市场；见表 1.2 */
    @Schema(description = "商品趋势详情(keepa)响应参数：市场；见表 1.2")
    private String marketplace;

    /** 商品趋势详情(keepa)响应参数：asin；B07V34QQ3C */
    @Schema(description = "商品趋势详情(keepa)响应参数：asin；B07V34QQ3C")
    private String asin;

    /** 商品趋势详情(keepa)响应参数：实际返回Keepa数据的ASIN；B07V34QQ3C */
    @Schema(description = "商品趋势详情(keepa)响应参数：实际返回Keepa数据的ASIN；B07V34QQ3C")
    private String dataAsin;

    /** 商品趋势详情(keepa)响应参数：父体ASIN；B0CWW9N7QW */
    @Schema(description = "商品趋势详情(keepa)响应参数：父体ASIN；B0CWW9N7QW")
    private String parentAsin;

    /** 商品趋势详情(keepa)响应参数：变体ASIN列表；["B0CN2PBVNS","B0BT4PMNY4","B0C6FYKC3D","B0CSLMG2TF","B0CGGPC6G3","B0BXG8L46Y","B0CRSZGN9L","B07V34QQ3C"] */
    @Schema(description = "商品趋势详情(keepa)响应参数：变体ASIN列表；[\"B0CN2PBVNS\",\"B0BT4PMNY4\",\"B0C6FYKC3D\",\"B0CSLMG2TF\",\"B0CGGPC6G3\",\"B0BXG8L46Y\",\"B0CRSZGN9L\",\"B07V34QQ3C\"]")
    private List<String> variationAsins;

    /** 商品趋势详情(keepa)响应参数：BSR大类节点ID；172282 */
    @Schema(description = "商品趋势详情(keepa)响应参数：BSR大类节点ID；172282")
    private String rootCategory;

    /** 商品趋势详情(keepa)响应参数：跟类目；Electronics */
    @Schema(description = "商品趋势详情(keepa)响应参数：跟类目；Electronics")
    private String rootCategoryLabel;

    /** 商品趋势详情(keepa)响应参数：排名节点ID；541966 */
    @Schema(description = "商品趋势详情(keepa)响应参数：排名节点ID；541966")
    private String salesRankReference;

    /** 商品趋势详情(keepa)响应参数：排名节点变动历史；PairStrDto 趋势字符串数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：排名节点变动历史；PairStrDto 趋势字符串数据结构")
    private List<StringTrendPointVo> salesRankReferenceHistory;

    /** 商品趋势详情(keepa)响应参数：上架类目全路径；172282:541966:13896617011:565098:13896597011 */
    @Schema(description = "商品趋势详情(keepa)响应参数：上架类目全路径；172282:541966:13896617011:565098:13896597011")
    private String nodeIdPath;

    /** 商品趋势详情(keepa)响应参数：上架类目名称全路径；Electronics:Computers & Accessories:Computers & Tablets:Desktops:Towers */
    @Schema(description = "商品趋势详情(keepa)响应参数：上架类目名称全路径；Electronics:Computers & Accessories:Computers & Tablets:Desktops:Towers")
    private String nodeLabelPath;

    /** 商品趋势详情(keepa)响应参数：商品状态；STANDARD:everything accessibleDOWNLOADABLE:no marketplace/3rd party price dataEBOOK:no price data and sales rank accessibleINACCESSIBLE:no data accessibleINVALID:invalid or deprecated asinVARIATION_PARENT:product is a parent ASINUNKNOWN:null of status */
    @Schema(description = "商品趋势详情(keepa)响应参数：商品状态；STANDARD:everything accessibleDOWNLOADABLE:no marketplace/3rd party price dataEBOOK:no price data and sales rank accessibleINACCESSIBLE:no data accessibleINVALID:invalid or deprecated asinVARIATION_PARENT:product is a parent ASINUNKNOWN:null of status")
    private String productStatus;

    /** 商品趋势详情(keepa)响应参数：亚马逊跟卖转态；-1 */
    @Schema(description = "商品趋势详情(keepa)响应参数：亚马逊跟卖转态；-1")
    private String availabilityAmazon;

    /** 商品趋势详情(keepa)响应参数：标题；iBUYPOWER Gaming PC Computer Desktop Element 9260 (Intel Core i7-9700F 3.0Ghz, NVIDIA GeForce GTX 1660 Ti 6GB, 16GB DDR4, 240GB SSD, 1TB HDD, Wi-Fi & Windows 10 Home) Black */
    @Schema(description = "商品趋势详情(keepa)响应参数：标题；iBUYPOWER Gaming PC Computer Desktop Element 9260 (Intel Core i7-9700F 3.0Ghz, NVIDIA GeForce GTX 1660 Ti 6GB, 16GB DDR4, 240GB SSD, 1TB HDD, Wi-Fi & Windows 10 Home) Black")
    private String title;

    /** 商品趋势详情(keepa)响应参数：品牌；iBUYPOWER */
    @Schema(description = "商品趋势详情(keepa)响应参数：品牌；iBUYPOWER")
    private String brand;

    /** 商品趋势详情(keepa)响应参数：ASIN链接；https://www.amazon.com/dp/B07V34QQ3C */
    @Schema(description = "商品趋势详情(keepa)响应参数：ASIN链接；https://www.amazon.com/dp/B07V34QQ3C")
    private String asinUrl;

    /** 商品趋势详情(keepa)响应参数：品牌链接；https://www.amazon.com/s?k=iBUYPOWER */
    @Schema(description = "商品趋势详情(keepa)响应参数：品牌链接；https://www.amazon.com/s?k=iBUYPOWER")
    private String brandUrl;

    /** 商品趋势详情(keepa)响应参数：销售排名链接；https://www.amazon.com/b/?node=541966 */
    @Schema(description = "商品趋势详情(keepa)响应参数：销售排名链接；https://www.amazon.com/b/?node=541966")
    private String salesRankUrl;

    /** 商品趋势详情(keepa)响应参数：商品缩略图200*200；https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US200_.jpg */
    @Schema(description = "商品趋势详情(keepa)响应参数：商品缩略图200*200；https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US200_.jpg")
    private String imageUrl;

    /** 商品趋势详情(keepa)响应参数：商品大图600*600；https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US600_.jpg */
    @Schema(description = "商品趋势详情(keepa)响应参数：商品大图600*600；https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US600_.jpg")
    private String zoomImageUrl;

    /** 商品趋势详情(keepa)响应参数：商品图片列表；["https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US200_.jpg","https://images-na.ssl-images-amazon.com/images/I/61bpfnvHjqL._AC_US200_.jpg",......] */
    @Schema(description = "商品趋势详情(keepa)响应参数：商品图片列表；[\"https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US200_.jpg\",\"https://images-na.ssl-images-amazon.com/images/I/61bpfnvHjqL._AC_US200_.jpg\",......]")
    private List<String> imageUrls;

    /** 商品趋势详情(keepa)响应参数：净尺寸；97 */
    @Schema(description = "商品趋势详情(keepa)响应参数：净尺寸；97")
    private String dimensions;

    /** 商品趋势详情(keepa)响应参数：净重量；1063280 */
    @Schema(description = "商品趋势详情(keepa)响应参数：净重量；1063280")
    private String weight;

    /** 商品趋势详情(keepa)响应参数：净重数值 单位统一为：克(g)；1055398:1063252:1063280 */
    @Schema(description = "商品趋势详情(keepa)响应参数：净重数值 单位统一为：克(g)；1055398:1063252:1063280")
    private Integer weightGram;

    /** 商品趋势详情(keepa)响应参数：打包尺寸；22 x 19.9 x 12.4 inches */
    @Schema(description = "商品趋势详情(keepa)响应参数：打包尺寸；22 x 19.9 x 12.4 inches")
    private String pkgDimensions;

    /** 商品趋势详情(keepa)响应参数：打包尺寸 长/宽/高 单位统一为：厘米(cm)；[558,506,316] */
    @Schema(description = "商品趋势详情(keepa)响应参数：打包尺寸 长/宽/高 单位统一为：厘米(cm)；[558,506,316]")
    private List<String> pkgDimensionsSize;

    /** 商品趋势详情(keepa)响应参数：打包重量；0.11 pounds */
    @Schema(description = "商品趋势详情(keepa)响应参数：打包重量；0.11 pounds")
    private String pkgWeight;

    /** 商品趋势详情(keepa)响应参数：打包重量数值 单位统一为：克(g)；13660 */
    @Schema(description = "商品趋势详情(keepa)响应参数：打包重量数值 单位统一为：克(g)；13660")
    private Integer pkgWeightGram;

    /** 商品趋势详情(keepa)响应参数：FBA总费用；26.11 */
    @Schema(description = "商品趋势详情(keepa)响应参数：FBA总费用；26.11")
    private BigDecimal fbaFees;

    /** 商品趋势详情(keepa)响应参数：FBA费用项明细JSON串，包含：仓储费，仓储费税，运送打包费，运送打包费税；"{\"pickAndPackFeeTax\":0,\"storageFee\":0,\"storageFeeTax\":0,\"pickAndPackFee\":26.11}" */
    @Schema(description = "商品趋势详情(keepa)响应参数：FBA费用项明细JSON串，包含：仓储费，仓储费税，运送打包费，运送打包费税；\"{\\\"pickAndPackFeeTax\\\":0,\\\"storageFee\\\":0,\\\"storageFeeTax\\\":0,\\\"pickAndPackFee\\\":26.11}\"")
    private String fbaItems;

    /** 商品趋势详情(keepa)响应参数：在第几页；-1 */
    @Schema(description = "商品趋势详情(keepa)响应参数：在第几页；-1")
    private Integer numberOfPages;

    /** 商品趋势详情(keepa)响应参数：在第几个；1 */
    @Schema(description = "商品趋势详情(keepa)响应参数：在第几个；1")
    private Integer numberOfItems;

    /** 商品趋势详情(keepa)响应参数：价格趋势；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：价格趋势；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> price;

    /** 商品趋势详情(keepa)响应参数：成交价趋势；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：成交价趋势；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> dealPrice;

    /** 商品趋势详情(keepa)响应参数：黄金购物车价格趋势；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：黄金购物车价格趋势；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> buyBox;

    /** 商品趋势详情(keepa)响应参数：划线价格；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：划线价格；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> priceList;

    /** 商品趋势详情(keepa)响应参数：黄金购物车卖家Id历史趋势；PairStrDto 趋势字符串数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：黄金购物车卖家Id历史趋势；PairStrDto 趋势字符串数据结构")
    private List<StringTrendPointVo> buyBoxSellerIdHistory;

    /** 商品趋势详情(keepa)响应参数：大类BSR排名历史趋势；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：大类BSR排名历史趋势；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> bsr;

    /** 商品趋势详情(keepa)响应参数：小类排名趋势数据；见 SubRankTrendDto 小类排名趋势 */
    @Schema(description = "商品趋势详情(keepa)响应参数：小类排名趋势数据；见 SubRankTrendDto 小类排名趋势")
    private List<SubRankTrendVo> subSalesRank;

    /** 商品趋势详情(keepa)响应参数：评分数趋势数据；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：评分数趋势数据；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> reviews;

    /** 商品趋势详情(keepa)响应参数：评分值趋势数据；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：评分值趋势数据；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> rating;

    /** 商品趋势详情(keepa)响应参数：卖家数趋势数据；见 PairNumberDto 趋势数字数据结构 */
    @Schema(description = "商品趋势详情(keepa)响应参数：卖家数趋势数据；见 PairNumberDto 趋势数字数据结构")
    private List<NumericTrendPointVo> sellers;

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
