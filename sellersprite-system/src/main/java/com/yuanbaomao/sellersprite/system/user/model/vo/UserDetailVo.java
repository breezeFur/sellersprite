package com.yuanbaomao.sellersprite.system.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "用户详情")
public class UserDetailVo {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "主部门ID")
    private String primaryDeptId;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    @Schema(description = "角色ID集合")
    private List<String> roleIds = new ArrayList<>();
}
