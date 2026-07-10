package com.yuanbaomao.sellersprite.ai.advisor;

import com.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
public class MyLoggerAdvisor implements BaseAdvisor {

    private static final int MAX_MESSAGE_SUMMARY_LENGTH = 500;
    private static final int MAX_TOOL_RESULT_PREVIEW_LENGTH = 160;

    private final AiPromptRecordService promptRecordService;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        List<Message> instructions = request.prompt().getInstructions();
        String promptRecordId = contextString(request.context(), AiAdvisorContextKeys.PROMPT_RECORD_ID);
        if (!promptRecordId.isBlank()) {
            promptRecordService.recordRequest(promptRecordId, instructions);
        }
        if (!instructions.isEmpty()) {
            Message lastMessage = instructions.getLast();
            log.info(">>>>>> [发送请求] 历史消息数: {}, 当前消息: [{}] {}",
                    instructions.size(), lastMessage.getMessageType().name(), summarizeMessage(lastMessage));
        }
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        ChatResponse chatResponse = response == null ? null : response.chatResponse();
        if (response != null) {
            String promptRecordId = contextString(response.context(), AiAdvisorContextKeys.PROMPT_RECORD_ID);
            if (!promptRecordId.isBlank()) {
                promptRecordService.recordSuccess(promptRecordId, chatResponse, elapsed(response.context()));
            }
        }
        String text = extractText(response);
        if (text != null) {
            log.info("<<<<<< [模型回复结束] 完整回复 <<<<<<\n{}", text);
        }
        logToolCalls(response);
        logReasoning(response);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        ChatClientRequest processed = before(request, chain);
        StringBuilder fullText = new StringBuilder();
        AtomicReference<ChatClientResponse> lastResponse = new AtomicReference<>();
        String promptRecordId = contextString(processed.context(), AiAdvisorContextKeys.PROMPT_RECORD_ID);
        return chain.nextStream(processed)
                .doOnNext(response -> {
                    lastResponse.set(response);
                    String delta = extractText(response);
                    if (delta != null) {
                        fullText.append(delta);
                        log.debug("[TOKEN] {}", delta);
                    }
                    logToolCalls(response);
                    logReasoning(response);
                })
                .doOnComplete(() -> {
                    log.info("[FINAL] {}", fullText);
                    if (!promptRecordId.isBlank()) {
                        ChatResponse mergedResponse = mergeStreamResponse(fullText.toString(), lastResponse.get());
                        promptRecordService.recordSuccess(promptRecordId, mergedResponse, elapsed(processed.context()));
                    }
                })
                .doOnError(throwable -> {
                    if (!promptRecordId.isBlank()) {
                        promptRecordService.recordFailure(promptRecordId, throwable, elapsed(processed.context()));
                    }
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatResponse mergeStreamResponse(String fullText, ChatClientResponse lastResponse) {
        if (lastResponse == null || lastResponse.chatResponse() == null) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage(fullText))));
        }
        ChatResponse source = lastResponse.chatResponse();
        Generation sourceGeneration = source.getResult();
        Generation generation = sourceGeneration == null
                ? new Generation(new AssistantMessage(fullText))
                : new Generation(new AssistantMessage(fullText), sourceGeneration.getMetadata());
        return new ChatResponse(List.of(generation), source.getMetadata());
    }

    private void logToolCalls(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null
                || response.chatResponse().getResult() == null
                || response.chatResponse().getResult().getOutput() == null) {
            return;
        }
        AssistantMessage output = response.chatResponse().getResult().getOutput();
        if (output.hasToolCalls()) {
            output.getToolCalls().forEach(tool -> log.info("[TOOL-DELTA] {} -> {}", tool.name(), tool.arguments()));
        }
    }

    private void logReasoning(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null || response.chatResponse().getMetadata() == null) {
            return;
        }
        var metadata = response.chatResponse().getMetadata();
        if (metadata.containsKey("reasoning")) {
            log.info("[REASONING] {}", String.valueOf(metadata.get("reasoning")));
        }
    }

    private String extractText(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null
                || response.chatResponse().getResult() == null
                || response.chatResponse().getResult().getOutput() == null) {
            return null;
        }
        return response.chatResponse().getResult().getOutput().getText();
    }

    private String summarizeMessage(Message message) {
        if (message instanceof ToolResponseMessage toolResponseMessage) {
            return summarizeToolResponseMessage(toolResponseMessage);
        }
        return limit(message.getText(), MAX_MESSAGE_SUMMARY_LENGTH);
    }

    private String summarizeToolResponseMessage(ToolResponseMessage toolResponseMessage) {
        List<ToolResponseMessage.ToolResponse> responses = toolResponseMessage.getResponses();
        if (responses == null || responses.isEmpty()) {
            return "toolResponses=0";
        }
        return "toolResponses=" + responses.size() + " "
                + responses.stream().map(this::summarizeToolResponse).toList();
    }

    private String summarizeToolResponse(ToolResponseMessage.ToolResponse response) {
        String responseData = response.responseData() == null ? "" : response.responseData();
        return "{toolCallId=" + response.id()
                + ", toolName=" + response.name()
                + ", resultChars=" + responseData.length()
                + ", preview=" + limit(responseData, MAX_TOOL_RESULT_PREVIEW_LENGTH)
                + "}";
    }

    private String limit(String content, int maxLength) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private long elapsed(Map<String, Object> context) {
        Object startedAt = context.get(AiAdvisorContextKeys.REQUEST_STARTED_AT);
        if (startedAt instanceof Number number) {
            return Math.max(System.currentTimeMillis() - number.longValue(), 0L);
        }
        return 0L;
    }

    private String contextString(Map<String, Object> context, String key) {
        Object value = context.get(key);
        return value == null ? "" : value.toString();
    }
}
