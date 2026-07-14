package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import com.yuanbaomao.sellersprite.db.mapper.AiConversationMessageMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AiConversationMessageDaoImpl extends ServiceImpl<AiConversationMessageMapper, AiConversationMessage>
        implements AiConversationMessageDao {

    @Override
    public List<AiConversationMessage> listByConversationId(String userId, String conversationId) {
        return lambdaQuery()
                .eq(AiConversationMessage::getUserId, userId)
                .eq(AiConversationMessage::getConversationId, conversationId)
                .orderByAsc(AiConversationMessage::getSequenceNo)
                .orderByAsc(AiConversationMessage::getCreatedAt)
                .list();
    }

    @Override
    public Optional<AiConversationMessage> findByIdAndUserId(String messageId, String userId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(AiConversationMessage::getMessageId, messageId)
                .eq(AiConversationMessage::getUserId, userId)
                .one());
    }

    @Override
    public Optional<AiConversationMessage> findLastByConversationId(String userId, String conversationId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(AiConversationMessage::getUserId, userId)
                .eq(AiConversationMessage::getConversationId, conversationId)
                .orderByDesc(AiConversationMessage::getSequenceNo)
                .orderByDesc(AiConversationMessage::getCreatedAt)
                .last("LIMIT 1")
                .one());
    }

    @Override
    public Optional<AiConversationMessage> findByPromptRecordId(String userId, String conversationId,
                                                                 String promptRecordId, String role) {
        return Optional.ofNullable(lambdaQuery()
                .eq(AiConversationMessage::getUserId, userId)
                .eq(AiConversationMessage::getConversationId, conversationId)
                .eq(AiConversationMessage::getPromptRecordId, promptRecordId)
                .eq(AiConversationMessage::getRole, role)
                .one());
    }

    @Override
    public boolean updateStatusIfMatches(AiConversationMessage message, String expectedStatus) {
        return lambdaUpdate()
                .eq(AiConversationMessage::getMessageId, message.getMessageId())
                .eq(AiConversationMessage::getUserId, message.getUserId())
                .eq(AiConversationMessage::getMessageStatus, expectedStatus)
                .set(AiConversationMessage::getContent, message.getContent())
                .set(AiConversationMessage::getMetadata, message.getMetadata())
                .set(AiConversationMessage::getMessageStatus, message.getMessageStatus())
                .set(AiConversationMessage::getErrorCode, message.getErrorCode())
                .set(AiConversationMessage::getErrorMessage, message.getErrorMessage())
                .update();
    }

    @Override
    public boolean deleteByConversationId(String userId, String conversationId) {
        return remove(Wrappers.<AiConversationMessage>lambdaQuery()
                .eq(AiConversationMessage::getUserId, userId)
                .eq(AiConversationMessage::getConversationId, conversationId));
    }
}
