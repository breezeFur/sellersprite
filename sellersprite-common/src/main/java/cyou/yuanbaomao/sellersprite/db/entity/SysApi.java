package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_api")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统接口实体")
public class SysApi extends BaseAudit {

    @TableId("sys_api_id")
    @Schema(description = "系统接口ID")
    private String sysApiId;

    @TableField("api_code")
    @Schema(description = "接口编码")
    private String apiCode;

    @TableField("api_name")
    @Schema(description = "接口名称")
    private String apiName;

    @TableField("api_type")
    @Schema(description = "接口类型")
    private String apiType;

    @TableField("http_method")
    @Schema(description = "HTTP方法")
    private String httpMethod;

    @TableField("path_pattern")
    @Schema(description = "接口路径模式")
    private String pathPattern;

    @TableField("permission_code")
    @Schema(description = "权限标识")
    private String permissionCode;

    @TableField("module_name")
    @Schema(description = "模块名称")
    private String moduleName;

    @TableField("operation_name")
    @Schema(description = "操作名称")
    private String operationName;

    @TableField("status")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
