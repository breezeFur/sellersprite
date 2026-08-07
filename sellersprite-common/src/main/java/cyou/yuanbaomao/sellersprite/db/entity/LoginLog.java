package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("login_log")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录日志实体")
public class LoginLog extends BaseCreateTime {

    @TableId("login_log_id")
    @Schema(description = "登录日志ID")
    private String loginLogId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private String userId;

    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    @TableField("login_type")
    @Schema(description = "登录方式")
    private String loginType;

    @TableField("success")
    @Schema(description = "是否成功：1成功 0失败")
    private Integer success;

    @TableField("error_code")
    @Schema(description = "稳定错误码")
    private String errorCode;

    @TableField("failure_reason")
    @Schema(description = "失败原因")
    private String failureReason;

    @TableField("login_ip")
    @Schema(description = "登录IP")
    private String loginIp;

    @TableField("login_location")
    @Schema(description = "登录地点")
    private String loginLocation;

    @TableField("user_agent")
    @Schema(description = "用户代理")
    private String userAgent;

    @TableField("device_name")
    @Schema(description = "设备名称")
    private String deviceName;

    @TableField("client_type")
    @Schema(description = "客户端类型")
    private String clientType;

    @TableField("track_id")
    @Schema(description = "链路追踪ID")
    private String trackId;
}
