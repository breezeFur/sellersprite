package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import java.util.List;
import java.util.Optional;

public interface AiConversationMessageDao extends IService<AiConversationMessage> {

    List<AiConversationMessage> listByConversationId(String userId, String conversationId);

    Optional<AiConversationMessage> findByIdAndUserId(String messageId, String userId);

    Optional<AiConversationMessage> findLastByConversationId(String userId, String conversationId);

    Optional<AiConversationMessage> findByPromptRecordId(String userId, String conversationId,
                                                          String promptRecordId, String role);

    boolean updateStatusIfMatches(AiConversationMessage message, String expectedStatus);

    boolean deleteByConversationId(String userId, String conversationId);
}
