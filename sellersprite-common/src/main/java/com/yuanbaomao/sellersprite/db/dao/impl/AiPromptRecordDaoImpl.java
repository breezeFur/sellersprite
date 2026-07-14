package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import com.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import com.yuanbaomao.sellersprite.db.mapper.AiPromptRecordMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AiPromptRecordDaoImpl extends ServiceImpl<AiPromptRecordMapper, AiPromptRecord>
        implements AiPromptRecordDao {

    @Override
    public long countByCreatedAtRange(long startTime, long endTime) {
        return lambdaQuery().ge(AiPromptRecord::getCreatedAt, startTime)
                .lt(AiPromptRecord::getCreatedAt, endTime).count();
    }

    @Override
    public Page<AiPromptRecord> page(String userId, String conversationId, String provider, String model,
                                     String status, Long startTime, Long endTime, long current, long size) {
        return lambdaQuery()
                .eq(userId != null && !userId.isBlank(), AiPromptRecord::getUserId, userId)
                .eq(conversationId != null && !conversationId.isBlank(),
                        AiPromptRecord::getConversationId, conversationId)
                .eq(provider != null && !provider.isBlank(), AiPromptRecord::getProvider, provider)
                .eq(model != null && !model.isBlank(), AiPromptRecord::getModel, model)
                .eq(status != null && !status.isBlank(), AiPromptRecord::getStatus, status)
                .ge(startTime != null, AiPromptRecord::getCreatedAt, startTime)
                .le(endTime != null, AiPromptRecord::getCreatedAt, endTime)
                .orderByDesc(AiPromptRecord::getCreatedAt)
                .page(Page.of(current, size));
    }

    @Override
    public Optional<AiPromptRecord> findById(String promptRecordId) {
        return Optional.ofNullable(getById(promptRecordId));
    }
}
