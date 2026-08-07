package cyou.yuanbaomao.sellersprite.system.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "角色权限与有效接口预览")
public class RolePermissionVo {

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "已选功能ID集合")
    private List<String> functionIds = new ArrayList<>();

    @Schema(description = "直接附加接口ID集合")
    private List<String> extraApiIds = new ArrayList<>();

    @Schema(description = "启用的有效接口及来源")
    private List<RoleEffectiveApiVo> effectiveApis = new ArrayList<>();
}
