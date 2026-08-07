package cyou.yuanbaomao.sellersprite.research.model;

/**
 * 市场调研数据源模式。
 */
public enum ResearchSourceMode {

    /** 使用版本化本地样例数据，不访问网络。 */
    MOCK,

    /** 通过现有 SellerSprite 强类型服务访问远端接口。 */
    REMOTE
}
