package cyou.yuanbaomao.sellersprite.ai.conversation.convert;

import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationMessageVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationSettingsVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversationMessage;

public final class AiConversationConverter {

    private AiConversationConverter() {
    }

    public static AiConversationVo toConversationVo(AiConversation entity) {
        AiConversationVo vo = new AiConversationVo();
        vo.setConversationId(entity.getConversationId());
        vo.setTitle(entity.getTitle());
        vo.setProvider(entity.getProvider());
        vo.setModel(entity.getModel());
        vo.setMessageCount(entity.getMessageCount());
        vo.setLastMessageAt(entity.getLastMessageAt());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    public static AiConversationMessageVo toMessageVo(AiConversationMessage entity) {
        AiConversationMessageVo vo = new AiConversationMessageVo();
        vo.setMessageId(entity.getMessageId());
        vo.setPromptRecordId(entity.getPromptRecordId());
        vo.setSequenceNo(entity.getSequenceNo());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setContentType(entity.getContentType());
        vo.setMetadata(entity.getMetadata());
        vo.setMessageStatus(entity.getMessageStatus());
        vo.setErrorCode(entity.getErrorCode());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static AiConversationSettingsVo toSettingsVo(AiConversation entity) {
        AiConversationSettingsVo vo = new AiConversationSettingsVo();
        vo.setProvider(entity.getProvider());
        vo.setModel(entity.getModel());
        vo.setSystemPrompt(entity.getSystemPrompt());
        return vo;
    }
}
