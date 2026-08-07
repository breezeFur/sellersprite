package cyou.yuanbaomao.sellersprite.research.model.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
@Builder
public class ResearchEvidencePageVo {

    private String datasetCode;
    private String sheetName;
    private String stageCode;
    private List<String> columns;
    private List<JsonNode> records;
    private Long current;
    private Long size;
    private Long total;
}
