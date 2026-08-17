package cyou.yuanbaomao.sellersprite.system.ops.ai.service;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;

public interface AiPromptLogService {

    YPage<AiPromptLogVo> page(YPage<AiPromptLogVo> page, AiPromptLogPageRequest request);

    AiPromptLogVo detail(String promptRecordId);
}
