package cyou.yuanbaomao.sellersprite.research.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * 一次远端操作或 Mock 操作产生的可持久化数据集。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResearchDataset {

    /** 供后续整理和 Excel 映射使用的稳定数据集编码。 */
    private String datasetCode;

    /** 对应 SellerSprite 业务操作的稳定名称。 */
    private String operation;

    /** 强类型响应序列化后的 JSON 树。 */
    private JsonNode payload;

    /** 当前数据集包含的业务记录数。 */
    private Integer recordCount;
}
