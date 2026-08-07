package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchArtifactMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchArtifactDaoImpl
        extends ServiceImpl<MarketResearchArtifactMapper, MarketResearchArtifact>
        implements MarketResearchArtifactDao {

    /** 已完成完整性校验、允许下载的产物状态。 */
    private static final String AVAILABLE_ARTIFACT_STATUS = "PUBLISHED";

    @Override
    public List<MarketResearchArtifact> listAvailableByJobIds(Collection<String> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .in(MarketResearchArtifact::getJobId, jobIds)
                .eq(MarketResearchArtifact::getArtifactStatus, AVAILABLE_ARTIFACT_STATUS)
                .orderByDesc(MarketResearchArtifact::getCreatedAt)
                .orderByDesc(MarketResearchArtifact::getArtifactId)
                .list();
    }

    @Override
    public Optional<MarketResearchArtifact> findByJobIdAndType(String jobId, String artifactType) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchArtifact::getJobId, jobId)
                .eq(MarketResearchArtifact::getArtifactType, artifactType)
                .isNull(MarketResearchArtifact::getAnalysisRunId)
                .one());
    }

    @Override
    public Optional<MarketResearchArtifact> findAvailableByJobIdAndType(
            String jobId, String artifactType) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchArtifact::getJobId, jobId)
                .eq(MarketResearchArtifact::getArtifactType, artifactType)
                .isNull(MarketResearchArtifact::getAnalysisRunId)
                .eq(MarketResearchArtifact::getArtifactStatus, AVAILABLE_ARTIFACT_STATUS)
                .one());
    }

    @Override
    public Optional<MarketResearchArtifact> findByAnalysisRunIdAndType(
            String analysisRunId, String artifactType) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchArtifact::getAnalysisRunId, analysisRunId)
                .eq(MarketResearchArtifact::getArtifactType, artifactType)
                .one());
    }

    @Override
    public Optional<MarketResearchArtifact> findAvailableByAnalysisRunIdAndType(
            String analysisRunId, String artifactType) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchArtifact::getAnalysisRunId, analysisRunId)
                .eq(MarketResearchArtifact::getArtifactType, artifactType)
                .eq(MarketResearchArtifact::getArtifactStatus, AVAILABLE_ARTIFACT_STATUS)
                .one());
    }
}
