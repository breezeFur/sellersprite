package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import com.yuanbaomao.sellersprite.db.entity.AiConversation;
import com.yuanbaomao.sellersprite.db.mapper.AiConversationMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AiConversationDaoImpl extends ServiceImpl<AiConversationMapper, AiConversation>
        implements AiConversationDao {

    @Override
    public Optional<AiConversation> findByIdAndUserId(String conversationId, String userId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(AiConversation::getConversationId, conversationId)
                .eq(AiConversation::getUserId, userId)
                .one());
    }

    @Override
    public Page<AiConversation> pageByUserId(String userId, String title, long current, long size) {
        return lambdaQuery()
                .eq(AiConversation::getUserId, userId)
                .like(title != null && !title.isBlank(), AiConversation::getTitle, title)
                .orderByDesc(AiConversation::getLastMessageAt)
                .orderByDesc(AiConversation::getCreatedAt)
                .page(Page.of(current, size));
    }

    @Override
    public boolean deleteByIdAndUserId(String conversationId, String userId) {
        return remove(Wrappers.<AiConversation>lambdaQuery()
                .eq(AiConversation::getConversationId, conversationId)
                .eq(AiConversation::getUserId, userId));
    }

    @Override
    public long countByCreatedAtRange(long startTime, long endTime) {
        return lambdaQuery().ge(AiConversation::getCreatedAt, startTime)
                .lt(AiConversation::getCreatedAt, endTime).count();
    }
}
