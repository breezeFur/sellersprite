package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_function")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统功能实体")
public class SysFunction extends BaseAudit {

    @TableId("sys_function_id")
    @Schema(description = "系统功能ID")
    private String sysFunctionId;

    @TableField("parent_id")
    @Schema(description = "父功能ID")
    private String parentId;

    @TableField("function_code")
    @Schema(description = "功能编码")
    private String functionCode;

    @TableField("function_name")
    @Schema(description = "功能名称")
    private String functionName;

    @TableField("function_type")
    @Schema(description = "功能类型")
    private String functionType;

    @TableField("route_path")
    @Schema(description = "前端路由路径")
    private String routePath;

    @TableField("component_path")
    @Schema(description = "前端组件路径")
    private String componentPath;

    @TableField("permission_code")
    @Schema(description = "权限标识")
    private String permissionCode;

    @TableField("icon")
    @Schema(description = "图标")
    private String icon;

    @TableField("visible")
    @Schema(description = "是否可见：1是 0否")
    private Integer visible;

    @TableField("cacheable")
    @Schema(description = "是否缓存：1是 0否")
    private Integer cacheable;

    @TableField("external_link")
    @Schema(description = "外链地址")
    private String externalLink;

    @TableField("sort_order")
    @Schema(description = "排序值")
    private Integer sortOrder;

    @TableField("status")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
