package com.yuanbaomao.sellersprite.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sellersprite.ai")
public class AiProperties {

    private Chat chat = new Chat();

    @Data
    public static class Chat {

        private boolean enabled = true;

        private String provider = "openai";

        private String model = "gpt-5.5";

        private String defaultSystemPrompt = "你是一个严谨、友好的 AI 助手。";

        private int memoryWindowSize = 20;
    }
}
