package cyou.yuanbaomao.sellersprite.system.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户密码重置请求")
public class UserPasswordResetRequest {

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度必须在6到128个字符之间")
    @Schema(description = "新密码")
    private String password;
}
