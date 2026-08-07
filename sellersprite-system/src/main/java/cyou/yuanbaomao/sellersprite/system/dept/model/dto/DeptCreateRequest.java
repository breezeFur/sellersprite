package cyou.yuanbaomao.sellersprite.system.dept.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建部门请求")
public class DeptCreateRequest {

    @NotBlank(message = "父部门ID不能为空")
    @Schema(description = "父部门ID，根节点填0")
    private String parentId;

    @NotBlank(message = "部门编码不能为空")
    @Size(max = 64, message = "部门编码不能超过64个字符")
    @Schema(description = "部门编码")
    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 128, message = "部门名称不能超过128个字符")
    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "负责人用户ID")
    private String leaderUserId;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
