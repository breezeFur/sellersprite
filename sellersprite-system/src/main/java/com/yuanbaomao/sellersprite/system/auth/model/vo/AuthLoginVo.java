package com.yuanbaomao.sellersprite.system.auth.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class AuthLoginVo {

    @Schema(description = "访问令牌")
    private String accessToken;

    @JsonIgnore
    @Schema(description = "刷新令牌，仅供服务端写入 HttpOnly Cookie", hidden = true)
    private String refreshToken;

    @Schema(description = "令牌类型")
    private String tokenType;

    @Schema(description = "过期时间，Unix毫秒")
    private Long expiresAt;

    @Schema(description = "用户信息")
    private UserDetailVo user;
}
