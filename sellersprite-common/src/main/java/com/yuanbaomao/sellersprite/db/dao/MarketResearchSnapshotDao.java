package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import java.util.List;
import java.util.Optional;

public interface MarketResearchSnapshotDao extends IService<MarketResearchSnapshot> {

    Optional<MarketResearchSnapshot> findByIdempotencyKey(
            String jobId, String phase, String operation, String businessKey);

    List<MarketResearchSnapshot> listByJobId(String jobId);
}
