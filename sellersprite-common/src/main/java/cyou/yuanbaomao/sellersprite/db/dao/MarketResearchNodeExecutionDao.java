package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import java.util.List;

public interface MarketResearchNodeExecutionDao extends IService<MarketResearchNodeExecution> {

    int nextNodeAttempt(String jobId, String nodeCode, int jobAttempt);

    List<MarketResearchNodeExecution> listByJobId(String jobId);
}
