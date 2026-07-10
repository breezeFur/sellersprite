package com.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class BaseCreateTime {

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间，Unix毫秒")
    private Long createdAt;
}
