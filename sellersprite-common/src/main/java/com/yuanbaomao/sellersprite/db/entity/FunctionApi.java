package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("function_api")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "功能接口关联实体")
public class FunctionApi extends BaseAudit {

    @TableId("function_api_id")
    @Schema(description = "功能接口关联ID")
    private String functionApiId;

    @TableField("sys_function_id")
    @Schema(description = "系统功能ID")
    private String sysFunctionId;

    @TableField("sys_api_id")
    @Schema(description = "系统接口ID")
    private String sysApiId;
}
