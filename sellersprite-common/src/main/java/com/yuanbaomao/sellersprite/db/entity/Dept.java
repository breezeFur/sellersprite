package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("dept")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "部门实体")
public class Dept extends BaseAudit {

    @TableId("dept_id")
    @Schema(description = "部门ID")
    private String deptId;

    @TableField("parent_id")
    @Schema(description = "父部门ID")
    private String parentId;

    @TableField("dept_code")
    @Schema(description = "部门编码")
    private String deptCode;

    @TableField("dept_name")
    @Schema(description = "部门名称")
    private String deptName;

    @TableField("dept_path")
    @Schema(description = "部门路径")
    private String deptPath;

    @TableField("leader_user_id")
    @Schema(description = "负责人用户ID")
    private String leaderUserId;

    @TableField("phone")
    @Schema(description = "联系电话")
    private String phone;

    @TableField("email")
    @Schema(description = "邮箱")
    private String email;

    @TableField("sort_order")
    @Schema(description = "排序值")
    private Integer sortOrder;

    @TableField("status")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
