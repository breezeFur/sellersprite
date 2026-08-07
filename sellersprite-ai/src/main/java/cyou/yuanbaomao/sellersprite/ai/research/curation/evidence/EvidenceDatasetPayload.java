package cyou.yuanbaomao.sellersprite.ai.research.curation.evidence;

import org.springframework.util.StringUtils;

/**
 * 持久化 evidence 数据集的最小只读快照，隔离 Agent 与数据库 DAO。
 */
public record EvidenceDatasetPayload(String datasetCode, String payloadJson) {

    public EvidenceDatasetPayload {
        if (!StringUtils.hasText(datasetCode)) {
            throw new IllegalArgumentException("datasetCode 不能为空");
        }
        if (!StringUtils.hasText(payloadJson)) {
            throw new IllegalArgumentException("payloadJson 不能为空");
        }
    }
}
