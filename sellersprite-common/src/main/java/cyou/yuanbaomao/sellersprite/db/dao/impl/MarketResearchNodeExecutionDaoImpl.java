package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchNodeExecutionDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchNodeExecutionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchNodeExecutionDaoImpl
        extends ServiceImpl<MarketResearchNodeExecutionMapper, MarketResearchNodeExecution>
        implements MarketResearchNodeExecutionDao {

    @Override
    public int nextNodeAttempt(String jobId, String nodeCode, int jobAttempt) {
        long existingAttempts = lambdaQuery()
                .eq(MarketResearchNodeExecution::getJobId, jobId)
                .eq(MarketResearchNodeExecution::getNodeCode, nodeCode)
                .eq(MarketResearchNodeExecution::getJobAttempt, jobAttempt)
                .count();
        return Math.toIntExact(existingAttempts + 1);
    }

    @Override
    public List<MarketResearchNodeExecution> listByJobId(String jobId) {
        return lambdaQuery()
                .eq(MarketResearchNodeExecution::getJobId, jobId)
                .orderByAsc(MarketResearchNodeExecution::getStartedAt)
                .orderByAsc(MarketResearchNodeExecution::getJobAttempt)
                .orderByAsc(MarketResearchNodeExecution::getNodeAttempt)
                .orderByAsc(MarketResearchNodeExecution::getExecutionId)
                .list();
    }
}
