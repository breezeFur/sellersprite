package cyou.yuanbaomao.sellersprite.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.ai.advisor.MyLoggerAdvisor;
import cyou.yuanbaomao.sellersprite.ai.context.AiCurrentUser;
import cyou.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamConversationVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamDeltaVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamDoneVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamErrorVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamEvent;
import cyou.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import cyou.yuanbaomao.sellersprite.ai.tool.SellerSpriteAiTools;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Flux;

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
    private ChatClient.StreamResponseSpec streamResponseSpec;

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
    private SellerSpriteAiTools sellerSpriteAiTools;

    @Mock
    private AiCurrentUser currentUser;

    private AiChatServiceImpl chatService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        chatService = new AiChatServiceImpl(chatClientBuilderProvider, "gpt-5.5", conversationDao,
                conversationMessageDao, promptRecordService, memoryAdvisor, loggerAdvisor,
                sellerSpriteAiTools, currentUser);
        when(currentUser.requireUserId()).thenReturn(USER_ID);
    }

    @SuppressWarnings("unchecked")
    private void stubChatInfrastructure() {
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.clone()).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(any(Advisor[].class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(promptRecordService.create(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(PROMPT_RECORD_ID);
        AtomicInteger messageIndex = new AtomicInteger();
        when(conversationMessageDao.save(any(AiConversationMessage.class))).thenAnswer(invocation -> {
            AiConversationMessage message = invocation.getArgument(0, AiConversationMessage.class);
            message.setMessageId("message-" + messageIndex.incrementAndGet());
            return true;
        });
    }

    private void stubNewConversationSave() {
        when(conversationDao.save(any(AiConversation.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, AiConversation.class).setConversationId(CONVERSATION_ID);
            return true;
        });
    }

    private void stubStreamingTransition() {
        when(conversationMessageDao.updateStatusIfMatches(any(AiConversationMessage.class), any(String.class)))
                .thenReturn(true);
    }

    @Test
    void shouldCreateConversationAndPersistBothVisibleMessages() {
        stubChatInfrastructure();
        stubNewConversationSave();
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.chatResponse()).thenReturn(
                new ChatResponse(List.of(new Generation(new AssistantMessage("模型回复")))));
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        AiChatVo result = chatService.chat(request);

        assertThat(result.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(result.getMessageId()).isEqualTo("message-2");
        assertThat(result.getContent()).isEqualTo("模型回复");
        verify(conversationDao).save(any(AiConversation.class));
        verify(conversationMessageDao, org.mockito.Mockito.times(2)).save(any(AiConversationMessage.class));
        verify(promptRecordService).create(CONVERSATION_ID, USER_ID, "openai", "gpt-5.5");
        verify(requestSpec).tools(sellerSpriteAiTools);
    }

    @Test
    void shouldEmitConversationDeltasAndSingleDoneInOrder() {
        stubChatInfrastructure();
        stubNewConversationSave();
        stubStreamingTransition();
        stubSuccessfulStream();
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        List<AiStreamEvent> events = chatService.stream(request).collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent)
                .containsExactly("conversation", "delta", "delta", "done");
        assertThat(events.stream().filter(event -> "delta".equals(event.getEvent()))
                .map(event -> (AiStreamDeltaVo) event.getData()))
                .extracting(AiStreamDeltaVo::getContent)
                .containsExactly("你", "好");
        AiStreamDoneVo done = (AiStreamDoneVo) events.getLast().getData();
        assertThat(done.getChat().getContent()).isEqualTo("你好");
        assertThat(events).filteredOn(event -> "done".equals(event.getEvent())).hasSize(1);
        verify(conversationMessageDao).updateStatusIfMatches(argThat(message ->
                "COMPLETED".equals(message.getMessageStatus()) && "你好".equals(message.getContent())),
                eq("STREAMING"));
        verify(requestSpec).tools(sellerSpriteAiTools);
    }

    @Test
    void shouldEmitSingleErrorAndNoDoneWhenModelStreamFails() {
        stubChatInfrastructure();
        stubNewConversationSave();
        stubStreamingTransition();
        when(requestSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.chatResponse()).thenReturn(Flux.error(new IllegalStateException("模型异常")));
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        List<AiStreamEvent> events = chatService.stream(request).collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent)
                .containsExactly("conversation", "error");
        assertThat(events).noneMatch(event -> "done".equals(event.getEvent()));
    }

    @Test
    void shouldPersistPartialTextAsFailedWhenStreamFails() {
        stubChatInfrastructure();
        stubNewConversationSave();
        stubStreamingTransition();
        when(requestSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.chatResponse()).thenReturn(Flux.concat(
                Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage("部分"))))),
                Flux.error(new IllegalStateException("模型异常"))));
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        List<AiStreamEvent> events = chatService.stream(request).collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent)
                .containsExactly("conversation", "delta", "error");
        verify(conversationMessageDao).updateStatusIfMatches(argThat(message ->
                "FAILED".equals(message.getMessageStatus()) && "部分".equals(message.getContent())),
                eq("STREAMING"));
        verify(promptRecordService).recordStreamFailure(
                org.mockito.ArgumentMatchers.eq(PROMPT_RECORD_ID),
                org.mockito.ArgumentMatchers.eq("部分"), any(Throwable.class), anyLong());
    }

    @Test
    void shouldPersistPartialTextAndCancelledMetadataWhenSubscriberStops() {
        stubChatInfrastructure();
        stubNewConversationSave();
        stubStreamingTransition();
        when(requestSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.chatResponse()).thenReturn(Flux.concat(
                Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage("部分"))))),
                Flux.never()));
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        List<AiStreamEvent> events = chatService.stream(request).take(2).collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent)
                .containsExactly("conversation", "delta");
        verify(conversationMessageDao).updateStatusIfMatches(argThat(message ->
                "CANCELLED".equals(message.getMessageStatus())
                        && "部分".equals(message.getContent())
                        && message.getMetadata().contains("CANCELLED")), eq("STREAMING"));
        verify(promptRecordService).recordCancelled(
                org.mockito.ArgumentMatchers.eq(PROMPT_RECORD_ID),
                org.mockito.ArgumentMatchers.eq("部分"), anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FAILED", "CANCELLED"})
    void shouldRetryLastFailedOrCancelledAssistantWithoutDuplicatingUserMessage(String status) {
        stubChatInfrastructure();
        stubStreamingTransition();
        stubSuccessfulStream();
        AiConversation conversation = existingConversation();
        AiConversationMessage failedAssistant = existingMessage(
                "failed-message", 2, "ASSISTANT", status, "old-prompt", "部分回复");
        AiConversationMessage originalUserMessage = existingMessage(
                "user-message", 1, "USER", "COMPLETED", "old-prompt", "原始问题");
        when(conversationDao.findByIdAndUserId(CONVERSATION_ID, USER_ID)).thenReturn(Optional.of(conversation));
        when(conversationMessageDao.findByIdAndUserId("failed-message", USER_ID))
                .thenReturn(Optional.of(failedAssistant));
        when(conversationMessageDao.findLastByConversationId(USER_ID, CONVERSATION_ID))
                .thenReturn(Optional.of(failedAssistant));
        when(conversationMessageDao.findByPromptRecordId(
                USER_ID, CONVERSATION_ID, "old-prompt", "USER"))
                .thenReturn(Optional.of(originalUserMessage));

        List<AiStreamEvent> events = chatService.retry(CONVERSATION_ID, "failed-message")
                .collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent)
                .containsExactly("conversation", "delta", "delta", "done");
        AiStreamConversationVo conversationEvent = (AiStreamConversationVo) events.getFirst().getData();
        assertThat(conversationEvent.getUserMessageId()).isEqualTo("user-message");
        verify(conversationMessageDao, times(1)).save(argThat(message -> "ASSISTANT".equals(message.getRole())));
        verify(conversationMessageDao, never()).save(argThat(message -> "USER".equals(message.getRole())));
        verify(requestSpec).tools(sellerSpriteAiTools);
    }

    @Test
    void shouldSkipDuplicateTerminalSideEffectsWhenConditionalUpdateLosesRace() {
        stubChatInfrastructure();
        stubNewConversationSave();
        when(conversationMessageDao.updateStatusIfMatches(
                any(AiConversationMessage.class), eq("STREAMING"))).thenReturn(false);
        stubSuccessfulStream();
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");

        List<AiStreamEvent> events = chatService.stream(request).collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent)
                .containsExactly("conversation", "delta", "delta");
        verify(promptRecordService, never()).recordSuccess(any(String.class), any(ChatResponse.class), anyLong());
        verify(promptRecordService, never()).recordStreamFailure(
                any(String.class), any(String.class), any(Throwable.class), anyLong());
        verify(promptRecordService, never()).recordCancelled(any(String.class), any(String.class), anyLong());
    }

    @Test
    void shouldRejectRetryWhenAssistantMessageIsCompleted() {
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        AiConversation conversation = existingConversation();
        AiConversationMessage completedAssistant = existingMessage(
                "completed-message", 2, "ASSISTANT", "COMPLETED", "old-prompt", "完整回复");
        when(conversationDao.findByIdAndUserId(CONVERSATION_ID, USER_ID)).thenReturn(Optional.of(conversation));
        when(conversationMessageDao.findByIdAndUserId("completed-message", USER_ID))
                .thenReturn(Optional.of(completedAssistant));

        List<AiStreamEvent> events = chatService.retry(CONVERSATION_ID, "completed-message")
                .collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent).containsExactly("error");
        AiStreamErrorVo error = (AiStreamErrorVo) events.getFirst().getData();
        assertThat(error.getCode()).isEqualTo("B409");
        verify(conversationMessageDao, never()).save(any(AiConversationMessage.class));
    }

    @Test
    void shouldRejectRetryWhenFailedAssistantIsNotLastMessage() {
        when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);
        AiConversation conversation = existingConversation();
        AiConversationMessage failedAssistant = existingMessage(
                "failed-message", 2, "ASSISTANT", "FAILED", "old-prompt", "部分回复");
        AiConversationMessage lastMessage = existingMessage(
                "later-message", 3, "ASSISTANT", "COMPLETED", "later-prompt", "后续回复");
        when(conversationDao.findByIdAndUserId(CONVERSATION_ID, USER_ID)).thenReturn(Optional.of(conversation));
        when(conversationMessageDao.findByIdAndUserId("failed-message", USER_ID))
                .thenReturn(Optional.of(failedAssistant));
        when(conversationMessageDao.findLastByConversationId(USER_ID, CONVERSATION_ID))
                .thenReturn(Optional.of(lastMessage));

        List<AiStreamEvent> events = chatService.retry(CONVERSATION_ID, "failed-message")
                .collectList().block();

        assertThat(events).extracting(AiStreamEvent::getEvent).containsExactly("error");
        AiStreamErrorVo error = (AiStreamErrorVo) events.getFirst().getData();
        assertThat(error.getCode()).isEqualTo("B409");
        verify(conversationMessageDao, never()).save(any(AiConversationMessage.class));
    }

    private void stubSuccessfulStream() {
        when(requestSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.chatResponse()).thenReturn(Flux.just(
                new ChatResponse(List.of(new Generation(new AssistantMessage("你")))),
                new ChatResponse(List.of(new Generation(new AssistantMessage("好"))))));
    }

    private AiConversation existingConversation() {
        AiConversation conversation = new AiConversation();
        conversation.setConversationId(CONVERSATION_ID);
        conversation.setUserId(USER_ID);
        conversation.setProvider("openai");
        conversation.setModel("gpt-5.5");
        conversation.setSystemPrompt("你是助手");
        conversation.setMessageCount(2);
        conversation.setStatus("ACTIVE");
        return conversation;
    }

    private AiConversationMessage existingMessage(String messageId, int sequenceNo, String role,
                                                   String status, String promptRecordId, String content) {
        AiConversationMessage message = new AiConversationMessage();
        message.setMessageId(messageId);
        message.setConversationId(CONVERSATION_ID);
        message.setUserId(USER_ID);
        message.setSequenceNo(sequenceNo);
        message.setRole(role);
        message.setMessageStatus(status);
        message.setPromptRecordId(promptRecordId);
        message.setContent(content);
        return message;
    }
}
