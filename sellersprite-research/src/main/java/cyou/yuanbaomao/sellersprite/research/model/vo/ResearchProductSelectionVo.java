package cyou.yuanbaomao.sellersprite.research.model.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResearchProductSelectionVo {

    private String stageCode;
    private String status;
    private List<ResearchProductCandidateVo> candidates;
    private List<String> selectedAsins;
    private Long submittedAt;
}
