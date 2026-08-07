package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class BaseAudit {

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间，Unix毫秒")
    private Long createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间，Unix毫秒")
    private Long updatedAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private String createdBy;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private String updatedBy;

    @TableLogic
    @TableField("deleted")
    @Schema(description = "逻辑删除：0正常 1删除")
    private Integer deleted;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
