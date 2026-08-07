package cyou.yuanbaomao.sellersprite.research.model.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResearchEvidenceTableSummaryVo {

    private String datasetCode;
    private String sheetName;
    private String stageCode;
    private Integer rowCount;
    private List<String> columns;
}
