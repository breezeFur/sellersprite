package cyou.yuanbaomao.sellersprite.system.role.model.dto;

import cyou.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色分页查询请求")
public class RolePageRequest extends PageQuery {

    @Schema(description = "角色名称关键字")
    private String roleName;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
