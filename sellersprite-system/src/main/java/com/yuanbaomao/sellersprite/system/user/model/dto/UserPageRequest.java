package com.yuanbaomao.sellersprite.system.user.model.dto;

import com.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户分页查询请求")
public class UserPageRequest extends PageQuery {

    @Schema(description = "用户名关键字")
    private String username;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
