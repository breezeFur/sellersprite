package cyou.yuanbaomao.sellersprite.ai.research.curation.react;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class AmazonSelectionReactResult {

    private String conversationId;

    private ProductWorkbook workbook;

    private List<SheetAnalysisResult> sheetAnalyses = new ArrayList<>();

    private String finalSummary;

    private boolean modelInvoked;

}
