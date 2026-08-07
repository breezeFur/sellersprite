package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "市场调研历史报告可下载产物")
public class ResearchArtifactVo {

    private String artifactId;
    private String analysisRunId;
    private String artifactType;
    private String fileName;
    private String mediaType;
    private Long fileSize;
    private Long createdAt;
}
