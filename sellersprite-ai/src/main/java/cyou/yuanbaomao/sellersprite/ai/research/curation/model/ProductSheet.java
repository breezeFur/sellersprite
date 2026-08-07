package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ProductSheet {

    private String sheetName;

    private int sheetIndex;

    private List<String> headers = new ArrayList<>();

    private List<ProductSheetRow> rows = new ArrayList<>();

    private RawSheet rawSheet;
}
