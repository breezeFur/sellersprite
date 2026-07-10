package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户实体")
public class User extends BaseAudit {

    @TableId("user_id")
    @Schema(description = "用户ID")
    private String userId;

    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    @TableField("password_hash")
    @Schema(description = "密码哈希")
    private String passwordHash;

    @TableField("nickname")
    @Schema(description = "昵称")
    private String nickname;

    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField("avatar_url")
    @Schema(description = "头像地址")
    private String avatarUrl;

    @TableField("mobile")
    @Schema(description = "手机号")
    private String mobile;

    @TableField("email")
    @Schema(description = "邮箱")
    private String email;

    @TableField("gender")
    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @TableField("primary_dept_id")
    @Schema(description = "主部门ID")
    private String primaryDeptId;

    @TableField("status")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    @TableField("last_login_at")
    @Schema(description = "最后登录时间，Unix毫秒")
    private Long lastLoginAt;

    @TableField("password_updated_at")
    @Schema(description = "密码更新时间，Unix毫秒")
    private Long passwordUpdatedAt;

    @TableField("permission_version")
    @Schema(description = "权限版本")
    private Long permissionVersion;
}
