package com.yuanbaomao.sellersprite.system.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建用户请求")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名不能超过64个字符")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度必须在6到128个字符之间")
    @Schema(description = "密码")
    private String password;

    @Size(max = 64, message = "昵称不能超过64个字符")
    @Schema(description = "昵称")
    private String nickname;

    @Size(max = 64, message = "真实姓名不能超过64个字符")
    @Schema(description = "真实姓名")
    private String realName;

    @Size(max = 32, message = "手机号不能超过32个字符")
    @Schema(description = "手机号")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "主部门ID")
    private String primaryDeptId;
}
