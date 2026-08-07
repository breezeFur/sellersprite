package cyou.yuanbaomao.sellersprite.system.dashboard.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "首页趋势点")
public class DashboardTrendPointVo {
    private String date;
    private Long loginCount;
    private Long aiConversationCount;
    private Long aiCallCount;
}
