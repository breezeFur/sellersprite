package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchDatasetDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchDatasetMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchDatasetDaoImpl
        extends ServiceImpl<MarketResearchDatasetMapper, MarketResearchDataset>
        implements MarketResearchDatasetDao {

    @Override
    public Optional<MarketResearchDataset> findByIdempotencyKey(
            String jobId,
            String nodeCode,
            String operation,
            String datasetCode,
            String requestHash) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchDataset::getJobId, jobId)
                .eq(MarketResearchDataset::getNodeCode, nodeCode)
                .eq(MarketResearchDataset::getOperation, operation)
                .eq(MarketResearchDataset::getDatasetCode, datasetCode)
                .eq(MarketResearchDataset::getRequestHash, requestHash)
                .one());
    }

    @Override
    public List<MarketResearchDataset> listByJobId(String jobId) {
        return lambdaQuery()
                .eq(MarketResearchDataset::getJobId, jobId)
                .orderByAsc(MarketResearchDataset::getCreatedAt)
                .orderByAsc(MarketResearchDataset::getDatasetId)
                .list();
    }

    @Override
    public List<MarketResearchDataset> listByJobIdAndNodeCodesAndDatasetCodes(
            String jobId, List<String> nodeCodes, List<String> datasetCodes) {
        return lambdaQuery()
                .eq(MarketResearchDataset::getJobId, jobId)
                .in(MarketResearchDataset::getNodeCode, nodeCodes)
                .in(MarketResearchDataset::getDatasetCode, datasetCodes)
                .orderByAsc(MarketResearchDataset::getCreatedAt)
                .orderByAsc(MarketResearchDataset::getDatasetId)
                .list();
    }
}
