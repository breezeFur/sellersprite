package com.yuanbaomao.sellersprite.ai.advisor;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class MyLoggerAdvisorTest {

    private static final String PROMPT_RECORD_ID = "prompt-record-id";

    @Test
    void shouldPersistFinalPromptObservedAfterMemoryAdvisor() {
        AiPromptRecordService promptRecordService = org.mockito.Mockito.mock(AiPromptRecordService.class);
        MyLoggerAdvisor advisor = new MyLoggerAdvisor(promptRecordService);
        List<org.springframework.ai.chat.messages.Message> messages = List.of(
                new SystemMessage("system"),
                new UserMessage("历史问题"),
                new AssistantMessage("历史回答"),
                new UserMessage("当前问题"));
        ChatClientRequest request = new ChatClientRequest(new Prompt(messages), Map.of(
                AiAdvisorContextKeys.PROMPT_RECORD_ID, PROMPT_RECORD_ID,
                AiAdvisorContextKeys.REQUEST_STARTED_AT, System.currentTimeMillis()));

        advisor.before(request, org.mockito.Mockito.mock(AdvisorChain.class));

        verify(promptRecordService).recordRequest(PROMPT_RECORD_ID, messages);
    }

    @Test
    void shouldPersistModelResponse() {
        AiPromptRecordService promptRecordService = org.mockito.Mockito.mock(AiPromptRecordService.class);
        MyLoggerAdvisor advisor = new MyLoggerAdvisor(promptRecordService);
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("完成"))));
        ChatClientResponse response = new ChatClientResponse(chatResponse, Map.of(
                AiAdvisorContextKeys.PROMPT_RECORD_ID, PROMPT_RECORD_ID,
                AiAdvisorContextKeys.REQUEST_STARTED_AT, System.currentTimeMillis()));

        advisor.after(response, org.mockito.Mockito.mock(AdvisorChain.class));

        verify(promptRecordService).recordSuccess(eq(PROMPT_RECORD_ID), eq(chatResponse), anyLong());
    }
}
