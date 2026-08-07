package cyou.yuanbaomao.sellersprite.system.permission.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "接口方法路径引用")
public class ApiEndpointRefRequest {

    @NotBlank
    @Schema(description = "HTTP 方法", example = "GET")
    private String httpMethod;

    @NotBlank
    @Schema(description = "规范化接口路径", example = "/api/users/{userId}")
    private String pathPattern;
}
