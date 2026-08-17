package cyou.yuanbaomao.sellersprite.ai.conversation.service;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationRenameRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationSettingsRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationSettingsVo;

public interface AiConversationService {

    YPage<AiConversationVo> page(YPage<AiConversationVo> page, String title);

    AiConversationDetailVo detail(String conversationId);

    AiConversationVo rename(String conversationId, AiConversationRenameRequest request);

    AiConversationSettingsVo updateSettings(String conversationId, AiConversationSettingsRequest request);

    void delete(String conversationId);
}
