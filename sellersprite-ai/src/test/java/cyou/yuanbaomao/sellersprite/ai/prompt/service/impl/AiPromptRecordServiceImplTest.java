package cyou.yuanbaomao.sellersprite.ai.prompt.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import cyou.yuanbaomao.sellersprite.ai.prompt.enums.AiPromptStatus;
import cyou.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
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
    void shouldPersistMaskedAndTruncatedPromptSummary() {
        String prompt = " password=secret " + "请分析".repeat(800);

        promptRecordService.recordRequest(PROMPT_RECORD_ID, List.of(new UserMessage(prompt)));

        ArgumentCaptor<AiPromptRecord> captor = ArgumentCaptor.forClass(AiPromptRecord.class);
        verify(promptRecordDao).updateById(captor.capture());
        AiPromptRecord record = captor.getValue();
        assertThat(record.getRequestMessages()).doesNotContain("secret").contains("[REDACTED]");
        assertThat(record.getPromptSummary()).contains("password=[REDACTED]").doesNotContain("secret");
        assertThat(record.getPromptSummary()).hasSize(2048);
        assertThat(record.getPromptTruncated()).isEqualTo(1);
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

    @Test
    void shouldKeepPartialResponseWhenStreamFails() {
        promptRecordService.recordStreamFailure(
                PROMPT_RECORD_ID, "已输出部分内容", new IllegalStateException("流式失败"), 30L);

        ArgumentCaptor<AiPromptRecord> captor = ArgumentCaptor.forClass(AiPromptRecord.class);
        verify(promptRecordDao).updateById(captor.capture());
        AiPromptRecord record = captor.getValue();
        assertThat(record.getStatus()).isEqualTo(AiPromptStatus.FAILED.name());
        assertThat(record.getResponseContent()).isEqualTo("已输出部分内容");
        assertThat(record.getErrorType()).isEqualTo(IllegalStateException.class.getName());
        assertThat(record.getErrorMessage()).isEqualTo("流式失败");
        assertThat(record.getCostMs()).isEqualTo(30L);
    }

    @Test
    void shouldMarkCancelledCallAndKeepPartialResponse() {
        promptRecordService.recordCancelled(PROMPT_RECORD_ID, "已输出部分内容", 35L);

        ArgumentCaptor<AiPromptRecord> captor = ArgumentCaptor.forClass(AiPromptRecord.class);
        verify(promptRecordDao).updateById(captor.capture());
        AiPromptRecord record = captor.getValue();
        assertThat(record.getStatus()).isEqualTo(AiPromptStatus.CANCELLED.name());
        assertThat(record.getResponseContent()).isEqualTo("已输出部分内容");
        assertThat(record.getFinishReason()).isEqualTo("cancelled");
        assertThat(record.getErrorType()).isEmpty();
        assertThat(record.getErrorMessage()).isEmpty();
        assertThat(record.getCostMs()).isEqualTo(35L);
    }

    @Test
    void shouldMaskStreamFailurePayloadBeforePersistence() {
        promptRecordService.recordStreamFailure(PROMPT_RECORD_ID, " token=secret ",
                new IllegalStateException(" api_key=secret "), 40L);

        ArgumentCaptor<AiPromptRecord> captor = ArgumentCaptor.forClass(AiPromptRecord.class);
        verify(promptRecordDao).updateById(captor.capture());
        AiPromptRecord record = captor.getValue();
        assertThat(record.getResponseContent()).contains("[REDACTED]").doesNotContain("secret");
        assertThat(record.getErrorMessage()).contains("[REDACTED]").doesNotContain("secret");
    }
}
