package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import com.yuanbaomao.sellersprite.db.mapper.MarketResearchArtifactMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchArtifactDaoImpl
        extends ServiceImpl<MarketResearchArtifactMapper, MarketResearchArtifact>
        implements MarketResearchArtifactDao {

    /** 已完成完整性校验、允许下载的产物状态。 */
    private static final String AVAILABLE_ARTIFACT_STATUS = "PUBLISHED";

    @Override
    public Optional<MarketResearchArtifact> findAvailableByJobId(String jobId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchArtifact::getJobId, jobId)
                .eq(MarketResearchArtifact::getArtifactStatus, AVAILABLE_ARTIFACT_STATUS)
                .one());
    }
}
