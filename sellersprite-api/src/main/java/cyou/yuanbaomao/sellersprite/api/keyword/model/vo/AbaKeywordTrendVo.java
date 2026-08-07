// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.keyword.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * ABA 数据选品-关键词趋势响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "ABA 数据选品-关键词趋势响应模型")
public class AbaKeywordTrendVo {

    /** ABA 数据选品-关键词趋势响应参数：日期 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：日期")
    private Long date;

    /** ABA 数据选品-关键词趋势响应参数：ABA排名 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：ABA排名")
    private String rank;

    /** ABA 数据选品-关键词趋势响应参数：搜索量 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：搜索量")
    private String searches;

    /** ABA 数据选品-关键词趋势响应参数：日期标签 */
    @Schema(description = "ABA 数据选品-关键词趋势响应参数：日期标签")
    private Integer label;

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
