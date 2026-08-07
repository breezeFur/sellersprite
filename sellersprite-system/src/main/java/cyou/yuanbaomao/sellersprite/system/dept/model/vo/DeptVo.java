package cyou.yuanbaomao.sellersprite.system.dept.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "部门响应")
public class DeptVo {

    @Schema(description = "部门ID")
    private String deptId;

    @Schema(description = "父部门ID")
    private String parentId;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门路径")
    private String deptPath;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    @Schema(description = "子部门")
    private List<DeptVo> children = new ArrayList<>();
}
