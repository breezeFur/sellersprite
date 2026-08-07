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
 * Amazon 商品徽章标识。
 */
@Data
@Slf4j
@Schema(description = "Amazon 商品徽章标识")
public class BadgeVo {

    @Schema(description = "Best Seller 标识或徽章文案")
    private String bestSeller;

    @Schema(description = "Amazon's Choice 标识，Y 表示有，N 表示无")
    private String amazonChoice;

    @Schema(description = "New Release 标识，Y 表示有，N 表示无")
    private String newRelease;

    @Schema(description = "是否包含 A+ 页面，Y 表示有，N 表示无")
    private String ebc;

    @Schema(description = "是否包含视频介绍，Y 表示有，N 表示无")
    private String video;

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
