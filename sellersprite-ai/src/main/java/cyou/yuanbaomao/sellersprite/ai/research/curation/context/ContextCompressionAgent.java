package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import cyou.yuanbaomao.sellersprite.ai.research.curation.agent.AmazonSelectionAnalysisException;
import cyou.yuanbaomao.sellersprite.ai.research.curation.budget.CurationAnalysisBudget;
import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ContextCompressionAgent {

    private static final String SYSTEM_PROMPT = """
            你是本项目的上下文压缩 Agent。
            你的任务是把较早的多轮对话压缩成可继续推理的事实摘要，保留用户目标、关键约束、已做结论、待办事项和重要数据。
            不要编造新事实；不要输出寒暄；用中文 Markdown 列点输出。
            """;
    private static final String SUMMARY_TITLE = "【历史上下文压缩摘要】";
    private static final String FALLBACK_SUMMARY_PREFIX = "模型暂不可用，以下是按原文截断保留的历史上下文片段。";

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final CurationAnalysisProperties analysisProperties;

    public ContextCompressionAgent(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            CurationAnalysisProperties analysisProperties) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.analysisProperties = analysisProperties;
    }

    public ContextCompressionResult compress(String conversationId, List<Message> messages) {
        return compress(conversationId, messages, CurationAnalysisBudget.unlimited());
    }

    public ContextCompressionResult compress(
            String conversationId, List<Message> messages, CurationAnalysisBudget analysisBudget) {
        if (messages == null || messages.isEmpty()) {
            return new ContextCompressionResult(List.of(), false);
        }
        CurationAnalysisProperties.ContextCompression properties = analysisProperties.getContextCompression();
        List<Message> systemMessages = messages.stream()
                .filter(SystemMessage.class::isInstance)
                .toList();
        List<Message> nonSystemMessages = messages.stream()
                .filter(message -> !(message instanceof SystemMessage))
                .toList();
        int recentMessageCount = Math.min(properties.getRecentMessagesToKeep(), nonSystemMessages.size());
        int compactEndIndex = nonSystemMessages.size() - recentMessageCount;
        if (compactEndIndex <= 0) {
            return new ContextCompressionResult(List.copyOf(messages), false);
        }

        List<Message> compactedMessages = nonSystemMessages.subList(0, compactEndIndex);
        List<Message> recentMessages = nonSystemMessages.subList(compactEndIndex, nonSystemMessages.size());
        ModelSummary modelSummary = summarizeWithModel(conversationId, compactedMessages, analysisBudget);
        String summary = StringUtils.hasText(modelSummary.content())
                ? modelSummary.content()
                : fallbackSummary(compactedMessages);
        summary = abbreviate(summary, properties.getMaxSummaryChars());

        List<Message> resultMessages = new ArrayList<>();
        resultMessages.addAll(systemMessages);
        resultMessages.add(new UserMessage(SUMMARY_TITLE + "\n" + summary));
        resultMessages.addAll(recentMessages);
        return new ContextCompressionResult(List.copyOf(resultMessages), modelSummary.modelInvoked());
    }

    private ModelSummary summarizeWithModel(
            String conversationId, List<Message> messages, CurationAnalysisBudget analysisBudget) {
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder == null) {
            return new ModelSummary("", false);
        }
        analysisBudget.beforeModelCall();
        try {
            String summary = chatClientBuilder.clone()
                    .build()
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(buildCompressionPrompt(conversationId, messages))
                    .call()
                    .content();
            analysisBudget.afterModelCall();
            return new ModelSummary(summary == null ? "" : summary, true);
        } catch (AmazonSelectionAnalysisException exception) {
            throw exception;
        } catch (RuntimeException ex) {
            analysisBudget.afterModelCall();
            log.warn("上下文压缩模型调用失败，conversationId={}", conversationId, ex);
            return new ModelSummary("", false);
        }
    }

    private String buildCompressionPrompt(String conversationId, List<Message> messages) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("会话 ID：")
                .append(conversationId)
                .append("\n请压缩以下较早历史消息，输出可供下一轮模型继续理解的摘要。\n\n");
        for (Message message : messages) {
            prompt.append("## ")
                    .append(message.getMessageType().name())
                    .append("\n")
                    .append(nullToEmpty(ContextMessageText.extract(message)))
                    .append("\n\n");
        }
        return prompt.toString();
    }

    private String fallbackSummary(List<Message> messages) {
        StringBuilder summary = new StringBuilder(FALLBACK_SUMMARY_PREFIX);
        for (Message message : messages) {
            summary.append("\n- ")
                    .append(message.getMessageType().name())
                    .append("：")
                    .append(nullToEmpty(ContextMessageText.extract(message)).replace('\n', ' '));
        }
        return summary.toString();
    }

    private String abbreviate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "...";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ModelSummary(String content, boolean modelInvoked) {
    }
}
