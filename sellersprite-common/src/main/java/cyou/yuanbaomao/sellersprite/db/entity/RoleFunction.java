package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("role_function")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色功能实体")
public class RoleFunction extends BaseAudit {

    @TableId("role_function_id")
    @Schema(description = "角色功能ID")
    private String roleFunctionId;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private String roleId;

    @TableField("sys_function_id")
    @Schema(description = "系统功能ID")
    private String sysFunctionId;
}
