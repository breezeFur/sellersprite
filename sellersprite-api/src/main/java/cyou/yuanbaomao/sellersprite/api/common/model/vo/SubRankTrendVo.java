package cyou.yuanbaomao.sellersprite.api.common.model.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SellerSprite 小类排名趋势，对应官方 SubRankTrendDto。
 */
@Data
@Schema(description = "SellerSprite 小类排名趋势")
public class SubRankTrendVo {

    /** Amazon 小类节点 ID。 */
    @Schema(description = "Amazon 小类节点 ID")
    private String nodeId;

    /** Amazon 小类节点名称。 */
    @Schema(description = "Amazon 小类节点名称")
    private String node;

    /** 该小类下的排名趋势点。 */
    @Schema(description = "该小类下的排名趋势点")
    private List<NumericTrendPointVo> ranks;
}
