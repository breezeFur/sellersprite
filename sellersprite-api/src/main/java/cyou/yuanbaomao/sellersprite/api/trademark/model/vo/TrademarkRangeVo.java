// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.trademark.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 全球商标库-数据范围响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "全球商标库-数据范围响应模型")
public class TrademarkRangeVo {

    /** 全球商标库-数据范围响应参数：简码；AD */
    @Schema(description = "全球商标库-数据范围响应参数：简码；AD")
    private String office;

    /** 全球商标库-数据范围响应参数：中文名称；安道尔 */
    @Schema(description = "全球商标库-数据范围响应参数：中文名称；安道尔")
    private String officeLabel;

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
