package cyou.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "权限菜单节点")
public class PermissionMenuVo {

    @Schema(description = "功能ID")
    private String functionId;

    @Schema(description = "父功能ID")
    private String parentId;

    @Schema(description = "功能名称")
    private String name;

    @Schema(description = "功能类型")
    private String type;

    @Schema(description = "路由路径")
    private String routePath;

    @Schema(description = "组件路径")
    private String componentPath;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "是否缓存：1是 0否")
    private Integer cacheable;

    @Schema(description = "权限标识")
    private String permissionCode;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "子菜单")
    private List<PermissionMenuVo> children = new ArrayList<>();
}
