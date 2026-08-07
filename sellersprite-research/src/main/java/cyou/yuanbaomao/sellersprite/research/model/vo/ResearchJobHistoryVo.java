package cyou.yuanbaomao.sellersprite.research.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "我的市场调研历史报告")
public class ResearchJobHistoryVo {

    private String jobId;
    private String reportName;
    private String marketplace;
    private String nodeIdPath;
    private String month;
    private String keyword;
    private String status;
    private String currentStage;
    private String waitingInputType;
    private Integer progress;
    private String analysisRunId;
    private String analysisStatus;
    private String analysisPhase;
    private Integer analysisProgress;
    private Long createdAt;
    private Long finishedAt;
    private List<ResearchArtifactVo> artifacts;
}
