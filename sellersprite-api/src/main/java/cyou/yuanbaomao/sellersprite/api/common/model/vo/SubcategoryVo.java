package cyou.yuanbaomao.sellersprite.api.common.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 商品子类目排名。
 */
@Data
@Slf4j
@Schema(description = "商品子类目排名")
public class SubcategoryVo {

    @Schema(description = "Amazon 类目编码")
    private String code;

    @Schema(description = "商品在该子类目中的排名")
    private Integer rank;

    @Schema(description = "Amazon 类目名称")
    private String label;

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
