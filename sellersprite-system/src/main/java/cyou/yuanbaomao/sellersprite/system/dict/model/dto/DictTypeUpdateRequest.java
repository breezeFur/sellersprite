package cyou.yuanbaomao.sellersprite.system.dict.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "编辑字典类型请求")
public class DictTypeUpdateRequest {

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 128, message = "字典名称不能超过128个字符")
    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
