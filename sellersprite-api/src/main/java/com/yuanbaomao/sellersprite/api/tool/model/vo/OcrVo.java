// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.tool.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 图片文字识别响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "图片文字识别响应模型")
public class OcrVo {

    /** 图片文字识别响应参数：识别的文字；卖家精灵 */
    @Schema(description = "图片文字识别响应参数：识别的文字；卖家精灵")
    private String data;

    /** 图片文字识别响应参数：状态码；OK */
    @Schema(description = "图片文字识别响应参数：状态码；OK")
    private Integer code;

    /** 图片文字识别响应参数：状态码描述；成功 */
    @Schema(description = "图片文字识别响应参数：状态码描述；成功")
    private String message;

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

}
