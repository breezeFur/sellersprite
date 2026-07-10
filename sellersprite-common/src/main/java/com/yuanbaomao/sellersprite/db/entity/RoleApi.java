package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("role_api")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色接口实体")
public class RoleApi extends BaseAudit {

    @TableId("role_api_id")
    @Schema(description = "角色接口ID")
    private String roleApiId;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private String roleId;

    @TableField("sys_api_id")
    @Schema(description = "系统接口ID")
    private String sysApiId;
}
