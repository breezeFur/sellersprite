package cyou.yuanbaomao.sellersprite.system.permission.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建系统功能请求")
public class SysFunctionCreateRequest {

    @NotBlank(message = "父功能ID不能为空")
    @Schema(description = "父功能ID，根节点填0")
    private String parentId;

    @NotBlank(message = "功能编码不能为空")
    @Size(max = 128, message = "功能编码不能超过128个字符")
    @Schema(description = "功能编码")
    private String functionCode;

    @NotBlank(message = "功能名称不能为空")
    @Size(max = 128, message = "功能名称不能超过128个字符")
    @Schema(description = "功能名称")
    private String functionName;

    @NotBlank(message = "功能类型不能为空")
    @Schema(description = "功能类型：DIR MENU BUTTON")
    private String functionType;

    @Schema(description = "前端路由路径")
    private String routePath;

    @Schema(description = "前端组件路径")
    private String componentPath;

    @Schema(description = "权限标识")
    private String permissionCode;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "是否可见：1是 0否")
    private Integer visible;

    @Schema(description = "是否缓存：1是 0否")
    private Integer cacheable;

    @Schema(description = "外链地址")
    private String externalLink;

    @Schema(description = "备注")
    private String remark;
}
