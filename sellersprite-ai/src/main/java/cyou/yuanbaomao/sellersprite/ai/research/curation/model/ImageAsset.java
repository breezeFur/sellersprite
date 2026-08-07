package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

import lombok.Data;

@Data
public class ImageAsset {

    private String sheetName;

    private int rowIndex;

    private int columnIndex;

    private String cellAddress;

    private ImageAssetSourceType sourceType;

    private String mimeType;

    private String reference;

    private String formulaId;

    private String sha256;

    private int sizeBytes;
}
