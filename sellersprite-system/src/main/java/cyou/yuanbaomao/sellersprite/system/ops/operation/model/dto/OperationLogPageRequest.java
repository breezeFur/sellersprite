package cyou.yuanbaomao.sellersprite.system.ops.operation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "操作日志分页查询")
public class OperationLogPageRequest {

    @Size(max = 36, message = "操作人ID不能超过36个字符")
    private String userId;

    @Size(max = 64, message = "操作人用户名不能超过64个字符")
    private String username;

    @Size(max = 128, message = "模块名不能超过128个字符")
    private String moduleName;

    @Size(max = 32, message = "操作类型不能超过32个字符")
    private String operationType;

    @Min(value = 0, message = "结果只能为0或1")
    @Max(value = 1, message = "结果只能为0或1")
    private Integer success;

    @Size(max = 64, message = "traceId不能超过64个字符")
    private String traceId;

    private Long startTime;

    private Long endTime;
}
