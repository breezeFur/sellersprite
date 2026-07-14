package com.yuanbaomao.sellersprite.ai.service;

import com.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import com.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import com.yuanbaomao.sellersprite.ai.model.vo.AiStreamEvent;
import reactor.core.publisher.Flux;

public interface AiChatService {

    AiChatVo chat(AiChatRequest request);

    Flux<AiStreamEvent> stream(AiChatRequest request);

    Flux<AiStreamEvent> retry(String conversationId, String messageId);
}
