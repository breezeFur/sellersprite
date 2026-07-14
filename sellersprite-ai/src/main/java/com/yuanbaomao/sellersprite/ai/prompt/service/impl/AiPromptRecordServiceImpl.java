package com.yuanbaomao.sellersprite.ai.prompt.service.impl;

import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.sellersprite.ai.prompt.enums.AiPromptStatus;
import com.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import com.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import com.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import com.yuanbaomao.sellersprite.framework.security.SensitiveDataMasker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AiPromptRecordServiceImpl implements AiPromptRecordService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 4000;
    private static final int MAX_PROMPT_SUMMARY_LENGTH = 2048;
    private static final int YES = 1;
    private static final int NO = 0;
    private static final String EMPTY_JSON_ARRAY = "[]";
    private static final String EMPTY_STRING = "";
    private static final String CANCELLED_FINISH_REASON = "cancelled";

    private final AiPromptRecordDao promptRecordDao;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(String conversationId, String userId, String provider, String model) {
        AiPromptRecord record = new AiPromptRecord();
        record.setConversationId(conversationId);
        record.setUserId(userId);
        record.setProvider(provider);
        record.setModel(model);
        record.setRequestMessages(EMPTY_JSON_ARRAY);
        record.setPromptSummary(EMPTY_STRING);
        record.setPromptTruncated(NO);
        record.setResponseContent(EMPTY_STRING);
        record.setStatus(AiPromptStatus.PROCESSING.name());
        record.setErrorType(EMPTY_STRING);
        record.setErrorMessage(EMPTY_STRING);
        record.setCostMs(0L);
        record.setTrackId(defaultString(RequestContextHolder.currentTrackId()));
        record.setRemark(EMPTY_STRING);
        promptRecordDao.save(record);
        return record.getPromptRecordId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRequest(String promptRecordId, List<Message> messages) {
        String requestMessages = SensitiveDataMasker.mask(writeJson(toMessagePayloads(messages)));
        SensitiveDataMasker.MaskedText summary = SensitiveDataMasker.maskAndTruncate(
                requestMessages, MAX_PROMPT_SUMMARY_LENGTH);
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(promptRecordId);
        record.setRequestMessages(requestMessages);
        record.setPromptSummary(summary.content());
        record.setPromptTruncated(summary.truncated() ? YES : NO);
        promptRecordDao.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordSuccess(String promptRecordId, ChatResponse chatResponse, long costMs) {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(promptRecordId);
        record.setStatus(AiPromptStatus.SUCCESS.name());
        record.setCostMs(Math.max(costMs, 0L));
        record.setErrorType(EMPTY_STRING);
        record.setErrorMessage(EMPTY_STRING);
        if (chatResponse != null) {
            applyResponse(record, chatResponse);
        }
        promptRecordDao.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordFailure(String promptRecordId, Throwable throwable, long costMs) {
        recordFailure(promptRecordId, null, throwable, costMs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordStreamFailure(String promptRecordId, String partialContent,
                                    Throwable throwable, long costMs) {
        recordFailure(promptRecordId, partialContent, throwable, costMs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordCancelled(String promptRecordId, String partialContent, long costMs) {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(promptRecordId);
        record.setStatus(AiPromptStatus.CANCELLED.name());
        record.setResponseContent(SensitiveDataMasker.mask(partialContent));
        record.setFinishReason(CANCELLED_FINISH_REASON);
        record.setCostMs(Math.max(costMs, 0L));
        record.setErrorType(EMPTY_STRING);
        record.setErrorMessage(EMPTY_STRING);
        promptRecordDao.updateById(record);
    }

    private void recordFailure(String promptRecordId, String partialContent,
                               Throwable throwable, long costMs) {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(promptRecordId);
        record.setStatus(AiPromptStatus.FAILED.name());
        if (partialContent != null) {
            record.setResponseContent(SensitiveDataMasker.mask(partialContent));
        }
        record.setCostMs(Math.max(costMs, 0L));
        record.setErrorType(throwable == null ? EMPTY_STRING : throwable.getClass().getName());
        record.setErrorMessage(throwable == null ? EMPTY_STRING
                : SensitiveDataMasker.mask(limit(throwable.getMessage(), MAX_ERROR_MESSAGE_LENGTH)));
        promptRecordDao.updateById(record);
    }

    private void applyResponse(AiPromptRecord record, ChatResponse chatResponse) {
        Generation generation = chatResponse.getResult();
        if (generation != null && generation.getOutput() != null) {
            record.setResponseContent(SensitiveDataMasker.mask(generation.getOutput().getText()));
        }
        ChatGenerationMetadata generationMetadata = generation == null ? null : generation.getMetadata();
        if (generationMetadata != null) {
            record.setFinishReason(defaultString(generationMetadata.getFinishReason()));
        }
        ChatResponseMetadata responseMetadata = chatResponse.getMetadata();
        if (responseMetadata != null) {
            Usage usage = responseMetadata.getUsage();
            if (usage != null) {
                record.setPromptTokens(usage.getPromptTokens());
                record.setCompletionTokens(usage.getCompletionTokens());
                record.setTotalTokens(usage.getTotalTokens());
            }
        }
        record.setResponseMetadata(SensitiveDataMasker.mask(writeJson(toResponseMetadata(chatResponse))));
    }

    private List<Map<String, Object>> toMessagePayloads(List<Message> messages) {
        List<Map<String, Object>> payloads = new ArrayList<>();
        if (messages == null) {
            return payloads;
        }
        for (Message message : messages) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("role", message.getMessageType().name());
            payload.put("content", defaultString(message.getText()));
            if (message.getMetadata() != null && !message.getMetadata().isEmpty()) {
                payload.put("metadata", message.getMetadata());
            }
            if (message instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCalls()) {
                payload.put("toolCalls", assistantMessage.getToolCalls());
            }
            if (message instanceof ToolResponseMessage toolResponseMessage) {
                payload.put("toolResponses", toolResponseMessage.getResponses());
            }
            payloads.add(payload);
        }
        return payloads;
    }

    private Map<String, Object> toResponseMetadata(ChatResponse chatResponse) {
        Map<String, Object> payload = new LinkedHashMap<>();
        ChatResponseMetadata responseMetadata = chatResponse.getMetadata();
        if (responseMetadata != null) {
            payload.put("id", responseMetadata.getId());
            payload.put("model", responseMetadata.getModel());
            payload.put("metadata", responseMetadata.entrySet().stream()
                    .collect(LinkedHashMap::new,
                            (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                            LinkedHashMap::putAll));
            Usage usage = responseMetadata.getUsage();
            if (usage != null) {
                Map<String, Object> usagePayload = new LinkedHashMap<>();
                usagePayload.put("promptTokens", usage.getPromptTokens());
                usagePayload.put("completionTokens", usage.getCompletionTokens());
                usagePayload.put("totalTokens", usage.getTotalTokens());
                usagePayload.put("cacheReadInputTokens", usage.getCacheReadInputTokens());
                usagePayload.put("cacheWriteInputTokens", usage.getCacheWriteInputTokens());
                usagePayload.put("nativeUsage", usage.getNativeUsage());
                payload.put("usage", usagePayload);
            }
        }
        Generation generation = chatResponse.getResult();
        if (generation != null) {
            ChatGenerationMetadata generationMetadata = generation.getMetadata();
            if (generationMetadata != null) {
                payload.put("finishReason", generationMetadata.getFinishReason());
                payload.put("contentFilters", generationMetadata.getContentFilters());
                payload.put("generationMetadata", generationMetadata.entrySet().stream()
                        .collect(LinkedHashMap::new,
                                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                                LinkedHashMap::putAll));
            }
            if (generation.getOutput() != null && generation.getOutput().hasToolCalls()) {
                payload.put("toolCalls", generation.getOutput().getToolCalls());
            }
        }
        return payload;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException("AI调用记录JSON序列化失败", exception);
        }
    }

    private String defaultString(String value) {
        return value == null ? EMPTY_STRING : value;
    }

    private String limit(String value, int maxLength) {
        String text = defaultString(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
