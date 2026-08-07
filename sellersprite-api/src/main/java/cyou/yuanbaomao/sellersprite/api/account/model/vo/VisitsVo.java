// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * SellerSprite 可用次数响应。
 *
 * <p>官方概览只说明该接口返回当前月份各模块可用次数，未公开 data 子字段结构，
 * 因此保留完整 JSON 节点而不猜造字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SellerSprite 当前月份各模块可用次数")
public class VisitsVo {

    @Schema(description = "官方未公开固定结构的各模块可用次数 JSON")
    private JsonNode details;
}
