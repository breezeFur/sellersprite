package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchEventMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MarketResearchEventDaoImpl
        extends ServiceImpl<MarketResearchEventMapper, MarketResearchEvent>
        implements MarketResearchEventDao {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarketResearchEvent saveEvent(MarketResearchEvent event) {
        lockJobStream(event.getJobId());
        if (!save(event)) {
            throw new IllegalStateException("保存市场调研事件失败");
        }
        MarketResearchEvent persisted = getById(event.getEventId());
        if (persisted == null || persisted.getSequenceNo() == null) {
            throw new IllegalStateException("读取市场调研事件序号失败");
        }
        return persisted;
    }

    @Override
    public List<MarketResearchEvent> listByJobIdAfterSequence(
            String jobId, long afterSequence) {
        return replayQuery(jobId, afterSequence).list();
    }

    @Override
    public List<MarketResearchEvent> listByJobIdAfterSequence(
            String jobId, long afterSequence, int limit) {
        int safeLimit = MarketResearchEventDao.normalizeReplayLimit(limit);
        return replayQuery(jobId, afterSequence)
                .last("LIMIT " + safeLimit)
                .list();
    }

    @Override
    public long findLatestSequenceByJobId(String jobId) {
        MarketResearchEvent latest = lambdaQuery()
                .select(MarketResearchEvent::getSequenceNo)
                .eq(MarketResearchEvent::getJobId, jobId)
                .orderByDesc(MarketResearchEvent::getSequenceNo)
                .last("LIMIT 1")
                .one();
        return latest == null || latest.getSequenceNo() == null ? 0L : latest.getSequenceNo();
    }

    private void lockJobStream(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("市场调研事件jobId不能为空");
        }
        if (baseMapper.findEventStreamLock(jobId) == null) {
            try {
                baseMapper.insertEventStreamLock(jobId);
            } catch (DuplicateKeyException ignored) {
                // 并发首事件可能同时初始化锁行，随后统一获取行锁。
            }
        }
        if (!jobId.equals(baseMapper.lockEventStream(jobId))) {
            throw new IllegalStateException("获取市场调研事件流锁失败: " + jobId);
        }
    }

    private com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<
                    MarketResearchEvent>
            replayQuery(String jobId, long afterSequence) {
        return lambdaQuery()
                .eq(MarketResearchEvent::getJobId, jobId)
                .gt(MarketResearchEvent::getSequenceNo, afterSequence)
                .orderByAsc(MarketResearchEvent::getSequenceNo);
    }
}
