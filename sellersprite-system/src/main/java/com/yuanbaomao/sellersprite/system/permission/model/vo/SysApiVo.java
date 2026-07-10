package com.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统接口响应")
public class SysApiVo {

    @Schema(description = "系统接口ID")
    private String sysApiId;

    @Schema(description = "接口编码")
    private String apiCode;

    @Schema(description = "接口名称")
    private String apiName;

    @Schema(description = "接口类型")
    private String apiType;

    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "接口路径模式")
    private String pathPattern;

    @Schema(description = "权限标识")
    private String permissionCode;
}
