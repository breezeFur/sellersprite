package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchStageInput;
import java.util.Optional;

public interface MarketResearchStageInputDao extends IService<MarketResearchStageInput> {

    Optional<MarketResearchStageInput> find(
            String jobId, String stageCode, String inputType);
}
