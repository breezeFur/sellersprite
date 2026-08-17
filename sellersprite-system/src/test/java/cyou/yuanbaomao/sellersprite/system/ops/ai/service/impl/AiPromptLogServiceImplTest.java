package cyou.yuanbaomao.sellersprite.system.ops.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiPromptLogServiceImplTest {

    @Mock
    private AiPromptRecordDao promptRecordDao;

    @InjectMocks
    private AiPromptLogServiceImpl promptLogService;

    @Test
    void shouldPageByUserConversationProviderModelStatusAndTime() {
        AiPromptLogPageRequest request = new AiPromptLogPageRequest();
        request.setUserId("user-1");
        request.setConversationId("conversation-1");
        request.setProvider("openai");
        request.setModel("gpt-5.5");
        request.setStatus("FAILED");
        request.setStartTime(100L);
        request.setEndTime(200L);
        Page<AiPromptRecord> page = Page.of(2, 10, 1);
        page.setRecords(List.of(promptRecord()));
        when(promptRecordDao.page("user-1", "conversation-1", "openai", "gpt-5.5", "FAILED",
                100L, 200L, 2L, 10L)).thenReturn(page);

        cyou.yuanbaomao.mybatis.result.YPage<AiPromptLogVo> result = promptLogService.page(
                cyou.yuanbaomao.mybatis.result.YPage.of(2L, 10L), request);

        assertThat(result.getRecords()).extracting(AiPromptLogVo::getPromptRecordId)
                .containsExactly("prompt-1");
        assertThat(result.getRecords().getFirst())
                .extracting("status", "model", "promptTokens", "completionTokens", "totalTokens")
                .containsExactly("FAILED", "gpt-5.5", 10, 5, 15);
        assertThat(result.getRecords().getFirst())
                .extracting("requestMessages", "responseContent", "responseMetadata")
                .containsOnlyNulls();
        verify(promptRecordDao).page("user-1", "conversation-1", "openai", "gpt-5.5", "FAILED",
                100L, 200L, 2L, 10L);
    }

    @Test
    void shouldReturnDetailOrNotFound() {
        when(promptRecordDao.findById("prompt-1")).thenReturn(Optional.of(promptRecord()));
        when(promptRecordDao.findById("missing")).thenReturn(Optional.empty());

        AiPromptLogVo detail = promptLogService.detail("prompt-1");
        assertThat(detail.getPromptSummary()).isEqualTo("请帮我分析");
        assertThat(detail.getRequestMessages()).contains("[REDACTED]").doesNotContain("secret");
        assertThat(detail.getResponseContent()).contains("[REDACTED]").doesNotContain("secret");
        assertThatThrownBy(() -> promptLogService.detail("missing"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(ResultCode.RESOURCE_NOT_FOUND.getCode()));
    }

    private AiPromptRecord promptRecord() {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId("prompt-1");
        record.setConversationId("conversation-1");
        record.setUserId("user-1");
        record.setProvider("openai");
        record.setModel("gpt-5.5");
        record.setRequestMessages("{\"password\":\"secret\"}");
        record.setPromptSummary("请帮我分析");
        record.setPromptTruncated(0);
        record.setResponseContent(" token=secret ");
        record.setResponseMetadata("{\"api_key\":\"secret\"}");
        record.setPromptTokens(10);
        record.setCompletionTokens(5);
        record.setTotalTokens(15);
        record.setStatus("FAILED");
        record.setCostMs(12L);
        record.setTraceId("trace-1");
        record.setCreatedAt(150L);
        return record;
    }
}
