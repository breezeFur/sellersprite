package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import java.util.List;

public interface AiConversationMessageDao extends IService<AiConversationMessage> {

    List<AiConversationMessage> listByConversationId(String userId, String conversationId);

    boolean deleteByConversationId(String userId, String conversationId);
}
