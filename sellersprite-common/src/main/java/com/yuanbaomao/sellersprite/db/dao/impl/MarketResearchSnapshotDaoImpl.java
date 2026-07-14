package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchSnapshotDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.db.mapper.MarketResearchSnapshotMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchSnapshotDaoImpl
        extends ServiceImpl<MarketResearchSnapshotMapper, MarketResearchSnapshot>
        implements MarketResearchSnapshotDao {

    @Override
    public Optional<MarketResearchSnapshot> findByIdempotencyKey(
            String jobId, String phase, String operation, String businessKey) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchSnapshot::getJobId, jobId)
                .eq(MarketResearchSnapshot::getPhase, phase)
                .eq(MarketResearchSnapshot::getOperation, operation)
                .eq(MarketResearchSnapshot::getBusinessKey, businessKey)
                .one());
    }

    @Override
    public List<MarketResearchSnapshot> listByJobId(String jobId) {
        return lambdaQuery()
                .eq(MarketResearchSnapshot::getJobId, jobId)
                .orderByAsc(MarketResearchSnapshot::getCreatedAt)
                .orderByAsc(MarketResearchSnapshot::getSnapshotId)
                .list();
    }
}
