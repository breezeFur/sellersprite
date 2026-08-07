package cyou.yuanbaomao.sellersprite.system.role.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "替换角色权限请求")
public class RolePermissionReplaceRequest {

    @NotNull(message = "功能ID集合不能为空")
    @Schema(description = "功能ID集合")
    private List<String> functionIds;

    @NotNull(message = "额外接口ID集合不能为空")
    @Schema(description = "额外接口ID集合")
    private List<String> extraApiIds;
}
