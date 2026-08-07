package cyou.yuanbaomao.sellersprite.system.role.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建角色请求")
public class RoleCreateRequest {

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码不能超过64个字符")
    @Schema(description = "角色编码")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 128, message = "角色名称不能超过128个字符")
    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色类型")
    private String roleType;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
