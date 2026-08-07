package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user_role")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户角色实体")
public class UserRole extends BaseAudit {

    @TableId("user_role_id")
    @Schema(description = "用户角色ID")
    private String userRoleId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private String userId;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private String roleId;

    @TableField("dept_id")
    @Schema(description = "角色所属部门ID")
    private String deptId;

    @TableField("primary_role")
    @Schema(description = "是否主角色：1是 0否")
    private Integer primaryRole;
}
