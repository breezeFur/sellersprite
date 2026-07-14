package com.yuanbaomao.sellersprite.ai.config;

import com.yuanbaomao.sellersprite.ai.advisor.MyLoggerAdvisor;
import com.yuanbaomao.sellersprite.ai.constants.AiChatConstants;
import com.yuanbaomao.sellersprite.ai.prompt.service.AiPromptRecordService;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModuleConfig {

    @Bean
    public MessageWindowChatMemory messageWindowChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(AiChatConstants.MEMORY_WINDOW_SIZE)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(MessageWindowChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public MyLoggerAdvisor myLoggerAdvisor(AiPromptRecordService promptRecordService) {
        return new MyLoggerAdvisor(promptRecordService);
    }
}
