package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("role")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色实体")
public class Role extends BaseAudit {

    @TableId("role_id")
    @Schema(description = "角色ID")
    private String roleId;

    @TableField("role_code")
    @Schema(description = "角色编码")
    private String roleCode;

    @TableField("role_name")
    @Schema(description = "角色名称")
    private String roleName;

    @TableField("role_type")
    @Schema(description = "角色类型")
    private String roleType;

    @TableField("sort_order")
    @Schema(description = "排序值")
    private Integer sortOrder;

    @TableField("status")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
