package cyou.yuanbaomao.sellersprite.research.model.dto;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "阶段一商品选择关卡输入")
public class ResearchProductSelectionRequest {

    @NotNull
    private ResearchSelectionDecision decision;

    @Size(max = 20)
    private List<String> selectedAsins = List.of();
}
