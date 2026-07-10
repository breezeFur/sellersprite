package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.AiConversation;
import java.util.Optional;

public interface AiConversationDao extends IService<AiConversation> {

    Optional<AiConversation> findByIdAndUserId(String conversationId, String userId);

    Page<AiConversation> pageByUserId(String userId, String title, long current, long size);

    boolean deleteByIdAndUserId(String conversationId, String userId);
}
