// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.market.model.vo;

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
 * 选市场-卖家所属地分布响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "选市场-卖家所属地分布响应模型")
public class MarketSellerLocationVo {

    /** 选市场-卖家所属地分布响应参数：类型说明；美国 */
    @Schema(description = "选市场-卖家所属地分布响应参数：类型说明；美国")
    private String label;

    /** 选市场-卖家所属地分布响应参数：国家；美国 */
    @Schema(description = "选市场-卖家所属地分布响应参数：国家；美国")
    private String country;

    /** 选市场-卖家所属地分布响应参数：包含的asin列表；["B00P19MFYE"] */
    @Schema(description = "选市场-卖家所属地分布响应参数：包含的asin列表；[\"B00P19MFYE\"]")
    private List<String> asins;

    /** 选市场-卖家所属地分布响应参数：产品数；3 */
    @Schema(description = "选市场-卖家所属地分布响应参数：产品数；3")
    private Integer products;

    /** 选市场-卖家所属地分布响应参数：销售额；47492.83 */
    @Schema(description = "选市场-卖家所属地分布响应参数：销售额；47492.83")
    private BigDecimal revenue;

    /** 选市场-卖家所属地分布响应参数：销量；4107 */
    @Schema(description = "选市场-卖家所属地分布响应参数：销量；4107")
    private Integer units;

    /** 选市场-卖家所属地分布响应参数：销量占比；0.7313 */
    @Schema(description = "选市场-卖家所属地分布响应参数：销量占比；0.7313")
    private BigDecimal unitsRatio;

    /** 选市场-卖家所属地分布响应参数：销售额占比；0.7794 */
    @Schema(description = "选市场-卖家所属地分布响应参数：销售额占比；0.7794")
    private BigDecimal revenueRatio;

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
