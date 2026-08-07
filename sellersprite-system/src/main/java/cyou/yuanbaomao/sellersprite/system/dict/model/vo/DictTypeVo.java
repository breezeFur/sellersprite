package cyou.yuanbaomao.sellersprite.system.dict.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "字典类型响应")
public class DictTypeVo {

    @Schema(description = "字典类型，稳定业务主键")
    private String dictType;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "是否系统内置：1是 0否")
    private Integer systemBuiltin;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    @Schema(description = "字典值列表")
    private List<DictItemVo> items = new ArrayList<>();
}
