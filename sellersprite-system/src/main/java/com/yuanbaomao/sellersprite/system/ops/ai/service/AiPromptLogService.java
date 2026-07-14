package com.yuanbaomao.sellersprite.system.ops.ai.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;

public interface AiPromptLogService {

    PageResult<AiPromptLogVo> page(AiPromptLogPageRequest request);

    AiPromptLogVo detail(String promptRecordId);
}
