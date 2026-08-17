package cyou.yuanbaomao.sellersprite.ai.conversation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationRenameRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.dto.AiConversationSettingsRequest;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationDetailVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationSettingsVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.model.vo.AiConversationVo;
import cyou.yuanbaomao.sellersprite.ai.conversation.service.AiConversationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AiConversationControllerTest {

    @Test
    void shouldExposeConversationHistoryLifecycleAndSettingsEndpoints() throws Exception {
        AiConversationService service = mock(AiConversationService.class);
        AiConversationVo conversation = new AiConversationVo();
        conversation.setConversationId("conversation-1");
        conversation.setTitle("测试会话");
        AiConversationDetailVo detail = new AiConversationDetailVo();
        detail.setConversation(conversation);
        detail.setMessages(List.of());
        AiConversationSettingsVo settings = new AiConversationSettingsVo();
        settings.setProvider("openai");
        settings.setModel("gpt-5.5");
        settings.setSystemPrompt("你是严谨助手");
        when(service.page(any(YPage.class), org.mockito.ArgumentMatchers.<String>isNull()))
                .thenReturn(YPage.of(1, 20, 1, List.of(conversation)));
        when(service.detail("conversation-1")).thenReturn(detail);
        when(service.rename(eq("conversation-1"), any(AiConversationRenameRequest.class)))
                .thenReturn(conversation);
        when(service.updateSettings(eq("conversation-1"), any(AiConversationSettingsRequest.class)))
                .thenReturn(settings);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AiConversationController(service)).build();

        mockMvc.perform(get("/api/ai/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].conversationId").value("conversation-1"));
        mockMvc.perform(get("/api/ai/conversations/conversation-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversation.title").value("测试会话"));
        mockMvc.perform(put("/api/ai/conversations/conversation-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"新标题\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/ai/conversations/conversation-1/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"systemPrompt\":\"你是严谨助手\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.model").value("gpt-5.5"));
        mockMvc.perform(delete("/api/ai/conversations/conversation-1"))
                .andExpect(status().isOk());
        verify(service).delete("conversation-1");
    }
}
