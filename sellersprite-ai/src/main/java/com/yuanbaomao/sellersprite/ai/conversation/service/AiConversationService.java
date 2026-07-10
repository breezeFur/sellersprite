package com.yuanbaomao.sellersprite.ai.conversation.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationPageRequest;
import com.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationRenameRequest;
import com.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import com.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;

public interface AiConversationService {

    PageResult<AiConversationVo> page(AiConversationPageRequest request);

    AiConversationDetailVo detail(String conversationId);

    AiConversationVo rename(String conversationId, AiConversationRenameRequest request);

    void delete(String conversationId);
}
