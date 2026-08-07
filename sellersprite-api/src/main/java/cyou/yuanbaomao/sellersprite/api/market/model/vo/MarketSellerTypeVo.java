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
 * 选市场-卖家类型分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "选市场-卖家类型分布响应模型")
public class MarketSellerTypeVo {

    /** 选市场-卖家类型分布响应参数：类型说明；Amazon自营 */
    @Schema(description = "选市场-卖家类型分布响应参数：类型说明；Amazon自营")
    private String label;

    /** 选市场-卖家类型分布响应参数：ASIN数量；4 */
    @Schema(description = "选市场-卖家类型分布响应参数：ASIN数量；4")
    private Integer asinNum;

    /** 选市场-卖家类型分布响应参数：ASIN数量占比；0.03 */
    @Schema(description = "选市场-卖家类型分布响应参数：ASIN数量占比；0.03")
    private BigDecimal asinRatio;

    /** 选市场-卖家类型分布响应参数：月销量；79875 */
    @Schema(description = "选市场-卖家类型分布响应参数：月销量；79875")
    private Integer units;

    /** 选市场-卖家类型分布响应参数：月销量占比；0.0345 */
    @Schema(description = "选市场-卖家类型分布响应参数：月销量占比；0.0345")
    private BigDecimal unitsRatio;

    /** 选市场-卖家类型分布响应参数：评分数；6607 */
    @Schema(description = "选市场-卖家类型分布响应参数：评分数；6607")
    private Integer ratings;

    /** 选市场-卖家类型分布响应参数：评分值；4.7 */
    @Schema(description = "选市场-卖家类型分布响应参数：评分值；4.7")
    private BigDecimal rating;

    /** 选市场-卖家类型分布响应参数：商品总数；3 */
    @Schema(description = "选市场-卖家类型分布响应参数：商品总数；3")
    private Integer productNum;

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
