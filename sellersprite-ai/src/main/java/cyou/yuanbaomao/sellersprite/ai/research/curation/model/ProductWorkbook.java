package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ProductWorkbook {

    private String fileName;

    private List<ProductSheet> sheets = new ArrayList<>();

    private List<RawSheet> rawSheets = new ArrayList<>();
}
