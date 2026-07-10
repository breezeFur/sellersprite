package com.yuanbaomao.sellersprite.system.permission.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建系统接口请求")
public class SysApiCreateRequest {

    @NotBlank(message = "接口编码不能为空")
    @Size(max = 128, message = "接口编码不能超过128个字符")
    @Schema(description = "接口编码")
    private String apiCode;

    @NotBlank(message = "接口名称不能为空")
    @Size(max = 128, message = "接口名称不能超过128个字符")
    @Schema(description = "接口名称")
    private String apiName;

    @NotBlank(message = "接口类型不能为空")
    @Schema(description = "接口类型：PUBLIC PERMISSION")
    private String apiType;

    @NotBlank(message = "HTTP方法不能为空")
    @Schema(description = "HTTP方法")
    private String httpMethod;

    @NotBlank(message = "接口路径不能为空")
    @Schema(description = "接口路径模式")
    private String pathPattern;

    @Schema(description = "权限标识")
    private String permissionCode;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "操作名称")
    private String operationName;
}
