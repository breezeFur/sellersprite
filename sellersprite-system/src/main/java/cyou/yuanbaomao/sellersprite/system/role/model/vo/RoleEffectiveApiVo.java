package cyou.yuanbaomao.sellersprite.system.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色有效接口授权")
public class RoleEffectiveApiVo {

    @Schema(description = "系统接口ID")
    private String sysApiId;

    @Schema(description = "接口编码")
    private String apiCode;

    @Schema(description = "接口名称")
    private String apiName;

    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "接口路径模式")
    private String pathPattern;

    @Schema(description = "权限标识")
    private String permissionCode;

    @Schema(description = "授权来源：FUNCTION、EXTRA、BOTH")
    private String grantSource;
}
