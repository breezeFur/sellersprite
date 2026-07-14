package com.yuanbaomao.sellersprite.system.role.model.dto;

import com.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色用户分页查询请求")
public class RoleUserPageRequest extends PageQuery {

    @Schema(description = "用户名关键字")
    private String username;
}
