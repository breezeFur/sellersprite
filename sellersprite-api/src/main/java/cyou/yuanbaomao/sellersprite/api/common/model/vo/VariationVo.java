package cyou.yuanbaomao.sellersprite.api.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Amazon 商品变体摘要。
 */
@Data
@Schema(description = "Amazon 商品变体摘要")
public class VariationVo {

    /** 变体 ASIN。 */
    @Schema(description = "变体 ASIN")
    private String asin;

    /** 区分变体的属性文本，例如颜色和尺寸。 */
    @Schema(description = "区分变体的属性文本，例如颜色和尺寸")
    private String attribute;
}
