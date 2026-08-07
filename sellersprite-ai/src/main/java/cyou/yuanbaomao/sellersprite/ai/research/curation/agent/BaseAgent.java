package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

@Getter
@Setter
public abstract class BaseAgent {

    private String name;

    private String systemPrompt;

    private String nextStepPrompt;

    private int maxSteps = 6;

    private int currentStep;

    private AgentState state = AgentState.PENDING;

    private List<Message> messageHistory = new ArrayList<>();

    public final String run(String userPrompt) {
        if (state != AgentState.PENDING) {
            throw new IllegalStateException("Cannot run agent from state: " + state);
        }
        if (userPrompt == null || userPrompt.isBlank()) {
            throw new IllegalArgumentException("User prompt cannot be empty");
        }

        state = AgentState.RUNNING;
        messageHistory.add(new UserMessage(userPrompt));
        List<String> stepResults = new ArrayList<>();
        try {
            for (int step = 0; step < maxSteps && state == AgentState.RUNNING; step++) {
                currentStep = step;
                stepResults.add(step());
            }
            if (state == AgentState.RUNNING) {
                state = AgentState.MAX_STEPS;
            }
            return String.join("\n", stepResults);
        } catch (RuntimeException ex) {
            state = AgentState.FAILED;
            throw ex;
        } finally {
            cleanup();
        }
    }

    public abstract String step();

    public void cleanup() {
    }
}
