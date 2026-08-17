package cyou.yuanbaomao.sellersprite.system.ops.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录日志")
public class LoginLogVo {

    private String loginLogId;
    private String userId;
    private String username;
    private String loginType;
    private Integer success;
    private String errorCode;
    private String failureReason;
    private String loginIp;
    private String loginLocation;
    private String userAgent;
    private String deviceName;
    private String clientType;
    private String traceId;
    private Long createdAt;
}
