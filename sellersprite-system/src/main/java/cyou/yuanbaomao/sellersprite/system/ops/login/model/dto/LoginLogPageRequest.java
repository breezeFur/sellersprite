package cyou.yuanbaomao.sellersprite.system.ops.login.model.dto;

import cyou.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录日志分页查询")
public class LoginLogPageRequest extends PageQuery {

    @Size(max = 36, message = "用户ID不能超过36个字符")
    @Schema(description = "用户ID")
    private String userId;

    @Size(max = 64, message = "用户名不能超过64个字符")
    @Schema(description = "用户名关键字")
    private String username;

    @Min(value = 0, message = "结果只能为0或1")
    @Max(value = 1, message = "结果只能为0或1")
    @Schema(description = "是否成功：1成功 0失败")
    private Integer success;

    @Size(max = 64, message = "客户端地址不能超过64个字符")
    @Schema(description = "客户端IP关键字")
    private String loginIp;

    @Schema(description = "开始时间，Unix毫秒")
    private Long startTime;

    @Schema(description = "结束时间，Unix毫秒")
    private Long endTime;
}
