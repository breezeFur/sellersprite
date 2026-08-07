package cyou.yuanbaomao.sellersprite.ai.conversation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.ai.context.AiCurrentUser;
import cyou.yuanbaomao.sellersprite.ai.conversation.convert.AiConversationConverter;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationPageRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationRenameRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationSettingsRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationMessageVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationSettingsVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.service.AiConversationService;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationDao conversationDao;
    private final AiConversationMessageDao conversationMessageDao;
    private final ChatMemory chatMemory;
    private final AiCurrentUser currentUser;

    @Override
    public PageResult<AiConversationVo> page(AiConversationPageRequest request) {
        Page<AiConversation> page = conversationDao.pageByUserId(currentUser.requireUserId(), request.getTitle(),
                request.getCurrent(), request.getSize());
        List<AiConversationVo> records = page.getRecords().stream()
                .map(AiConversationConverter::toConversationVo)
                .toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public AiConversationDetailVo detail(String conversationId) {
        String userId = currentUser.requireUserId();
        AiConversation conversation = requireOwnedConversation(conversationId, userId);
        List<AiConversationMessageVo> messages = conversationMessageDao
                .listByConversationId(userId, conversationId)
                .stream()
                .map(AiConversationConverter::toMessageVo)
                .toList();
        AiConversationDetailVo detail = new AiConversationDetailVo();
        detail.setConversation(AiConversationConverter.toConversationVo(conversation));
        detail.setMessages(messages);
        detail.setSettings(AiConversationConverter.toSettingsVo(conversation));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConversationVo rename(String conversationId, AiConversationRenameRequest request) {
        String userId = currentUser.requireUserId();
        AiConversation conversation = requireOwnedConversation(conversationId, userId);
        conversation.setTitle(request.getTitle().trim());
        conversationDao.updateById(conversation);
        return AiConversationConverter.toConversationVo(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConversationSettingsVo updateSettings(String conversationId, AiConversationSettingsRequest request) {
        String userId = currentUser.requireUserId();
        AiConversation conversation = requireOwnedConversation(conversationId, userId);
        conversation.setSystemPrompt(normalizeSystemPrompt(request.getSystemPrompt()));
        conversationDao.updateById(conversation);
        return AiConversationConverter.toSettingsVo(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String conversationId) {
        String userId = currentUser.requireUserId();
        requireOwnedConversation(conversationId, userId);
        conversationMessageDao.deleteByConversationId(userId, conversationId);
        conversationDao.deleteByIdAndUserId(conversationId, userId);
        chatMemory.clear(conversationId);
    }

    private AiConversation requireOwnedConversation(String conversationId, String userId) {
        return conversationDao.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new BizException(ResultCode.AI_CONVERSATION_NOT_FOUND));
    }

    private String normalizeSystemPrompt(String systemPrompt) {
        return systemPrompt == null ? "" : systemPrompt.trim();
    }
}
