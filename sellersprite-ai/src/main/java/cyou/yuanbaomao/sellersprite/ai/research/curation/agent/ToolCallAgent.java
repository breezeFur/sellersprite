package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientAttributes;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

@Getter
@Setter
public class ToolCallAgent extends ReActAgent {

    private static final int MAX_TOOL_RESPONSE_LENGTH = 500;
    private static final String TOOL_CONTEXT_CONVERSATION_ID = "conversationId";
    private static final String NO_TOOL_CALL_MESSAGE = "没有工具调用";
    private static final String TOOL_COMPLETED_MESSAGE = "工具执行完成";

    private final ToolCallback[] allTools;
    private String chatId;
    private ChatClient chatClient;
    private ToolCallingManager toolCallingManager;
    private ChatOptions chatOptions;
    private ChatResponse toolCallChatResponse;
    private String finalAnswer;

    public ToolCallAgent(ToolCallback[] allTools, String chatId) {
        this.allTools = allTools == null ? new ToolCallback[0] : allTools;
        this.chatId = chatId;
    }

    public boolean canCallModel() {
        return chatClient != null && allTools.length > 0;
    }

    protected void prepareToolCallRun() {
        this.toolCallChatResponse = null;
        this.finalAnswer = null;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(allTools)
                .toolContext(TOOL_CONTEXT_CONVERSATION_ID, chatId)
                .build();
        setCurrentStep(0);
        setState(AgentState.PENDING);
        setMessageHistory(new ArrayList<>());
    }

    @Override
    public boolean think() {
        if (chatClient == null) {
            throw new IllegalStateException("ChatClient cannot be null");
        }
        if (chatOptions == null) {
            prepareToolCallRun();
        }
        List<Message> contextMessages = new ArrayList<>(getMessageHistory());
        if (getNextStepPrompt() != null && !getNextStepPrompt().isBlank()) {
            contextMessages.add(new UserMessage(getNextStepPrompt()));
        }
        Prompt prompt = new Prompt(contextMessages, chatOptions);
        beforeModelCall();
        try {
            toolCallChatResponse = chatClient.prompt(prompt)
                    .system(getSystemPrompt())
                    .advisors(advisorSpec -> {
                        advisorSpec
                                .param(ChatMemory.CONVERSATION_ID, chatId)
                                .param(ChatClientAttributes.TOOL_CALLING_ADVISOR_AUTO_REGISTER.getKey(), false);
                        customizeAdvisorParams(advisorSpec);
                    })
                    .call()
                    .chatResponse();
        } catch (RuntimeException exception) {
            afterModelCall();
            throw exception;
        }
        afterModelCall();
        AssistantMessage output = toolCallChatResponse.getResult().getOutput();
        onThink(output);
        if (!toolCallChatResponse.hasToolCalls()) {
            getMessageHistory().add(output);
            finalAnswer = output.getText();
            setState(AgentState.COMPLETED);
            return false;
        }
        return true;
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return NO_TOOL_CALL_MESSAGE;
        }
        Prompt prompt = new Prompt(getMessageHistory(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        setMessageHistory(new ArrayList<>(toolExecutionResult.conversationHistory()));
        String result = formatLatestToolResponse();
        onAct(result);
        return result;
    }

    protected void onThink(AssistantMessage output) {
    }

    protected void onAct(String result) {
    }

    protected void customizeAdvisorParams(ChatClient.AdvisorSpec advisorSpec) {
    }

    protected void beforeModelCall() {
    }

    protected void afterModelCall() {
    }

    protected String formatLatestToolResponse() {
        Message lastMessage = getMessageHistory().getLast();
        if (lastMessage instanceof ToolResponseMessage toolResponseMessage) {
            return toolResponseMessage.getResponses().stream()
                    .map(response -> "工具 " + response.name() + " 执行完成："
                            + abbreviate(response.responseData()))
                    .collect(Collectors.joining("\n"));
        }
        return TOOL_COMPLETED_MESSAGE;
    }

    protected String abbreviate(String value) {
        if (value == null || value.length() <= MAX_TOOL_RESPONSE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TOOL_RESPONSE_LENGTH) + "...";
    }
}
