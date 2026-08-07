package cyou.yuanbaomao.sellersprite.system.ops.operation.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志")
public class OperationLogVo {

    private String operationLogId;
    private String userId;
    private String username;
    private String moduleName;
    private String operationName;
    private String operationType;
    private String httpMethod;
    private String requestUri;
    private String requestParams;
    private String responsePayload;
    private Integer responseStatus;
    private Integer success;
    private String errorMessage;
    private String clientIp;
    private String userAgent;
    private Long costMs;
    private String trackId;
    private Long createdAt;
}
