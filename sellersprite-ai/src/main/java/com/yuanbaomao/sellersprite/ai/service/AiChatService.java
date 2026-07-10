package com.yuanbaomao.sellersprite.ai.service;

import com.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import com.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;

public interface AiChatService {

    AiChatVo chat(AiChatRequest request);
}
