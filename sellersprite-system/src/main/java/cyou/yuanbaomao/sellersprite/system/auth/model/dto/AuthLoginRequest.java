package cyou.yuanbaomao.sellersprite.system.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class AuthLoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名不能超过64个字符")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码不能超过128个字符")
    @Schema(description = "密码")
    private String password;

    @Size(max = 128, message = "设备ID不能超过128个字符")
    @Schema(description = "设备ID")
    private String deviceId;

    @Size(max = 128, message = "设备名称不能超过128个字符")
    @Schema(description = "设备名称")
    private String deviceName;

    @Size(max = 32, message = "客户端类型不能超过32个字符")
    @Schema(description = "客户端类型", example = "WEB")
    private String clientType;
}
