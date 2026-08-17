package cyou.yuanbaomao.sellersprite.system.permission.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统接口分页查询请求")
public class SysApiPageRequest {

    private String keyword;
    private String apiType;
    private String httpMethod;
    private String moduleName;
    private Integer status;
}
