package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchStageInputDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchStageInput;
import cyou.yuanbaomao.sellersprite.db.mapper.MarketResearchStageInputMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MarketResearchStageInputDaoImpl
        extends ServiceImpl<MarketResearchStageInputMapper, MarketResearchStageInput>
        implements MarketResearchStageInputDao {

    @Override
    public Optional<MarketResearchStageInput> find(
            String jobId, String stageCode, String inputType) {
        return Optional.ofNullable(lambdaQuery()
                .eq(MarketResearchStageInput::getJobId, jobId)
                .eq(MarketResearchStageInput::getStageCode, stageCode)
                .eq(MarketResearchStageInput::getInputType, inputType)
                .one());
    }
}
