package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import java.util.List;
import java.util.Optional;

public interface MarketResearchDatasetDao extends IService<MarketResearchDataset> {

    Optional<MarketResearchDataset> findByIdempotencyKey(
            String jobId, String nodeCode, String operation, String datasetCode, String requestHash);

    /** 查询解析数据集响应所需字段，不加载请求和标准化JSON。 */
    Optional<MarketResearchDataset> findPayloadByJobIdAndDatasetCode(
            String jobId, String datasetCode);

    List<MarketResearchDataset> listByJobId(String jobId);

    /** 查询数据集目录所需字段，不加载请求和响应JSON。 */
    List<MarketResearchDataset> listMetadataByJobIdAndDatasetCodes(
            String jobId, List<String> datasetCodes);

    List<MarketResearchDataset> listByJobIdAndNodeCodesAndDatasetCodes(
            String jobId, List<String> nodeCodes, List<String> datasetCodes);
}
