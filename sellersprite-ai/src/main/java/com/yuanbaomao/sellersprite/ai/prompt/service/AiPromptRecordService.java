package com.yuanbaomao.sellersprite.ai.prompt.service;

import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;

public interface AiPromptRecordService {

    String create(String conversationId, String userId, String provider, String model);

    void recordRequest(String promptRecordId, List<Message> messages);

    void recordSuccess(String promptRecordId, ChatResponse chatResponse, long costMs);

    void recordFailure(String promptRecordId, Throwable throwable, long costMs);

    void recordStreamFailure(String promptRecordId, String partialContent, Throwable throwable, long costMs);

    void recordCancelled(String promptRecordId, String partialContent, long costMs);
}
