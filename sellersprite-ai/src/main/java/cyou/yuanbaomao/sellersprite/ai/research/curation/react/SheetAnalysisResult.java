package cyou.yuanbaomao.sellersprite.ai.research.curation.react;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SheetAnalysisResult {

    private String sheetName;

    private Integer sheetIndex;

    private Integer rowCount;

    @Builder.Default
    private List<String> headers = new ArrayList<>();

    private Integer rawCellCount;

    private Integer imageAssetCount;

    private String observation;

    private String action;

    private String summary;
}
