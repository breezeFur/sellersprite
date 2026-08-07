package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MarketResearchArtifactDao extends IService<MarketResearchArtifact> {

    List<MarketResearchArtifact> listAvailableByJobIds(Collection<String> jobIds);

    Optional<MarketResearchArtifact> findByJobIdAndType(String jobId, String artifactType);

    Optional<MarketResearchArtifact> findAvailableByJobIdAndType(
            String jobId, String artifactType);

    Optional<MarketResearchArtifact> findByAnalysisRunIdAndType(
            String analysisRunId, String artifactType);

    Optional<MarketResearchArtifact> findAvailableByAnalysisRunIdAndType(
            String analysisRunId, String artifactType);
}
