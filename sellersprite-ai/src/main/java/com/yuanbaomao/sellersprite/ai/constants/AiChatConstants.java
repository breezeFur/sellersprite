package com.yuanbaomao.sellersprite.ai.constants;

/**
 * AI 聊天固定业务参数。
 */
public final class AiChatConstants {

    /** 会话记录使用的模型服务提供方标识。 */
    public static final String PROVIDER_OPENAI = "openai";

    /** 用户未指定系统提示词时使用的默认提示词。 */
    public static final String DEFAULT_SYSTEM_PROMPT = "你是一个严谨、友好的 AI 助手。";

    /** 参与模型上下文的最大消息数。 */
    public static final int MEMORY_WINDOW_SIZE = 20;

    private AiChatConstants() {
    }
}
