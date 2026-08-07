// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.product.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 查产品类目响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "查产品类目响应模型")
public class ProductNodeVo {

    /** 查产品类目响应参数：类目 id 字符串，即 nodeIdPath；2619525011:3741271 */
    @Schema(description = "查产品类目响应参数：类目 id 字符串，即 nodeIdPath；2619525011:3741271")
    private String nodeIdPath;

    /** 查产品类目响应参数：类目名称；Appliances:Dishwashers */
    @Schema(description = "查产品类目响应参数：类目名称；Appliances:Dishwashers")
    private String nodeLabelPath;

    /** 查产品类目响应参数：类目下产品数；42 */
    @Schema(description = "查产品类目响应参数：类目下产品数；42")
    private Integer products;

    /** 查产品类目响应参数：类目节点名称中文；洗碗机 */
    @Schema(description = "查产品类目响应参数：类目节点名称中文；洗碗机")
    private String nodeLabelLocale;

    /** 查产品类目响应参数：类目所属所有节点名称中文；大家电:洗碗机 */
    @Schema(description = "查产品类目响应参数：类目所属所有节点名称中文；大家电:洗碗机")
    private String nodeLabelPathLocale;

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
