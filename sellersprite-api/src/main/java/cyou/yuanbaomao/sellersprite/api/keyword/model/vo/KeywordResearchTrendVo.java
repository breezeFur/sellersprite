// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.keyword.model.vo;

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
 * 关键词选品-趋势数据响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "关键词选品-趋势数据响应模型")
public class KeywordResearchTrendVo {

    /** 关键词选品-趋势数据响应参数：时间 */
    @Schema(description = "关键词选品-趋势数据响应参数：时间")
    private String time;

    /** 关键词选品-趋势数据响应参数：关键词 */
    @Schema(description = "关键词选品-趋势数据响应参数：关键词")
    private String keywrod;

    /** 关键词选品-趋势数据响应参数：关键词-中文 */
    @Schema(description = "关键词选品-趋势数据响应参数：关键词-中文")
    private String keywrodCn;

    /** 关键词选品-趋势数据响应参数：关键词-日文 */
    @Schema(description = "关键词选品-趋势数据响应参数：关键词-日文")
    private String keywrodJp;

    /** 关键词选品-趋势数据响应参数：搜索量 */
    @Schema(description = "关键词选品-趋势数据响应参数：搜索量")
    private Integer search;

    /** 关键词选品-趋势数据响应参数：购买量 */
    @Schema(description = "关键词选品-趋势数据响应参数：购买量")
    private BigDecimal purchase;

    /** 关键词选品-趋势数据响应参数：购买率 */
    @Schema(description = "关键词选品-趋势数据响应参数：购买率")
    private BigDecimal purchaseRate;

    /** 关键词选品-趋势数据响应参数：同比增长率 */
    @Schema(description = "关键词选品-趋势数据响应参数：同比增长率")
    private BigDecimal yearlyGrowth;

    /** 关键词选品-趋势数据响应参数：环比增长率 */
    @Schema(description = "关键词选品-趋势数据响应参数：环比增长率")
    private BigDecimal chainGrowth;

    /** 关键词选品-趋势数据响应参数：三个月增长率 */
    @Schema(description = "关键词选品-趋势数据响应参数：三个月增长率")
    private BigDecimal threeMonthGrowth;

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
