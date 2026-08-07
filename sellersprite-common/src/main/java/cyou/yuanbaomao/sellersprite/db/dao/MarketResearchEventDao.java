package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import java.util.List;

public interface MarketResearchEventDao extends IService<MarketResearchEvent> {

    int MIN_REPLAY_LIMIT = 1;
    int MAX_REPLAY_LIMIT = 5000;

    MarketResearchEvent saveEvent(MarketResearchEvent event);

    List<MarketResearchEvent> listByJobIdAfterSequence(String jobId, long afterSequence);

    List<MarketResearchEvent> listByJobIdAfterSequence(
            String jobId, long afterSequence, int limit);

    long findLatestSequenceByJobId(String jobId);

    static int normalizeReplayLimit(int limit) {
        return Math.max(MIN_REPLAY_LIMIT, Math.min(limit, MAX_REPLAY_LIMIT));
    }
}
