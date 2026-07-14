package com.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "用户有效接口权限")
public class EffectiveApiPermissionVo {

    @Schema(description = "系统接口ID")
    private String sysApiId;

    @Schema(description = "接口编码")
    private String apiCode;

    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "接口路径模式")
    private String pathPattern;

    @Schema(description = "权限标识")
    private String permissionCode;

    @Schema(description = "授权来源")
    private List<PermissionApiSourceVo> sources = new ArrayList<>();
}
