package com.yuanbaomao.sellersprite.ai.prompt.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.yuanbaomao.sellersprite.ai.prompt.enums.AiPromptStatus;
import com.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import com.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AiPromptRecordServiceImplTest {

    private static final String PROMPT_RECORD_ID = "019f447a-6e5d-7f80-94c7-9c5e0bdd808f";

    @Mock
    private AiPromptRecordDao promptRecordDao;

    private AiPromptRecordServiceImpl promptRecordService;

    @BeforeEach
    void setUp() {
        promptRecordService = new AiPromptRecordServiceImpl(promptRecordDao, new ObjectMapper());
    }

    @Test
    void shouldPersistAllPromptRolesAsJson() {
        promptRecordService.recordRequest(PROMPT_RECORD_ID, List.of(
                new SystemMessage("系统提示"),
                new UserMessage("历史问题"),
                new AssistantMessage("历史回答"),
                new UserMessage("当前问题")));

        ArgumentCaptor<AiPromptRecord> captor = ArgumentCaptor.forClass(AiPromptRecord.class);
        verify(promptRecordDao).updateById(captor.capture());
        assertThat(captor.getValue().getPromptRecordId()).isEqualTo(PROMPT_RECORD_ID);
        assertThat(captor.getValue().getRequestMessages())
                .contains("\"role\":\"SYSTEM\"")
                .contains("\"role\":\"USER\"")
                .contains("\"role\":\"ASSISTANT\"")
                .contains("当前问题");
    }

    @Test
    void shouldMarkFailedCallWithErrorDetails() {
        promptRecordService.recordFailure(PROMPT_RECORD_ID, new IllegalStateException("模型调用失败"), 25L);

        ArgumentCaptor<AiPromptRecord> captor = ArgumentCaptor.forClass(AiPromptRecord.class);
        verify(promptRecordDao).updateById(captor.capture());
        AiPromptRecord record = captor.getValue();
        assertThat(record.getStatus()).isEqualTo(AiPromptStatus.FAILED.name());
        assertThat(record.getErrorType()).isEqualTo(IllegalStateException.class.getName());
        assertThat(record.getErrorMessage()).isEqualTo("模型调用失败");
        assertThat(record.getCostMs()).isEqualTo(25L);
    }
}
