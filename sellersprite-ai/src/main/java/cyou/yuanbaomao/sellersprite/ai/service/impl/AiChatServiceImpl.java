package cyou.yuanbaomao.sellersprite.ai.service.impl;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.result.ErrorCode;
import cyou.yuanbaomao.sellersprite.ai.advisor.AiAdvisorContextKeys;
import cyou.yuanbaomao.sellersprite.ai.advisor.MyLoggerAdvisor;
import cyou.yuanbaomao.sellersprite.ai.constants.AiChatConstants;
import cyou.yuanbaomao.sellersprite.ai.context.AiCurrentUser;
import cyou.yuanbaomao.sellersprite.ai.conversation.constants.AiConversationConstants;
import cyou.yuanbaomao.sellersprite.ai.conversation.enums.AiConversationStatus;
import cyou.yuanbaomao.sellersprite.ai.conversation.enums.AiMessageRole;
import cyou.yuanbaomao.sellersprite.ai.conversation.enums.AiMessageStatus;
import cyou.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamConversationVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamDeltaVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamDoneVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamErrorVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamEvent;
import cyou.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import cyou.yuanbaomao.sellersprite.ai.service.AiChatService;
import cyou.yuanbaomao.sellersprite.ai.tool.SellerSpriteAiTools;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String AI_MODEL_NOT_CONFIGURED_MESSAGE = "AI 聊天模型未配置，请检查 spring.ai.openai 配置和模型密钥";
    private static final String STREAM_ERROR_MESSAGE = "AI 流式响应失败，请稍后重试";
    private static final String EVENT_CONVERSATION = "conversation";
    private static final String EVENT_DELTA = "delta";
    private static final String EVENT_DONE = "done";
    private static final String EVENT_ERROR = "error";
    private static final String EMPTY_STRING = "";
    private static final int MAX_SAFE_ERROR_MESSAGE_LENGTH = 512;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final String model;
    private final AiConversationDao conversationDao;
    private final AiConversationMessageDao conversationMessageDao;
    private final AiPromptRecordService promptRecordService;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final MyLoggerAdvisor loggerAdvisor;
    private final SellerSpriteAiTools sellerSpriteAiTools;
    private final AiCurrentUser currentUser;

    public AiChatServiceImpl(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                             @Value("${spring.ai.openai.chat.model}") String model,
                             AiConversationDao conversationDao,
                             AiConversationMessageDao conversationMessageDao,
                             AiPromptRecordService promptRecordService,
                             MessageChatMemoryAdvisor memoryAdvisor,
                             MyLoggerAdvisor loggerAdvisor,
                             SellerSpriteAiTools sellerSpriteAiTools,
                             AiCurrentUser currentUser) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.model = model;
        this.conversationDao = conversationDao;
        this.conversationMessageDao = conversationMessageDao;
        this.promptRecordService = promptRecordService;
        this.memoryAdvisor = memoryAdvisor;
        this.loggerAdvisor = loggerAdvisor;
        this.sellerSpriteAiTools = sellerSpriteAiTools;
        this.currentUser = currentUser;
    }

    @Override
    public AiChatVo chat(AiChatRequest request) {
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder == null) {
            throw new BizException(ResultCode.AI_MODEL_NOT_CONFIGURED, AI_MODEL_NOT_CONFIGURED_MESSAGE);
        }

        String userId = currentUser.requireUserId();
        AiConversation conversation = resolveConversation(request, userId);
        String promptRecordId = promptRecordService.create(conversation.getConversationId(), userId,
                conversation.getProvider(), conversation.getModel());
        long requestStartedAt = System.currentTimeMillis();

        try {
            saveVisibleMessage(conversation, userId, promptRecordId, AiMessageRole.USER, request.getPrompt());
            ChatResponse chatResponse = chatClientBuilder.clone()
                    .defaultAdvisors(memoryAdvisor, loggerAdvisor)
                    .build()
                    .prompt()
                    .system(defaultIfBlank(conversation.getSystemPrompt(), AiChatConstants.DEFAULT_SYSTEM_PROMPT))
                    .user(request.getPrompt())
                    .tools(sellerSpriteAiTools)
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

    @Override
    public Flux<AiStreamEvent> stream(AiChatRequest request) {
        return Flux.defer(() -> streamPrepared(request))
                .onErrorResume(exception -> Flux.just(errorEvent(exception)));
    }

    @Override
    public Flux<AiStreamEvent> retry(String conversationId, String messageId) {
        return Flux.defer(() -> retryPrepared(conversationId, messageId))
                .onErrorResume(exception -> Flux.just(errorEvent(exception)));
    }

    private Flux<AiStreamEvent> streamPrepared(AiChatRequest request) {
        ChatClient.Builder builder = requireChatClientBuilder();
        String userId = currentUser.requireUserId();
        AiConversation conversation = resolveConversation(request, userId);
        String promptRecordId = promptRecordService.create(conversation.getConversationId(), userId,
                conversation.getProvider(), conversation.getModel());
        AiConversationMessage userMessage = saveVisibleMessage(conversation, userId, promptRecordId,
                AiMessageRole.USER, request.getPrompt());
        return streamAttempt(builder, conversation, userId, promptRecordId,
                userMessage, request.getPrompt());
    }

    private Flux<AiStreamEvent> retryPrepared(String conversationId, String messageId) {
        ChatClient.Builder builder = requireChatClientBuilder();
        String userId = currentUser.requireUserId();
        AiConversation conversation = conversationDao.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new BizException(ResultCode.AI_CONVERSATION_NOT_FOUND));
        AiConversationMessage failedMessage = conversationMessageDao.findByIdAndUserId(messageId, userId)
                .orElseThrow(() -> new BizException(ResultCode.AI_MESSAGE_NOT_RETRYABLE));
        requireRetryableLastMessage(conversationId, userId, failedMessage);
        AiConversationMessage userMessage = conversationMessageDao.findByPromptRecordId(
                        userId, conversationId, failedMessage.getPromptRecordId(), AiMessageRole.USER.name())
                .orElseThrow(() -> new BizException(ResultCode.AI_MESSAGE_NOT_RETRYABLE));
        String promptRecordId = promptRecordService.create(conversationId, userId,
                conversation.getProvider(), conversation.getModel());
        return streamAttempt(builder, conversation, userId, promptRecordId,
                userMessage, userMessage.getContent());
    }

    private Flux<AiStreamEvent> streamAttempt(ChatClient.Builder builder, AiConversation conversation, String userId,
                                               String promptRecordId, AiConversationMessage userMessage,
                                               String prompt) {
        AiConversationMessage assistantMessage = saveStreamingAssistantMessage(
                conversation, userId, promptRecordId);
        long startedAt = System.currentTimeMillis();
        AtomicBoolean terminalWritten = new AtomicBoolean();
        AtomicReference<ChatResponse> lastResponse = new AtomicReference<>();
        StringBuilder content = new StringBuilder();
        Flux<AiStreamEvent> deltas = Flux.defer(() -> builder.clone()
                        .defaultAdvisors(memoryAdvisor, loggerAdvisor)
                        .build()
                        .prompt()
                        .system(defaultIfBlank(conversation.getSystemPrompt(), AiChatConstants.DEFAULT_SYSTEM_PROMPT))
                        .user(prompt)
                        .tools(sellerSpriteAiTools)
                        .advisors(advisorSpec -> advisorSpec
                                .param(ChatMemory.CONVERSATION_ID, conversation.getConversationId())
                                .param(AiAdvisorContextKeys.PROMPT_RECORD_ID, promptRecordId)
                                .param(AiAdvisorContextKeys.REQUEST_STARTED_AT, startedAt))
                        .stream()
                        .chatResponse())
                .handle((response, sink) -> {
                    lastResponse.set(response);
                    String delta = responseText(response);
                    if (!delta.isEmpty()) {
                        content.append(delta);
                        sink.next(new AiStreamEvent(EVENT_DELTA, new AiStreamDeltaVo(delta)));
                    }
                });
        Mono<AiStreamEvent> done = Mono.defer(() -> {
            if (content.isEmpty()) {
                return Mono.error(new BizException(ResultCode.MODEL_RESPONSE_EMPTY));
            }
            if (!terminalWritten.compareAndSet(false, true)) {
                return Mono.empty();
            }
            if (!completeAssistantMessage(assistantMessage, content.toString())) {
                return Mono.empty();
            }
            ChatResponse response = lastResponse.get();
            promptRecordService.recordSuccess(promptRecordId, response,
                    Math.max(System.currentTimeMillis() - startedAt, 0L));
            return Mono.just(new AiStreamEvent(EVENT_DONE, new AiStreamDoneVo(
                    toChatVo(conversation, assistantMessage, promptRecordId, response))));
        });
        Flux<AiStreamEvent> body = Flux.concat(deltas, done)
                .onErrorResume(exception -> {
                    if (terminalWritten.compareAndSet(false, true)
                            && failAssistantMessage(assistantMessage, content.toString(), exception)) {
                        promptRecordService.recordStreamFailure(promptRecordId, content.toString(), exception,
                                Math.max(System.currentTimeMillis() - startedAt, 0L));
                    }
                    return Flux.just(errorEvent(exception));
                });
        return Flux.concat(Flux.just(new AiStreamEvent(EVENT_CONVERSATION,
                        new AiStreamConversationVo(conversation.getConversationId(), userMessage.getMessageId()))), body)
                .doOnCancel(() -> {
                    if (terminalWritten.compareAndSet(false, true)
                            && cancelAssistantMessage(assistantMessage, content.toString())) {
                        promptRecordService.recordCancelled(promptRecordId, content.toString(),
                                Math.max(System.currentTimeMillis() - startedAt, 0L));
                    }
                });
    }

    private ChatClient.Builder requireChatClientBuilder() {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new BizException(ResultCode.AI_MODEL_NOT_CONFIGURED, AI_MODEL_NOT_CONFIGURED_MESSAGE);
        }
        return builder;
    }

    private void requireRetryableLastMessage(String conversationId, String userId,
                                             AiConversationMessage message) {
        boolean retryableStatus = AiMessageStatus.FAILED.name().equals(message.getMessageStatus())
                || AiMessageStatus.CANCELLED.name().equals(message.getMessageStatus());
        boolean assistantMessage = AiMessageRole.ASSISTANT.name().equals(message.getRole());
        boolean sameConversation = conversationId.equals(message.getConversationId());
        if (!retryableStatus || !assistantMessage || !sameConversation) {
            throw new BizException(ResultCode.AI_MESSAGE_NOT_RETRYABLE);
        }
        AiConversationMessage lastMessage = conversationMessageDao
                .findLastByConversationId(userId, conversationId)
                .orElseThrow(() -> new BizException(ResultCode.AI_MESSAGE_NOT_RETRYABLE));
        if (!message.getMessageId().equals(lastMessage.getMessageId())) {
            throw new BizException(ResultCode.AI_MESSAGE_NOT_RETRYABLE);
        }
    }

    private String responseText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null
                || response.getResult().getOutput().getText() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private AiStreamEvent errorEvent(Throwable throwable) {
        ErrorCode code = throwable instanceof BizException bizException
                ? bizException.getErrorCode() : ResultCode.INTERNAL_ERROR;
        String message = safeErrorMessage(throwable);
        return new AiStreamEvent(EVENT_ERROR, new AiStreamErrorVo(code.getCode(), message,
                RequestContextHolder.currentTrackId(), !(throwable instanceof BizException)));
    }

    private AiConversation resolveConversation(AiChatRequest request, String userId) {
        if (request.getConversationId() != null && !request.getConversationId().isBlank()) {
            return conversationDao.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new BizException(ResultCode.AI_CONVERSATION_NOT_FOUND));
        }
        long now = System.currentTimeMillis();
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(defaultTitle(request.getPrompt()));
        conversation.setProvider(AiChatConstants.PROVIDER_OPENAI);
        conversation.setModel(model);
        conversation.setSystemPrompt(defaultIfBlank(request.getSystemPrompt(), AiChatConstants.DEFAULT_SYSTEM_PROMPT));
        conversation.setMessageCount(0);
        conversation.setLastMessageAt(now);
        conversation.setStatus(AiConversationStatus.ACTIVE.name());
        conversation.setRemark("");
        conversationDao.save(conversation);
        return conversation;
    }

    private AiConversationMessage saveVisibleMessage(AiConversation conversation, String userId,
                                                       String promptRecordId, AiMessageRole role, String content) {
        return saveMessage(conversation, userId, promptRecordId, role, content,
                AiMessageStatus.COMPLETED, null);
    }

    private AiConversationMessage saveStreamingAssistantMessage(AiConversation conversation, String userId,
                                                                  String promptRecordId) {
        return saveMessage(conversation, userId, promptRecordId, AiMessageRole.ASSISTANT, EMPTY_STRING,
                AiMessageStatus.STREAMING, streamMetadata(AiMessageStatus.STREAMING));
    }

    private AiConversationMessage saveMessage(AiConversation conversation, String userId, String promptRecordId,
                                               AiMessageRole role, String content, AiMessageStatus status,
                                               String metadata) {
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
        message.setMetadata(metadata);
        message.setMessageStatus(status.name());
        message.setErrorCode(EMPTY_STRING);
        message.setErrorMessage(EMPTY_STRING);
        conversationMessageDao.save(message);
        conversation.setMessageCount(sequenceNo);
        conversation.setLastMessageAt(now);
        conversationDao.updateById(conversation);
        return message;
    }

    private boolean completeAssistantMessage(AiConversationMessage message, String content) {
        return updateAssistantMessage(message, AiMessageStatus.COMPLETED, content, null);
    }

    private boolean cancelAssistantMessage(AiConversationMessage message, String partialContent) {
        return updateAssistantMessage(message, AiMessageStatus.CANCELLED, partialContent, null);
    }

    private boolean failAssistantMessage(AiConversationMessage message, String partialContent, Throwable throwable) {
        return updateAssistantMessage(message, AiMessageStatus.FAILED, partialContent, throwable);
    }

    private boolean updateAssistantMessage(AiConversationMessage message, AiMessageStatus status,
                                           String content, Throwable throwable) {
        message.setContent(defaultString(content));
        message.setMetadata(streamMetadata(status));
        message.setMessageStatus(status.name());
        message.setErrorCode(throwable == null ? EMPTY_STRING : errorCode(throwable).getCode());
        message.setErrorMessage(throwable == null ? EMPTY_STRING : limitSafeMessage(safeErrorMessage(throwable)));
        return conversationMessageDao.updateStatusIfMatches(message, AiMessageStatus.STREAMING.name());
    }

    private String streamMetadata(AiMessageStatus status) {
        return "{\"streamStatus\":\"" + status.name() + "\"}";
    }

    private ErrorCode errorCode(Throwable throwable) {
        return throwable instanceof BizException bizException
                ? bizException.getErrorCode() : ResultCode.INTERNAL_ERROR;
    }

    private String safeErrorMessage(Throwable throwable) {
        return throwable instanceof BizException ? throwable.getMessage() : STREAM_ERROR_MESSAGE;
    }

    private String limitSafeMessage(String message) {
        String safeMessage = defaultString(message);
        return safeMessage.length() <= MAX_SAFE_ERROR_MESSAGE_LENGTH
                ? safeMessage : safeMessage.substring(0, MAX_SAFE_ERROR_MESSAGE_LENGTH);
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
