package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
public class ProductSheetRow {

    private int rowIndex;

    private Map<String, String> cells = new LinkedHashMap<>();
}
