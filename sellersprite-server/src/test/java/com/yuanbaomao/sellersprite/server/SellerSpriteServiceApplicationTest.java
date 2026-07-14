package com.yuanbaomao.sellersprite.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.yuanbaomao.sellersprite.ai.advisor.MyLoggerAdvisor;
import com.yuanbaomao.sellersprite.framework.security.TokenAuthInterceptor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
class SellerSpriteServiceApplicationTest {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private ChatMemoryRepository chatMemoryRepository;

    @Autowired
    private MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    @Autowired
    private MyLoggerAdvisor myLoggerAdvisor;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void contextLoads() {
        Set<String> registeredPaths = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(requestMappingInfo -> requestMappingInfo.getPatternValues().stream())
                .collect(Collectors.toSet());

        assertThat(registeredPaths).contains(
                "/api/auth/login",
                "/api/users",
                "/api/roles",
                "/api/depts",
                "/api/dicts/types",
                "/api/system/dicts/types",
                "/api/permissions/functions",
                "/api/ai/chat",
                "/api/ai/conversations",
                "/api/ai/conversations/{conversationId}",
                "/api/market-research/jobs",
                "/api/market-research/jobs/{jobId}",
                "/api/market-research/jobs/{jobId}/download",
                "/api/cache/keys",
                "/api/cache/value",
                "/api/cache/exists",
                "/api/cache/key",
                "/api/cache");
        assertThat(registeredPaths.stream()
                .filter(path -> path.startsWith("/api/sellersprite/")))
                .hasSize(45)
                .anyMatch(path -> path.startsWith("/api/sellersprite/account/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/products/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/asins/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/keywords/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/traffic/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/markets/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/reviews/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/trademarks/"))
                .anyMatch(path -> path.startsWith("/api/sellersprite/tools/"));
        assertThat(TokenAuthInterceptor.publicPathPatterns())
                .noneMatch(path -> path.startsWith("/api/sellersprite/"));
        assertThat(chatMemoryRepository).isInstanceOf(JdbcChatMemoryRepository.class);
        assertThat(messageChatMemoryAdvisor).isNotNull();
        assertThat(myLoggerAdvisor).isNotNull();
    }

    @Test
    void jdbcChatMemoryRepositoryShouldPersistMessages() {
        String conversationId = "019f447a-6e5d-7f80-94c7-9c5e0bdd8090";

        chatMemoryRepository.saveAll(conversationId, List.of(new UserMessage("测试记忆")));

        assertThat(chatMemoryRepository.findByConversationId(conversationId))
                .extracting(message -> message.getMessageType().name(), message -> message.getText())
                .containsExactly(org.assertj.core.groups.Tuple.tuple("USER", "测试记忆"));
        chatMemoryRepository.deleteByConversationId(conversationId);
        assertThat(chatMemoryRepository.findByConversationId(conversationId)).isEmpty();
    }

    @Test
    void sellerSpriteRoutesShouldRequireAccessToken() throws Exception {
        var result = mockMvc.perform(get("/api/sellersprite/account/visits")).andReturn();

        assertThat(result.getResolvedException())
                .isInstanceOf(com.yuanbaomao.base.exception.BizException.class)
                .extracting("code")
                .isEqualTo("A401");
    }

    @Test
    void marketResearchRoutesShouldRequireAccessToken() throws Exception {
        var result = mockMvc.perform(get("/api/market-research/jobs/test-job")).andReturn();

        assertThat(result.getResolvedException())
                .isInstanceOf(com.yuanbaomao.base.exception.BizException.class)
                .extracting("code")
                .isEqualTo("A401");
    }
}
