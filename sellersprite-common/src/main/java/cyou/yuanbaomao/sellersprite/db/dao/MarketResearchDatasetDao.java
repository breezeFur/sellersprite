package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import java.util.List;
import java.util.Optional;

public interface MarketResearchDatasetDao extends IService<MarketResearchDataset> {

    Optional<MarketResearchDataset> findByIdempotencyKey(
            String jobId, String nodeCode, String operation, String datasetCode, String requestHash);

    List<MarketResearchDataset> listByJobId(String jobId);

    List<MarketResearchDataset> listByJobIdAndNodeCodesAndDatasetCodes(
            String jobId, List<String> nodeCodes, List<String> datasetCodes);
}
