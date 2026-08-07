package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

import lombok.Data;

@Data
public class RawCell {

    private int rowIndex;

    private int columnIndex;

    private String cellAddress;

    private String value;

    private String formula;
}
