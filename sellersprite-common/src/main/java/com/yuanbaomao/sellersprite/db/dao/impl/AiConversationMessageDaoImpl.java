package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import com.yuanbaomao.sellersprite.db.mapper.AiConversationMessageMapper;
import java.util.List;
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
    public boolean deleteByConversationId(String userId, String conversationId) {
        return remove(Wrappers.<AiConversationMessage>lambdaQuery()
                .eq(AiConversationMessage::getUserId, userId)
                .eq(AiConversationMessage::getConversationId, conversationId));
    }
}
