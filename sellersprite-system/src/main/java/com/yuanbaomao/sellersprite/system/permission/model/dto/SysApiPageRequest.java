package com.yuanbaomao.sellersprite.system.permission.model.dto;

import com.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统接口分页查询请求")
public class SysApiPageRequest extends PageQuery {

    private String keyword;
    private String apiType;
    private String httpMethod;
    private String moduleName;
    private Integer status;
}
