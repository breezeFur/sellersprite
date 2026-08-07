package cyou.yuanbaomao.sellersprite.system.ops.ai.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;

public interface AiPromptLogService {

    PageResult<AiPromptLogVo> page(AiPromptLogPageRequest request);

    AiPromptLogVo detail(String promptRecordId);
}
