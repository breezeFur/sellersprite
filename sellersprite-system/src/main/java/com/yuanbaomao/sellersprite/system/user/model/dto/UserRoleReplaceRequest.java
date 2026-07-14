package com.yuanbaomao.sellersprite.system.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "用户角色替换请求")
public class UserRoleReplaceRequest {

    @NotNull(message = "角色ID集合不能为空")
    @Schema(description = "角色ID集合，空数组表示解除全部角色")
    private List<@NotBlank(message = "角色ID不能为空") String> roleIds = new ArrayList<>();
}
