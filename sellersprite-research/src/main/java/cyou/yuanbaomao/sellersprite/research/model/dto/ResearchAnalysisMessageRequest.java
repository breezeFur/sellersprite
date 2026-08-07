package cyou.yuanbaomao.sellersprite.research.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "市场调研后续分析问题")
public class ResearchAnalysisMessageRequest {

    @NotBlank
    @Size(max = 4_000)
    @Schema(description = "基于当前任务证据继续追问的内容")
    private String content;
}
