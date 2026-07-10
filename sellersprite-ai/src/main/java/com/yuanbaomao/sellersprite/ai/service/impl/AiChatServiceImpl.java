package com.yuanbaomao.sellersprite.ai.service.impl;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.ai.advisor.AiAdvisorContextKeys;
import com.yuanbaomao.sellersprite.ai.advisor.MyLoggerAdvisor;
import com.yuanbaomao.sellersprite.ai.config.AiProperties;
import com.yuanbaomao.sellersprite.ai.context.AiCurrentUser;
import com.yuanbaomao.sellersprite.ai.conversation.constants.AiConversationConstants;
import com.yuanbaomao.sellersprite.ai.conversation.enums.AiConversationStatus;
import com.yuanbaomao.sellersprite.ai.conversation.enums.AiMessageRole;
import com.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import com.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import com.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import com.yuanbaomao.sellersprite.ai.service.AiChatService;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import com.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import com.yuanbaomao.sellersprite.db.entity.AiConversation;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String AI_MODEL_NOT_CONFIGURED_MESSAGE = "AI 聊天模型未配置，请先启用 spring.ai.model.chat 并配置模型密钥";

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final AiProperties aiProperties;
    private final AiConversationDao conversationDao;
    private final AiConversationMessageDao conversationMessageDao;
    private final AiPromptRecordService promptRecordService;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final MyLoggerAdvisor loggerAdvisor;
    private final AiCurrentUser currentUser;

    @Override
    public AiChatVo chat(AiChatRequest request) {
        AiProperties.Chat chatProperties = aiProperties.getChat();
        if (!chatProperties.isEnabled()) {
            throw new BizException(ResultCode.AI_MODEL_DISABLED);
        }

        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder == null) {
            throw new BizException(ResultCode.AI_MODEL_NOT_CONFIGURED, AI_MODEL_NOT_CONFIGURED_MESSAGE);
        }

        String userId = currentUser.requireUserId();
        AiConversation conversation = resolveConversation(request, userId, chatProperties);
        String promptRecordId = promptRecordService.create(conversation.getConversationId(), userId,
                conversation.getProvider(), conversation.getModel());
        long requestStartedAt = System.currentTimeMillis();

        try {
            saveVisibleMessage(conversation, userId, promptRecordId, AiMessageRole.USER, request.getPrompt());
            ChatResponse chatResponse = chatClientBuilder.clone()
                    .defaultAdvisors(memoryAdvisor, loggerAdvisor)
                    .build()
                    .prompt()
                    .system(defaultIfBlank(conversation.getSystemPrompt(), chatProperties.getDefaultSystemPrompt()))
                    .user(request.getPrompt())
                    .advisors(advisorSpec -> advisorSpec
                            .param(ChatMemory.CONVERSATION_ID, conversation.getConversationId())
                            .param(AiAdvisorContextKeys.PROMPT_RECORD_ID, promptRecordId)
                            .param(AiAdvisorContextKeys.REQUEST_STARTED_AT, requestStartedAt))
                    .call()
                    .chatResponse();
            String content = responseContent(chatResponse);
            AiConversationMessage assistantMessage = saveVisibleMessage(conversation, userId, promptRecordId,
                    AiMessageRole.ASSISTANT, content);
            return toChatVo(conversation, assistantMessage, promptRecordId, chatResponse);
        } catch (RuntimeException exception) {
            promptRecordService.recordFailure(promptRecordId, exception,
                    Math.max(System.currentTimeMillis() - requestStartedAt, 0L));
            throw exception;
        }
    }

    private AiConversation resolveConversation(AiChatRequest request, String userId, AiProperties.Chat chatProperties) {
        if (request.getConversationId() != null && !request.getConversationId().isBlank()) {
            return conversationDao.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new BizException(ResultCode.AI_CONVERSATION_NOT_FOUND));
        }
        long now = System.currentTimeMillis();
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(defaultTitle(request.getPrompt()));
        conversation.setProvider(chatProperties.getProvider());
        conversation.setModel(chatProperties.getModel());
        conversation.setSystemPrompt(defaultIfBlank(request.getSystemPrompt(), chatProperties.getDefaultSystemPrompt()));
        conversation.setMessageCount(0);
        conversation.setLastMessageAt(now);
        conversation.setStatus(AiConversationStatus.ACTIVE.name());
        conversation.setRemark("");
        conversationDao.save(conversation);
        return conversation;
    }

    private AiConversationMessage saveVisibleMessage(AiConversation conversation, String userId,
                                                       String promptRecordId, AiMessageRole role, String content) {
        long now = System.currentTimeMillis();
        int sequenceNo = defaultMessageCount(conversation) + 1;
        AiConversationMessage message = new AiConversationMessage();
        message.setConversationId(conversation.getConversationId());
        message.setUserId(userId);
        message.setPromptRecordId(promptRecordId);
        message.setSequenceNo(sequenceNo);
        message.setRole(role.name());
        message.setContent(content);
        message.setContentType(AiConversationConstants.CONTENT_TYPE_TEXT);
        message.setMetadata(null);
        conversationMessageDao.save(message);
        conversation.setMessageCount(sequenceNo);
        conversation.setLastMessageAt(now);
        conversationDao.updateById(conversation);
        return message;
    }

    private AiChatVo toChatVo(AiConversation conversation, AiConversationMessage assistantMessage,
                              String promptRecordId, ChatResponse chatResponse) {
        AiChatVo vo = new AiChatVo();
        vo.setConversationId(conversation.getConversationId());
        vo.setMessageId(assistantMessage.getMessageId());
        vo.setPromptRecordId(promptRecordId);
        vo.setContent(assistantMessage.getContent());
        vo.setProvider(conversation.getProvider());
        vo.setModel(conversation.getModel());
        vo.setCreatedAt(assistantMessage.getCreatedAt() == null ? System.currentTimeMillis() : assistantMessage.getCreatedAt());
        ChatResponseMetadata metadata = chatResponse == null ? null : chatResponse.getMetadata();
        Usage usage = metadata == null ? null : metadata.getUsage();
        if (usage != null) {
            vo.setPromptTokens(usage.getPromptTokens());
            vo.setCompletionTokens(usage.getCompletionTokens());
            vo.setTotalTokens(usage.getTotalTokens());
        }
        Generation generation = chatResponse == null ? null : chatResponse.getResult();
        ChatGenerationMetadata generationMetadata = generation == null ? null : generation.getMetadata();
        if (generationMetadata != null) {
            vo.setFinishReason(generationMetadata.getFinishReason());
        }
        return vo;
    }

    private String responseContent(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResult() == null
                || chatResponse.getResult().getOutput() == null
                || chatResponse.getResult().getOutput().getText() == null
                || chatResponse.getResult().getOutput().getText().isBlank()) {
            throw new BizException(ResultCode.MODEL_RESPONSE_EMPTY);
        }
        return chatResponse.getResult().getOutput().getText();
    }

    private int defaultMessageCount(AiConversation conversation) {
        return conversation.getMessageCount() == null ? 0 : conversation.getMessageCount();
    }

    private String defaultTitle(String prompt) {
        String normalized = defaultString(prompt).replaceAll("\\s+", " ").trim();
        if (normalized.length() <= AiConversationConstants.DEFAULT_TITLE_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, AiConversationConstants.DEFAULT_TITLE_LENGTH);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
