package com.yuanbaomao.sellersprite.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.ai.advisor.MyLoggerAdvisor;
import com.yuanbaomao.sellersprite.ai.config.AiProperties;
import com.yuanbaomao.sellersprite.ai.context.AiCurrentUser;
import com.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import com.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import com.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import com.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import com.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import com.yuanbaomao.sellersprite.db.entity.AiConversation;
import com.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    private static final String USER_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd808d";
    private static final String CONVERSATION_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd808e";
    private static final String PROMPT_RECORD_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd808f";

    @Mock
    private ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private AiConversationDao conversationDao;

    @Mock
    private AiConversationMessageDao conversationMessageDao;

    @Mock
    private AiPromptRecordService promptRecordService;

    @Mock
    private MessageChatMemoryAdvisor memoryAdvisor;

    @Mock
    private MyLoggerAdvisor loggerAdvisor;

    @Mock
    private AiCurrentUser currentUser;

    private AiChatServiceImpl chatService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        AiProperties properties = new AiProperties();
        chatService = new AiChatServiceImpl(chatClientBuilderProvider, properties, conversationDao,
                conversationMessageDao, promptRecordService, memoryAdvisor, loggerAdvisor, currentUser);
        when(currentUser.requireUserId()).thenReturn(USER_ID);
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.clone()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(any(Advisor[].class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.chatResponse()).thenReturn(
                new ChatResponse(List.of(new Generation(new AssistantMessage("模型回复")))));
        when(promptRecordService.create(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(PROMPT_RECORD_ID);
        when(conversationDao.save(any(AiConversation.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, AiConversation.class).setConversationId(CONVERSATION_ID);
            return true;
        });
        AtomicInteger messageIndex = new AtomicInteger();
        when(conversationMessageDao.save(any(AiConversationMessage.class))).thenAnswer(invocation -> {
            AiConversationMessage message = invocation.getArgument(0, AiConversationMessage.class);
            message.setMessageId("message-" + messageIndex.incrementAndGet());
            return true;
        });
    }

    @Test
    void shouldCreateConversationAndPersistBothVisibleMessages() {
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        AiChatVo result = chatService.chat(request);

        assertThat(result.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(result.getMessageId()).isEqualTo("message-2");
        assertThat(result.getContent()).isEqualTo("模型回复");
        verify(conversationDao).save(any(AiConversation.class));
        verify(conversationMessageDao, org.mockito.Mockito.times(2)).save(any(AiConversationMessage.class));
        verify(promptRecordService).create(CONVERSATION_ID, USER_ID, "openai", "gpt-5.5");
    }
}
