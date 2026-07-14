package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import java.util.Optional;

public interface MarketResearchArtifactDao extends IService<MarketResearchArtifact> {

    Optional<MarketResearchArtifact> findAvailableByJobId(String jobId);
}
