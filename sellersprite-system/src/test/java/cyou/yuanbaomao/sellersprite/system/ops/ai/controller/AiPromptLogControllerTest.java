package cyou.yuanbaomao.sellersprite.system.ops.ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.ai.service.AiPromptLogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AiPromptLogControllerTest {

    @Test
    void shouldExposeAiPromptLogPageAndDetail() throws Exception {
        AiPromptLogService service = mock(AiPromptLogService.class);
        AiPromptLogVo log = new AiPromptLogVo();
        log.setPromptRecordId("prompt-1");
        log.setModel("gpt-5.5");
        when(service.page(any(AiPromptLogPageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(log)));
        when(service.detail("prompt-1")).thenReturn(log);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AiPromptLogController(service)).build();

        mockMvc.perform(get("/api/logs/ai-prompts").param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].promptRecordId").value("prompt-1"));
        mockMvc.perform(get("/api/logs/ai-prompts/prompt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.model").value("gpt-5.5"));
    }
}
