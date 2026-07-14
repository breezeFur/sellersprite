package com.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "系统功能响应")
public class SysFunctionVo {

    @Schema(description = "系统功能ID")
    private String sysFunctionId;

    @Schema(description = "父功能ID")
    private String parentId;

    @Schema(description = "功能编码")
    private String functionCode;

    @Schema(description = "功能名称")
    private String functionName;

    @Schema(description = "功能类型")
    private String functionType;

    @Schema(description = "路由路径")
    private String routePath;

    private String componentPath;

    private String icon;

    private Integer visible;

    private Integer cacheable;

    private String externalLink;

    @Schema(description = "权限标识")
    private String permissionCode;

    @Schema(description = "排序值")
    private Integer sortOrder;

    private Integer status;

    private List<SysFunctionVo> children = new ArrayList<>();
}
