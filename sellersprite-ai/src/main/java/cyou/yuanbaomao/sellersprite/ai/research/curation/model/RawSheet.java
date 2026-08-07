package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RawSheet {

    private String sheetName;

    private int sheetIndex;

    private int rowCount;

    private int columnCount;

    private List<RawCell> rawCells = new ArrayList<>();

    private List<ImageAsset> imageAssets = new ArrayList<>();

    private String rawMarkdown = "";
}
