package cyou.yuanbaomao.sellersprite.ai.research.curation.model;

/**
 * Excel 图片资产来源类型。
 */
public enum ImageAssetSourceType {

    /**
     * 单元格文本中的公网图片链接。
     */
    URL,

    /**
     * 单元格公式中的图片占位，例如 WPS DISPIMG。
     */
    FORMULA,

    /**
     * Excel 文件中真正嵌入的图片对象。
     */
    EMBEDDED
}
