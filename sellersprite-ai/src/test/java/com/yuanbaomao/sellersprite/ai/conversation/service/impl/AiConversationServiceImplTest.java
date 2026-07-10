package com.yuanbaomao.sellersprite.ai.conversation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.ai.context.AiCurrentUser;
import com.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import com.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import com.yuanbaomao.sellersprite.db.entity.AiConversation;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;

@ExtendWith(MockitoExtension.class)
class AiConversationServiceImplTest {

    private static final String USER_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd808d";
    private static final String CONVERSATION_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd808e";

    @Mock
    private AiConversationDao conversationDao;

    @Mock
    private AiConversationMessageDao conversationMessageDao;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private AiCurrentUser currentUser;

    @InjectMocks
    private AiConversationServiceImpl conversationService;

    @Test
    void shouldReturnOwnedConversationWithCompleteMessages() {
        AiConversation conversation = conversation();
        AiConversationMessage userMessage = message("message-1", 1, "USER", "你好");
        AiConversationMessage assistantMessage = message("message-2", 2, "ASSISTANT", "你好，有什么可以帮你？");
        when(currentUser.requireUserId()).thenReturn(USER_ID);
        when(conversationDao.findByIdAndUserId(CONVERSATION_ID, USER_ID)).thenReturn(Optional.of(conversation));
        when(conversationMessageDao.listByConversationId(USER_ID, CONVERSATION_ID))
                .thenReturn(List.of(userMessage, assistantMessage));

        AiConversationDetailVo detail = conversationService.detail(CONVERSATION_ID);

        assertThat(detail.getConversation().getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(detail.getMessages()).extracting("role", "content")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("USER", "你好"),
                        org.assertj.core.groups.Tuple.tuple("ASSISTANT", "你好，有什么可以帮你？"));
    }

    @Test
    void shouldHideConversationOwnedByAnotherUser() {
        when(currentUser.requireUserId()).thenReturn(USER_ID);
        when(conversationDao.findByIdAndUserId(CONVERSATION_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.detail(CONVERSATION_ID))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo(ResultCode.AI_CONVERSATION_NOT_FOUND.getCode()));
    }

    @Test
    void shouldDeleteVisibleHistoryAndClearSpringChatMemory() {
        when(currentUser.requireUserId()).thenReturn(USER_ID);
        when(conversationDao.findByIdAndUserId(CONVERSATION_ID, USER_ID)).thenReturn(Optional.of(conversation()));

        conversationService.delete(CONVERSATION_ID);

        verify(conversationMessageDao).deleteByConversationId(USER_ID, CONVERSATION_ID);
        verify(conversationDao).deleteByIdAndUserId(CONVERSATION_ID, USER_ID);
        verify(chatMemory).clear(CONVERSATION_ID);
    }

    private AiConversation conversation() {
        AiConversation conversation = new AiConversation();
        conversation.setConversationId(CONVERSATION_ID);
        conversation.setUserId(USER_ID);
        conversation.setTitle("测试会话");
        conversation.setProvider("openai");
        conversation.setModel("gpt-5.5");
        conversation.setMessageCount(2);
        conversation.setLastMessageAt(1000L);
        conversation.setStatus("ACTIVE");
        conversation.setCreatedAt(900L);
        conversation.setUpdatedAt(1000L);
        return conversation;
    }

    private AiConversationMessage message(String messageId, int sequenceNo, String role, String content) {
        AiConversationMessage message = new AiConversationMessage();
        message.setMessageId(messageId);
        message.setConversationId(CONVERSATION_ID);
        message.setUserId(USER_ID);
        message.setSequenceNo(sequenceNo);
        message.setRole(role);
        message.setContent(content);
        message.setContentType("TEXT");
        message.setCreatedAt(1000L + sequenceNo);
        return message;
    }
}
